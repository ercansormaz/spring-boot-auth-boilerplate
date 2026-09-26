package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.request.DeviceRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.enums.DevicePlatform;
import dev.ercan.auth.boilerplate.model.enums.DeviceType;
import dev.ercan.auth.boilerplate.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DeviceServiceTest {

  @Test
  void registersNewDeviceWithRequestFieldsAndActiveState() {
    DeviceRepository repository = mock(DeviceRepository.class);
    DeviceService service = new DeviceService(repository);
    Account account = new Account();
    when(repository.findByAccountAndIdentifier(account, "device-1")).thenReturn(null);
    when(repository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Device result = service.registerOrUpdateDevice(account, request());

    ArgumentCaptor<Device> savedDevice = ArgumentCaptor.forClass(Device.class);
    verify(repository).save(savedDevice.capture());
    assertSame(account, result.getAccount());
    assertEquals("device-1", result.getIdentifier());
    assertEquals("Phone", result.getModel());
    assertEquals(DevicePlatform.IOS, result.getPlatform());
    assertEquals(DeviceType.PHONE, result.getType());
    assertEquals("en", result.getLanguage());
    assertEquals("1.2.3", result.getAppVersion());
    assertEquals("17", result.getOsVersion());
    assertTrue(savedDevice.getValue().isActive());
  }

  @Test
  void refreshesExistingDeviceFieldsAndReactivatesIt() {
    DeviceRepository repository = mock(DeviceRepository.class);
    DeviceService service = new DeviceService(repository);
    Account account = new Account();
    Device existing = new Device();
    existing.setIdentifier("device-1");
    existing.setActive(false);
    when(repository.findByAccountAndIdentifier(account, "device-1")).thenReturn(existing);
    when(repository.save(existing)).thenReturn(existing);

    Device result = service.registerOrUpdateDevice(account, request());

    assertSame(existing, result);
    assertTrue(result.isActive());
    assertEquals("Phone", result.getModel());
    assertEquals(DevicePlatform.IOS, result.getPlatform());
    verify(repository).save(existing);
  }

  private static DeviceRequest request() {
    DeviceRequest request = new DeviceRequest();
    request.setIdentifier("device-1");
    request.setModel("Phone");
    request.setPlatform(DevicePlatform.IOS);
    request.setType(DeviceType.PHONE);
    request.setLanguage("en");
    request.setAppVersion("1.2.3");
    request.setOsVersion("17");
    return request;
  }
}
