package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  RefreshToken findByDevice(Device device);

  @Transactional
  void deleteByDevice(Device device);

}
