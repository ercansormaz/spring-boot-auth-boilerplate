package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken save(RefreshToken refreshToken) {
    return refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken getById(Long id) {
    return refreshTokenRepository.findById(id).orElse(null);
  }

  public RefreshToken getByDevice(Device device) {
    return refreshTokenRepository.findByDevice(device);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public List<RefreshToken> getExpiredTokens(int count) {
    return refreshTokenRepository.findByExpiresAtBefore(Instant.now(), PageRequest.of(0, count));
  }

  public void delete(RefreshToken refreshToken) {
    refreshTokenRepository.delete(refreshToken);
  }

}
