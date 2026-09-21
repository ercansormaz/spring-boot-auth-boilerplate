package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

  public void delete(RefreshToken refreshToken) {
    refreshTokenRepository.delete(refreshToken);
  }

}
