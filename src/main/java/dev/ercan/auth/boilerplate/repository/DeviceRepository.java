package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

  Device findByAccountAndIdentifier(Account account, String identifier);

  Device findByAccountAndId(Account account, UUID id);

  List<Device> findByAccountAndActive(Account account, boolean active);

}
