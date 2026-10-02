package dev.ercan.auth.boilerplate.facade;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.ercan.auth.boilerplate.dto.request.QrAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.exception.BadRequestException;
import dev.ercan.auth.boilerplate.exception.RateLimitExceedException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.QrCode;
import dev.ercan.auth.boilerplate.model.entity.QrCode.QrCodeStatus;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import dev.ercan.auth.boilerplate.model.pojo.QrCodeDetail;
import dev.ercan.auth.boilerplate.service.QrCodeService;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import dev.ercan.auth.boilerplate.util.HMacUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeAuthFacade {

  private static final String RETRY_LIMIT_KEY = "qr:auth:retry:%s";
  private static final String DEFAULT_IMAGE_FORMAT = "png";
  private static final String HMAC_DATA = "%s:%s";

  private final QrCodeService qrCodeService;
  private final AuthenticationFacade authenticationFacade;
  private final RateLimiter rateLimiter;

  private final Map<String, PendingQrAuth> pendingAuths = new ConcurrentHashMap<>();

  @Value("${auth.qr.ttl}")
  private Duration qrCodeTtl;

  @Value("${auth.qr.poll-timeout:5s}")
  private Duration pollTimeout;

  public QrCodeDetail generateQrCode(int width, int height) {
    QrCode qrCode = qrCodeService.create(qrCodeTtl);

    String signature = qrCode.getId() + ":" + encrypt(qrCode);
    String qrData = Base64.getEncoder().withoutPadding().encodeToString(signature.getBytes(StandardCharsets.UTF_8));

    try {
      return new QrCodeDetail(qrCode.getId(), qrCode.getExpiresAt(), generateQrCodeImage(qrData, width, height),
          pollTimeout.toSeconds());
    } catch (WriterException | IOException e) {
      log.error("[QR_CODE_FACADE] [QR_GENERATION_FAILED]", e);
      throw new IllegalStateException("Failed to generate QR code", e);
    }
  }

  public CompletableFuture<Optional<AuthResponse>> authenticate(QrAuthRequest request) {
    UUID qrId = parseQrId(request.getId());

    String retryLimitKey = String.format(RETRY_LIMIT_KEY, request.getId());
    if (!rateLimiter.tryConsume(retryLimitKey, 1, pollTimeout.minusSeconds(2))) {
      throw new RateLimitExceedException(RateLimitType.QR_AUTH_REQUESTED.getErrorType());
    }

    Optional<Account> approvedAccount = qrCodeService.consumeIfApproved(qrId);
    if (approvedAccount.isPresent()) {
      request.setAccount(approvedAccount.get());
      AuthResponse authResponse = authenticationFacade.authenticate(request, AuthProviderType.QR);
      return CompletableFuture.completedFuture(Optional.of(authResponse));
    }

    CompletableFuture<Optional<Account>> accountFuture = new CompletableFuture<>();
    accountFuture.completeOnTimeout(Optional.empty(), pollTimeout.toMillis(), TimeUnit.MILLISECONDS);
    accountFuture.whenComplete((res, ex) -> pendingAuths.remove(request.getId()));

    // @formatter:off
    CompletableFuture<Optional<AuthResponse>> responseFuture = accountFuture.thenApply(optAccount ->
        optAccount.map(account -> {
          request.setAccount(account);
          return authenticationFacade.authenticate(request, AuthProviderType.QR);
        })
    );
    // @formatter:on

    pendingAuths.put(request.getId(), new PendingQrAuth(request, accountFuture));

    // Double-check: approval may have arrived between consumeIfApproved and put().
    Optional<Account> doubleCheckAccount = qrCodeService.consumeIfApproved(qrId);
    if (doubleCheckAccount.isPresent()) {
      PendingQrAuth pending = pendingAuths.remove(request.getId());
      if (Objects.nonNull(pending)) {
        pending.accountFuture().complete(doubleCheckAccount);
      }
    }

    return responseFuture;
  }

  @Transactional
  public void approve(String qrData, Account account) {
    QrCode qrCode = validate(qrData);
    String qrId = qrCode.getId().toString();

    PendingQrAuth pending = pendingAuths.remove(qrId);
    if (Objects.nonNull(pending)) {
      boolean delivered = pending.accountFuture().complete(Optional.of(account));
      if (delivered) {
        qrCodeService.delete(qrCode);
      } else {
        log.warn("[QR_CODE_FACADE] [APPROVE_AFTER_TIMEOUT] [QR_ID={}]", qrId);
        markAsApproved(qrCode, account);
      }
    } else {
      markAsApproved(qrCode, account);
    }
  }

  private QrCode validate(String qrData) {
    String decodedQrData;
    try {
      decodedQrData = new String(Base64.getDecoder().decode(qrData), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }

    String[] parts = decodedQrData.trim().split(":");
    if (parts.length != 2) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }

    UUID qrId = parseQrId(parts[0]);

    QrCode qrCode = qrCodeService.getById(qrId);
    if (Objects.isNull(qrCode) || !qrCode.getStatus().equals(QrCodeStatus.PENDING)) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }

    if (Instant.now().isAfter(qrCode.getExpiresAt())) {
      qrCodeService.delete(qrCode);
      throw new BadRequestException(ErrorType.EXPIRED_QR_CODE);
    }

    String hmac = encrypt(qrCode);
    if (!MessageDigest.isEqual(parts[1].getBytes(UTF_8), hmac.getBytes(UTF_8))) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }

    return qrCode;
  }

  private UUID parseQrId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }
  }

  private void markAsApproved(QrCode qrCode, Account account) {
    qrCode.setStatus(QrCodeStatus.APPROVED);
    qrCode.setAccount(account);
    qrCodeService.save(qrCode);
  }

  private byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

    var qrcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    ImageIO.write(qrcodeImage, DEFAULT_IMAGE_FORMAT, pngOutputStream);

    return pngOutputStream.toByteArray();
  }

  private String encrypt(QrCode qrCode) {
    try {
      String data = String.format(HMAC_DATA, qrCode.getId(), qrCode.getExpiresAt().getEpochSecond());
      return HMacUtil.hmac(qrCode.getNonce().toString(), data);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("[QR_CODE_FACADE] [HMAC_GENERATION_FAILED]", e);
      throw new IllegalStateException("Failed to generate QR code signature", e);
    }
  }

  private record PendingQrAuth(QrAuthRequest request, CompletableFuture<Optional<Account>> accountFuture) {}
}
