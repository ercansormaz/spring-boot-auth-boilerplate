package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.RefreshTokenAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.facade.AuthenticationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshTokenAuthController {

  private final AuthenticationFacade authenticationFacade;

  @PostMapping(ApiEndpoints.REFRESH_TOKEN_AUTH)
  public AuthResponse authenticateWithRefreshToken(@Valid @RequestBody RefreshTokenAuthRequest request) {
    return authenticationFacade.authenticate(request, AuthProviderType.REFRESH_TOKEN);
  }

}
