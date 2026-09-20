package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.repository.RefreshTokenRepository;
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
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${auth.token.refresh.duration}")
  private Duration duration;

  public RefreshToken getById(Long id) {
    return refreshTokenRepository.findById(id).orElse(null);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public RefreshToken rotate(Device device) {
    RefreshToken refreshToken = refreshTokenRepository.findByDevice(device);

    if (Objects.isNull(refreshToken)) {
      return create(device);
    }

    return update(refreshToken);
  }

  public void deleteByDevice(Device device) {
    refreshTokenRepository.deleteByDevice(device);
  }

  private RefreshToken create(Device device) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setDevice(device);
    return update(refreshToken);
  }

  private RefreshToken update(RefreshToken refreshToken) {
    refreshToken.setSalt(UUID.randomUUID().toString());
    refreshToken.setExpiresAt(Instant.now().plusSeconds(duration.toSeconds()));
    return refreshTokenRepository.save(refreshToken);
  }

}
