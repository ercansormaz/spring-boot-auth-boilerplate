package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

  Device findByAccountAndIdentifier(Account account, String identifier);

}
