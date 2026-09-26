package dev.ercan.auth.boilerplate.provider.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.request.EmailAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.facade.EmailOtpFacade;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.service.AccountService;
import org.junit.jupiter.api.Test;

class EmailAuthProviderTest {

  @Test
  void validatesOtpWithNormalizedEmailAndReturnsExistingAccount() {
    AccountService accountService = mock(AccountService.class);
    EmailOtpFacade emailOtpFacade = mock(EmailOtpFacade.class);
    EmailAuthProvider provider = new EmailAuthProvider(accountService, emailOtpFacade);
    EmailAuthRequest request = request("  User@Example.COM  ");
    Account account = new Account();
    when(emailOtpFacade.validate(request.getOtp(), OtpFlowType.AUTHENTICATION, "user@example.com"))
        .thenReturn(true);
    when(accountService.getByEmail("user@example.com")).thenReturn(account);

    assertSame(account, provider.resolveAccount(request));

    verify(accountService).getByEmail("user@example.com");
    verify(accountService, never()).save(any(Account.class));
  }

  @Test
  void rejectsRequestWhenOtpIsInvalid() {
    AccountService accountService = mock(AccountService.class);
    EmailOtpFacade emailOtpFacade = mock(EmailOtpFacade.class);
    EmailAuthProvider provider = new EmailAuthProvider(accountService, emailOtpFacade);
    EmailAuthRequest request = request("user@example.com");
    when(emailOtpFacade.validate(request.getOtp(), OtpFlowType.AUTHENTICATION, "user@example.com"))
        .thenReturn(false);

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> provider.resolveAccount(request));

    assertEquals(ErrorType.INVALID_OTP, exception.getErrorType());
    verify(accountService, never()).getByEmail(any());
  }

  private static EmailAuthRequest request(String email) {
    EmailAuthRequest request = new EmailAuthRequest();
    request.setEmail(email);
    request.setOtp(new OtpRequest());
    return request;
  }
}
