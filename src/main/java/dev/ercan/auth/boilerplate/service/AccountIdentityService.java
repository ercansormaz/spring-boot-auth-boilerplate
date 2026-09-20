package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.AccountIdentity;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.repository.AccountIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountIdentityService {

  private final AccountIdentityRepository accountIdentityRepository;

  public AccountIdentity getByProviderAndSubject(AuthProviderType provider, String subject) {
    return accountIdentityRepository.findByProviderAndSubject(provider, subject);
  }

  public void addIdentity(Account account, AuthProviderType providerType, String subject) {
    AccountIdentity identity = new AccountIdentity();
    identity.setAccount(account);
    identity.setSubject(subject);
    identity.setProvider(providerType);

    try {
      accountIdentityRepository.save(identity);
    } catch (DataIntegrityViolationException ignored) {
      log.info("[ACCOUNT_IDENTITY_SERVICE] [ADD_IDENTITY] [SKIPPED] [ALREADY_EXISTS]");
    }
  }
}
