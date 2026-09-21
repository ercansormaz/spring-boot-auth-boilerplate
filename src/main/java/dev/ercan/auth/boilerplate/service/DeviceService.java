package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.dto.request.DeviceRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;

  public Device getById(UUID id) {
    return deviceRepository.findById(id).orElse(null);
  }

  public Device getByAccountAndId(Account account, UUID id) {
    return deviceRepository.findByAccountAndId(account, id);
  }

  public Device registerOrUpdateDevice(Account account, DeviceRequest deviceRequest) {
    Device device = deviceRepository.findByAccountAndIdentifier(account, deviceRequest.getIdentifier());

    if (device == null) {
      device = buildNewDevice(account, deviceRequest);

      try {
        return deviceRepository.save(device);
      } catch (DataIntegrityViolationException e) {
        device = deviceRepository.findByAccountAndIdentifier(account, deviceRequest.getIdentifier());
      }
    }

    updateDeviceFields(device, deviceRequest);

    return deviceRepository.save(device);
  }

  public List<Device> getActivesByAccount(Account account) {
    return deviceRepository.findByAccountAndActive(account, true);
  }

  public void deactivate(Device device) {
    device.setActive(false);
    deviceRepository.save(device);
  }

  private Device buildNewDevice(Account account, DeviceRequest request) {
    Device device = new Device();
    device.setAccount(account);
    device.setIdentifier(request.getIdentifier());

    updateDeviceFields(device, request);

    return device;
  }

  private void updateDeviceFields(Device device, DeviceRequest request) {
    device.setModel(request.getModel());
    device.setPlatform(request.getPlatform());
    device.setType(request.getType());
    device.setLanguage(request.getLanguage());
    device.setOsVersion(request.getOsVersion());
    device.setAppVersion(request.getAppVersion());
    device.setActive(true);
  }
}
