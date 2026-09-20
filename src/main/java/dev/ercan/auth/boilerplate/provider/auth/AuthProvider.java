package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;

public interface AuthProvider {

  Account resolveAccount(AbstractAuthRequest request);
  AuthProviderType getAuthProviderType();

}
