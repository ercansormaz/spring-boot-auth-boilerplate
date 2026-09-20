package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

  private final AccessTokenRepository accessTokenRepository;

  @Value("${auth.token.access.duration}")
  private Duration duration;

  public AccessToken getById(Long id) {
    return accessTokenRepository.findById(id).orElse(null);
  }

  public AccessToken getByDevice(Device device) {
    return accessTokenRepository.findByDevice(device);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public AccessToken rotate(Device device) {
    AccessToken accessToken = accessTokenRepository.findByDevice(device);

    if (Objects.isNull(accessToken)) {
      return create(device);
    }

    return update(accessToken);
  }

  public void deleteByDevice(Device device) {
    accessTokenRepository.deleteByDevice(device);
  }

  private AccessToken create(Device device) {
    AccessToken accessToken = new AccessToken();
    accessToken.setDevice(device);
    return update(accessToken);
  }

  private AccessToken update(AccessToken accessToken) {
    accessToken.setSalt(UUID.randomUUID().toString());
    accessToken.setExpiresAt(Instant.now().plusSeconds(duration.toSeconds()));
    return accessTokenRepository.save(accessToken);
  }

}
