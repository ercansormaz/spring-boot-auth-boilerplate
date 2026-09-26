package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.repository.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

  @Test
  void savesAccountThroughRepository() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountService service = new AccountService(repository);
    Account account = new Account();
    Account savedAccount = new Account();
    when(repository.save(account)).thenReturn(savedAccount);

    assertSame(savedAccount, service.save(account));

    verify(repository).save(account);
  }

  @Test
  void returnsAccountWhenIdExists() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountService service = new AccountService(repository);
    Account account = new Account();
    when(repository.findById(42L)).thenReturn(Optional.of(account));

    assertSame(account, service.getById(42L));
  }

  @Test
  void returnsNullWhenIdDoesNotExist() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountService service = new AccountService(repository);
    when(repository.findById(42L)).thenReturn(Optional.empty());

    assertNull(service.getById(42L));
  }

  @Test
  void returnsAccountFoundByEmail() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountService service = new AccountService(repository);
    Account account = new Account();
    when(repository.findByEmail("user@example.com")).thenReturn(account);

    assertSame(account, service.getByEmail("user@example.com"));
  }
}
