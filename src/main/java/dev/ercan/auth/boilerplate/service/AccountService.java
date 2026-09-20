package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;

  public Account save(Account account) {
    return accountRepository.save(account);
  }

  public Account getById(Long id) {
    return accountRepository.findById(id).orElse(null);
  }

}
