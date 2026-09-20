package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.LoginHistory;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

  private final LoginHistoryRepository loginHistoryRepository;

  public void recordLogin(Device device, AuthProviderType authProviderType) {
    LoginHistory loginHistory = new LoginHistory();
    loginHistory.setDevice(device);
    loginHistory.setType(authProviderType);
    loginHistoryRepository.save(loginHistory);
  }

}
