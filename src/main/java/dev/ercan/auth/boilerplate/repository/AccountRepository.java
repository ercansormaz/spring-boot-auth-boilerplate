package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
