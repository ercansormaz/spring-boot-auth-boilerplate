package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.LoginHistory;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.repository.LoginHistoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoginHistoryServiceTest {

  @Test
  void recordsLoginUsingDeviceAndProviderType() {
    LoginHistoryRepository repository = mock(LoginHistoryRepository.class);
    LoginHistoryService service = new LoginHistoryService(repository);
    Device device = new Device();

    service.recordLogin(device, AuthProviderType.GOOGLE);

    ArgumentCaptor<LoginHistory> history = ArgumentCaptor.forClass(LoginHistory.class);
    verify(repository).save(history.capture());
    assertSame(device, history.getValue().getDevice());
    assertEquals(AuthProviderType.GOOGLE, history.getValue().getType());
  }
}
