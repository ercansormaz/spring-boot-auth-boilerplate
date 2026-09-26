package dev.ercan.auth.boilerplate.provider.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class AnonymousAuthProviderTest {

  @Test
  void createsAccountWithGeneratedEmailAndDefaultName() {
    AccountService accountService = mock(AccountService.class);
    when(accountService.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
    AnonymousAuthProvider provider = provider(accountService);

    Account account = provider.resolveAccount(null);

    assertNotNull(account);
    assertEquals("Anonymous User", account.getName());
    assertEquals("example.test", account.getEmail().substring(account.getEmail().indexOf('@') + 1));
    verify(accountService).save(any(Account.class));
  }

  @Test
  void retriesAfterUniqueEmailCollision() {
    AccountService accountService = mock(AccountService.class);
    String[] firstEmail = new String[1];
    doAnswer(invocation -> {
      Account account = invocation.getArgument(0);
      firstEmail[0] = account.getEmail();
      throw new DataIntegrityViolationException("duplicate");
    }).doAnswer(invocation -> invocation.getArgument(0)).when(accountService).save(any(Account.class));
    AnonymousAuthProvider provider = provider(accountService);

    Account account = provider.resolveAccount(null);

    assertNotNull(account);
    assertNotNull(firstEmail[0]);
    assertNotNull(account.getEmail());
    org.junit.jupiter.api.Assertions.assertNotEquals(firstEmail[0], account.getEmail());
    verify(accountService, times(2)).save(any(Account.class));
  }

  @Test
  void returnsNullAfterAllGeneratedEmailsCollide() {
    AccountService accountService = mock(AccountService.class);
    doThrow(new DataIntegrityViolationException("duplicate"))
        .when(accountService).save(any(Account.class));
    AnonymousAuthProvider provider = provider(accountService);

    assertNull(provider.resolveAccount(null));
    verify(accountService, times(5)).save(any(Account.class));
  }

  private static AnonymousAuthProvider provider(AccountService accountService) {
    AnonymousAuthProvider provider = new AnonymousAuthProvider(accountService);
    ReflectionTestUtils.setField(provider, "emailDomain", "example.test");
    return provider;
  }
}
