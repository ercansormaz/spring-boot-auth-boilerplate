package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static dev.ercan.auth.boilerplate.config.CaffeineCacheConfig.EXPIRE_AFTER_ACCESS;
import static dev.ercan.auth.boilerplate.constant.CacheTypes.ACCOUNT;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = ACCOUNT, cacheManager = EXPIRE_AFTER_ACCESS)
public class AccountService {

  private final AccountRepository accountRepository;

  @CacheEvict(key = "#account.id", condition = "#account != null and #account.id != null")
  public Account save(Account account) {
    return accountRepository.save(account);
  }

  @Cacheable(key = "#id", unless = "#result == null")
  public Account getById(Long id) {
    return accountRepository.findById(id).orElse(null);
  }

  public Account getByEmail(String email) {
    return accountRepository.findByEmail(email);
  }

}
