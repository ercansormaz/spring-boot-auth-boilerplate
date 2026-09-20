package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.AppleAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.facade.AuthenticationFacade;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.apple.enabled", havingValue = "true")
public class AppleAuthController {

  private final AuthenticationFacade authenticationFacade;

  @RateLimit(type = RateLimitType.APPLE_LOGIN_FAIL, scope = RateLimitScope.IP, statuses = {HttpStatus.UNAUTHORIZED})
  @PostMapping(ApiEndpoints.APPLE_AUTH)
  public AuthResponse authenticateWithApple(@Valid @RequestBody AppleAuthRequest request) {
    return authenticationFacade.authenticate(request, AuthProviderType.APPLE);
  }

}
