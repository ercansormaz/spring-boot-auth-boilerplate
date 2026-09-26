package dev.ercan.auth.boilerplate.provider.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwk.JwkProvider;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.AccountIdentity;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.OAuthTokenDetail;
import dev.ercan.auth.boilerplate.service.AccountIdentityService;
import dev.ercan.auth.boilerplate.service.AccountService;
import org.junit.jupiter.api.Test;

class AbstractOAuthProviderTest {

  @Test
  void returnsAccountForExistingProviderIdentity() {
    AccountService accountService = mock(AccountService.class);
    AccountIdentityService identityService = mock(AccountIdentityService.class);
    TestOAuthProvider provider = new TestOAuthProvider(accountService, identityService);
    Account account = new Account();
    AccountIdentity identity = new AccountIdentity();
    identity.setAccount(account);
    when(identityService.getByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1"))
        .thenReturn(identity);

    assertSame(account, provider.resolveOrCreateAccount(
        new OAuthTokenDetail(AuthProviderType.GOOGLE, "subject-1", null, null, false)));

    verify(accountService, never()).getByEmail(org.mockito.ArgumentMatchers.any());
    verify(identityService, never()).addIdentity(
        org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejectsUnverifiedEmailWhenIdentityDoesNotExist() {
    AccountService accountService = mock(AccountService.class);
    AccountIdentityService identityService = mock(AccountIdentityService.class);
    TestOAuthProvider provider = new TestOAuthProvider(accountService, identityService);
    when(identityService.getByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1")).thenReturn(null);

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> provider.resolveOrCreateAccount(
            new OAuthTokenDetail(AuthProviderType.GOOGLE, "subject-1", "user@example.com", "User", false)));

    assertEquals(ErrorType.INVALID_GOOGLE_TOKEN, exception.getErrorType());
    verify(accountService, never()).getByEmail(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void linksNewProviderIdentityToExistingEmailAccount() {
    AccountService accountService = mock(AccountService.class);
    AccountIdentityService identityService = mock(AccountIdentityService.class);
    TestOAuthProvider provider = new TestOAuthProvider(accountService, identityService);
    Account account = new Account();
    when(identityService.getByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1")).thenReturn(null);
    when(accountService.getByEmail("user@example.com")).thenReturn(account);

    assertSame(account, provider.resolveOrCreateAccount(
        new OAuthTokenDetail(AuthProviderType.GOOGLE, "subject-1", "user@example.com", "User", true)));

    verify(identityService).addIdentity(account, AuthProviderType.GOOGLE, "subject-1");
    verify(accountService, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createsAccountAndIdentityWhenEmailIsNew() {
    AccountService accountService = mock(AccountService.class);
    AccountIdentityService identityService = mock(AccountIdentityService.class);
    TestOAuthProvider provider = new TestOAuthProvider(accountService, identityService);
    Account savedAccount = new Account();
    when(identityService.getByProviderAndSubject(AuthProviderType.GOOGLE, "subject-1")).thenReturn(null);
    when(accountService.getByEmail("user@example.com")).thenReturn(null);
    when(accountService.save(org.mockito.ArgumentMatchers.any(Account.class))).thenReturn(savedAccount);

    assertSame(savedAccount, provider.resolveOrCreateAccount(
        new OAuthTokenDetail(AuthProviderType.GOOGLE, "subject-1", "user@example.com", "User", true)));

    verify(identityService).addIdentity(savedAccount, AuthProviderType.GOOGLE, "subject-1");
  }

  private static final class TestOAuthProvider extends AbstractOAuthProvider {

    private TestOAuthProvider(AccountService accountService, AccountIdentityService identityService) {
      super(accountService, identityService);
    }

    @Override
    String getClientId() {
      return "client-id";
    }

    @Override
    String[] getIssuers() {
      return new String[] {"https://issuer.example"};
    }

    @Override
    JwkProvider getJwkProvider() {
      return null;
    }

    @Override
    ErrorType getErrorType() {
      return ErrorType.INVALID_GOOGLE_TOKEN;
    }

    @Override
    public AuthProviderType getAuthProviderType() {
      return AuthProviderType.GOOGLE;
    }

    @Override
    public Account resolveAccount(dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest request) {
      throw new UnsupportedOperationException();
    }
  }
}
