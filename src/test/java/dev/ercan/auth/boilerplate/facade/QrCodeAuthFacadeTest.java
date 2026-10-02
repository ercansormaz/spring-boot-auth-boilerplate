package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import dev.ercan.auth.boilerplate.dto.request.QrAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.exception.BadRequestException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.QrCode;
import dev.ercan.auth.boilerplate.model.entity.QrCode.QrCodeStatus;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.QrCodeDetail;
import dev.ercan.auth.boilerplate.service.QrCodeService;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import dev.ercan.auth.boilerplate.util.HMacUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QrCodeAuthFacadeTest {

  @Test
  void generatesPngContainingSignedQrData() throws Exception {
    QrCodeService qrCodeService = mock(QrCodeService.class);
    AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
    QrCodeAuthFacade facade = facade(qrCodeService, authenticationFacade);
    Duration ttl = Duration.ofMinutes(2);
    ReflectionTestUtils.setField(facade, "qrCodeTtl", ttl);
    QrCode qrCode = qrCode(QrCodeStatus.PENDING);
    when(qrCodeService.create(ttl)).thenReturn(qrCode);

    QrCodeDetail detail = facade.generateQrCode(250, 250);

    BufferedImage image = ImageIO.read(new ByteArrayInputStream(detail.image()));
    String encodedData = new QRCodeReader().decode(
        new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText();
    String decodedData = new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
    String expectedSignature = HMacUtil.hmac(
        qrCode.getNonce().toString(),
        qrCode.getId() + ":" + qrCode.getExpiresAt().getEpochSecond());

    assertEquals(qrCode.getId(), detail.id());
    assertEquals(qrCode.getExpiresAt(), detail.expiresAt());
    assertEquals(qrCode.getId() + ":" + expectedSignature, decodedData);
    verify(qrCodeService).create(ttl);
  }

  @Test
  void authenticatesImmediatelyWhenQrCodeIsAlreadyApproved() {
    QrCodeService qrCodeService = mock(QrCodeService.class);
    AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
    QrCodeAuthFacade facade = facade(qrCodeService, authenticationFacade);
    QrAuthRequest request = request(UUID.randomUUID());
    Account account = new Account();
    AuthResponse expected = new AuthResponse();
    when(qrCodeService.consumeIfApproved(UUID.fromString(request.getId()))).thenReturn(Optional.of(account));
    when(authenticationFacade.authenticate(request, AuthProviderType.QR)).thenReturn(expected);

    AuthResponse response = facade.authenticate(request).join().orElseThrow();

    assertSame(expected, response);
    assertSame(account, request.getAccount());
    verify(authenticationFacade).authenticate(request, AuthProviderType.QR);
  }

  @Test
  void deliversApprovalToWaitingAuthenticationAndDeletesQrCode() throws Exception {
    QrCodeService qrCodeService = mock(QrCodeService.class);
    AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
    QrCodeAuthFacade facade = facade(qrCodeService, authenticationFacade);
    QrCode qrCode = qrCode(QrCodeStatus.PENDING);
    Account account = new Account();
    QrAuthRequest request = request(qrCode.getId());
    AuthResponse expected = new AuthResponse();
    when(qrCodeService.consumeIfApproved(qrCode.getId())).thenReturn(Optional.empty());
    when(qrCodeService.getById(qrCode.getId())).thenReturn(qrCode);
    when(authenticationFacade.authenticate(request, AuthProviderType.QR)).thenReturn(expected);

    var responseFuture = facade.authenticate(request);
    facade.approve(qrData(qrCode), account);

    assertSame(expected, responseFuture.get(1, TimeUnit.SECONDS).orElseThrow());
    assertSame(account, request.getAccount());
    verify(qrCodeService).delete(qrCode);
    verify(qrCodeService, never()).save(qrCode);
  }

  @Test
  void persistsApprovalWhenNoAuthenticationRequestIsWaiting() throws Exception {
    QrCodeService qrCodeService = mock(QrCodeService.class);
    QrCodeAuthFacade facade = facade(qrCodeService, mock(AuthenticationFacade.class));
    QrCode qrCode = qrCode(QrCodeStatus.PENDING);
    Account account = new Account();
    when(qrCodeService.getById(qrCode.getId())).thenReturn(qrCode);

    facade.approve(qrData(qrCode), account);

    assertEquals(QrCodeStatus.APPROVED, qrCode.getStatus());
    assertSame(account, qrCode.getAccount());
    verify(qrCodeService).save(qrCode);
  }

  @Test
  void rejectsQrDataWithInvalidSignature() throws Exception {
    QrCodeService qrCodeService = mock(QrCodeService.class);
    QrCodeAuthFacade facade = facade(qrCodeService, mock(AuthenticationFacade.class));
    QrCode qrCode = qrCode(QrCodeStatus.PENDING);
    when(qrCodeService.getById(qrCode.getId())).thenReturn(qrCode);
    String invalidData = Base64.getEncoder().encodeToString(
        (qrCode.getId() + ":invalid-signature").getBytes(StandardCharsets.UTF_8));

    BadRequestException exception = assertThrows(
        BadRequestException.class, () -> facade.approve(invalidData, new Account()));

    assertEquals(ErrorType.INVALID_QR_CODE, exception.getErrorType());
    verify(qrCodeService, never()).save(qrCode);
    verify(qrCodeService, never()).delete(qrCode);
  }

  private static QrCodeAuthFacade facade(
      QrCodeService qrCodeService, AuthenticationFacade authenticationFacade) {
    RateLimiter rateLimiter = mock(RateLimiter.class);
    when(rateLimiter.tryConsume(anyString(), anyInt(), any())).thenReturn(true);
    QrCodeAuthFacade facade = new QrCodeAuthFacade(qrCodeService, authenticationFacade, rateLimiter);
    ReflectionTestUtils.setField(facade, "qrCodeTtl", Duration.ofMinutes(2));
    ReflectionTestUtils.setField(facade, "pollTimeout", Duration.ofSeconds(5));
    return facade;
  }

  private static QrAuthRequest request(UUID id) {
    QrAuthRequest request = new QrAuthRequest();
    request.setId(id.toString());
    return request;
  }

  private static QrCode qrCode(QrCodeStatus status) {
    QrCode qrCode = new QrCode();
    qrCode.setId(UUID.randomUUID());
    qrCode.setNonce(UUID.randomUUID());
    qrCode.setStatus(status);
    qrCode.setExpiresAt(Instant.now().plusSeconds(120));
    return qrCode;
  }

  private static String qrData(QrCode qrCode) throws Exception {
    String signature = qrCode.getId() + ":" + HMacUtil.hmac(
        qrCode.getNonce().toString(),
        qrCode.getId() + ":" + qrCode.getExpiresAt().getEpochSecond());
    return Base64.getEncoder().encodeToString(signature.getBytes(StandardCharsets.UTF_8));
  }
}
