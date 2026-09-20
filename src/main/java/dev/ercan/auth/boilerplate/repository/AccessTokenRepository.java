package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

  AccessToken findByDevice(Device device);

  @Transactional
  void deleteByDevice(Device device);
  
}
