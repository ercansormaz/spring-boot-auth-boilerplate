package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.GoogleAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.facade.AuthenticationFacade;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.google.enabled", havingValue = "true")
public class GoogleAuthController {

  private final AuthenticationFacade authenticationFacade;

  @PostMapping(ApiEndpoints.GOOGLE_AUTH)
  public AuthResponse authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
    return authenticationFacade.authenticate(request, AuthProviderType.GOOGLE);
  }

}
