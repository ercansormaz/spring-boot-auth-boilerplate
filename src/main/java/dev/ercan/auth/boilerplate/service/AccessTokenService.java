package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static dev.ercan.auth.boilerplate.config.CaffeineCacheConfig.EXPIRE_AFTER_WRITE;
import static dev.ercan.auth.boilerplate.constant.CacheTypes.ACCESS_TOKEN;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = ACCESS_TOKEN, cacheManager = EXPIRE_AFTER_WRITE)
public class AccessTokenService {

  private final AccessTokenRepository accessTokenRepository;

  @CacheEvict(key = "#token.id", condition = "#token != null and #token.id != null")
  public AccessToken save(AccessToken token) {
    return accessTokenRepository.save(token);
  }

  @Cacheable(key = "#id", unless = "#result == null")
  public AccessToken getById(Long id) {
    return accessTokenRepository.findById(id).orElse(null);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public List<AccessToken> getExpiredTokens(int count) {
    return accessTokenRepository.findByExpiresAtBefore(Instant.now(), PageRequest.of(0, count));
  }

  @CacheEvict(key = "#token.id", condition = "#token != null and #token.id != null")
  public void delete(AccessToken token) {
    accessTokenRepository.delete(token);
  }

  public AccessToken getByDevice(Device device) {
    return accessTokenRepository.findByDevice(device);
  }

}
