package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.facade.SessionFacade;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.LOGOUT)
@RequiredArgsConstructor
public class LogoutController {

  private final SessionFacade sessionFacade;

  @DeleteMapping
  public void logout(Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    AuthTokenDetail authTokenDetail = (AuthTokenDetail) authentication.getDetails();
    sessionFacade.logout(account, authTokenDetail.getDeviceId());
  }

}
