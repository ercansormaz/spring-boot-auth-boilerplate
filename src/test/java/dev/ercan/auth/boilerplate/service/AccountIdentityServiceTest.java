package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.AccountIdentity;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.repository.AccountIdentityRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

class AccountIdentityServiceTest {

  @Test
  void returnsIdentityFoundByProviderAndSubject() {
    AccountIdentityRepository repository = mock(AccountIdentityRepository.class);
    AccountIdentityService service = new AccountIdentityService(repository);
    AccountIdentity identity = new AccountIdentity();
    when(repository.findByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1")).thenReturn(identity);

    assertSame(identity, service.getByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1"));
  }

  @Test
  void savesIdentityWithAccountProviderAndSubject() {
    AccountIdentityRepository repository = mock(AccountIdentityRepository.class);
    AccountIdentityService service = new AccountIdentityService(repository);
    Account account = new Account();

    service.addIdentity(account, AuthProviderType.APPLE, "subject-2");

    ArgumentCaptor<AccountIdentity> identity = ArgumentCaptor.forClass(AccountIdentity.class);
    verify(repository).save(identity.capture());
    assertSame(account, identity.getValue().getAccount());
    org.junit.jupiter.api.Assertions.assertEquals(AuthProviderType.APPLE, identity.getValue().getProvider());
    org.junit.jupiter.api.Assertions.assertEquals("subject-2", identity.getValue().getSubject());
  }

  @Test
  void ignoresDuplicateIdentityConstraintViolation() {
    AccountIdentityRepository repository = mock(AccountIdentityRepository.class);
    AccountIdentityService service = new AccountIdentityService(repository);
    doThrow(new DataIntegrityViolationException("identity already exists"))
        .when(repository).save(org.mockito.ArgumentMatchers.any(AccountIdentity.class));

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> service.addIdentity(new Account(), AuthProviderType.GOOGLE, "subject-1"));

    verify(repository).save(org.mockito.ArgumentMatchers.any(AccountIdentity.class));
  }
}
