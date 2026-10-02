package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.QrApproveRequest;
import dev.ercan.auth.boilerplate.dto.request.QrAuthRequest;
import dev.ercan.auth.boilerplate.facade.QrCodeAuthFacade;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import dev.ercan.auth.boilerplate.model.pojo.QrCodeDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@Validated
@RestController
@RequestMapping(ApiEndpoints.QR_AUTH)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.qr.enabled", havingValue = "true")
public class QrAuthController {

  private static final String X_QR_ID = "X-Qr-Id";
  private static final String X_EXPIRES_AT = "X-Expires-At";
  private static final String X_POLL_TIMEOUT = "X-Poll-Timeout";

  private final QrCodeAuthFacade qrCodeAuthFacade;

  @RateLimit(type = RateLimitType.QR_IMAGE_GENERATE_SUCCESS, scope = RateLimitScope.IP, statuses = {HttpStatus.OK})
  @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQrCode(
      @Min(value = 100) @Max(value = 1024) @RequestParam(required = false, defaultValue = "250") int width,
      @Min(value = 100) @Max(value = 1024) @RequestParam(required = false, defaultValue = "250") int height) {
    QrCodeDetail qrCodeDetail = qrCodeAuthFacade.generateQrCode(width, height);
    // @formatter:off
    return ResponseEntity.ok()
        .header(X_QR_ID, qrCodeDetail.id().toString())
        .header(X_EXPIRES_AT, String.valueOf(qrCodeDetail.expiresAt().getEpochSecond()))
        .header(X_POLL_TIMEOUT, String.valueOf(qrCodeDetail.pollingTimeout()))
        .body(qrCodeDetail.image());
    // @formatter:on
  }

  @PostMapping
  public CompletableFuture<ResponseEntity<?>> authenticateWithQr(@Valid @RequestBody QrAuthRequest request) {
    return qrCodeAuthFacade.authenticate(request)
        .thenApply(authResponseOpt -> authResponseOpt
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.ACCEPTED).build()));
  }

  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approveQr(@Valid @RequestBody QrApproveRequest request, Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    qrCodeAuthFacade.approve(request.getData(), account);
  }


}
