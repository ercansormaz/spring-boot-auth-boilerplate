package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.AccountIdentity;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountIdentityRepository extends JpaRepository<AccountIdentity, Long> {

  AccountIdentity findByProviderAndSubject(AuthProviderType provider, String subject);

}
