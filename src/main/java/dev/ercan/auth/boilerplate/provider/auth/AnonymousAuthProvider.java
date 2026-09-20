package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.util.UUIDv7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.anonymous.enabled", havingValue = "true")
public class AnonymousAuthProvider implements AuthProvider {

  private static final int RETRY_COUNT = 5;
  private static final String DEFAULT_ANONYMOUS_NAME = "Anonymous User";

  @Value("${auth.anonymous.email.domain}")
  private String emailDomain;

  private final AccountService accountService;

  @Override
  public Account resolveAccount(AbstractAuthRequest request) {
    Account account = new Account();
    account.setEmail(createEmail());
    account.setName(DEFAULT_ANONYMOUS_NAME);

    for (int i = 1; i <= RETRY_COUNT; i++) {
      try {
        return accountService.save(account);
      } catch (DataIntegrityViolationException e) {
        account.setEmail(createEmail());
      }
    }

    return null;
  }

  @Override
  public AuthProviderType getAuthProviderType() {
    return AuthProviderType.ANONYMOUS;
  }

  private String createEmail() {
    return UUIDv7.randomUUID() + "@" + emailDomain;
  }

}
