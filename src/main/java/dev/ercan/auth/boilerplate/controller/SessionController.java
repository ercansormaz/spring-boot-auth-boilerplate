package dev.ercan.auth.boilerplate.controller;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.dto.response.ActiveSessionResponse;
import dev.ercan.auth.boilerplate.facade.SessionFacade;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.SESSIONS)
@RequiredArgsConstructor
public class SessionController {

  private final SessionFacade sessionFacade;

  @GetMapping
  public List<ActiveSessionResponse> getActiveSessions(Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    AuthTokenDetail authTokenDetail = (AuthTokenDetail) authentication.getDetails();
    return sessionFacade.getActiveSessions(account, authTokenDetail.getDeviceId());
  }

  @DeleteMapping("/{deviceId}")
  public void logoutByDevice(@PathVariable UUID deviceId, Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    sessionFacade.logout(account, deviceId);
  }

  @DeleteMapping("/others")
  public void logoutOthers(Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    AuthTokenDetail authTokenDetail = (AuthTokenDetail) authentication.getDetails();
    sessionFacade.logoutOthers(account, authTokenDetail.getDeviceId());
  }

  @DeleteMapping
  public void logoutAll(Authentication authentication) {
    Account account = (Account) authentication.getPrincipal();
    sessionFacade.logoutAll(account);
  }

}
