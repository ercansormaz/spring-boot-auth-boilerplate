package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.EmailAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.EmailOtpRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.dto.response.OtpResponse;
import dev.ercan.auth.boilerplate.facade.AuthenticationFacade;
import dev.ercan.auth.boilerplate.facade.EmailOtpFacade;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.email.enabled", havingValue = "true")
public class EmailAuthController {

  private final AuthenticationFacade authenticationFacade;
  private final EmailOtpFacade emailOtpFacade;

  @RateLimit(type = RateLimitType.EMAIL_OTP_REQUESTED, scope = RateLimitScope.IP, statuses = {HttpStatus.ACCEPTED})
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PutMapping(ApiEndpoints.EMAIL_AUTH)
  public OtpResponse getOtp(@Valid @RequestBody EmailOtpRequest request) {
    return emailOtpFacade.createAndSendOtp(OtpFlowType.AUTHENTICATION, request.getEmail());
  }

  @PostMapping(ApiEndpoints.EMAIL_AUTH)
  public AuthResponse authenticateWithEmail(@Valid @RequestBody EmailAuthRequest request) {
    return authenticationFacade.authenticate(request, AuthProviderType.EMAIL);
  }

}
