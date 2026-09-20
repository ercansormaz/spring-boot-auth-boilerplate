package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.request.AnonymousAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.facade.AuthenticationFacade;
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
@ConditionalOnProperty(name = "auth.anonymous.enabled", havingValue = "true")
public class AnonymousAuthController {

  private final AuthenticationFacade authenticationFacade;

  @PostMapping(ApiEndpoints.ANONYMOUS_AUTH)
  public AuthResponse authenticateAnonymously(@Valid @RequestBody AnonymousAuthRequest request) {
    return authenticationFacade.authenticate(request, AuthProviderType.ANONYMOUS);
  }

}
