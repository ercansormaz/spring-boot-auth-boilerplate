package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.EmailAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.facade.EmailOtpFacade;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.email.enabled", havingValue = "true")
public class EmailAuthProvider implements AuthProvider {

  private final AccountService accountService;
  private final EmailOtpFacade emailOtpFacade;

  @Override
  public Account resolveAccount(AbstractAuthRequest request) {
    EmailAuthRequest emailAuthRequest = (EmailAuthRequest) request;

    String email = EmailNormalizer.normalize(emailAuthRequest.getEmail());
    OtpRequest otpRequest = emailAuthRequest.getOtp();

    if (!emailOtpFacade.validate(otpRequest, OtpFlowType.AUTHENTICATION, email)) {
      throw new InvalidCredentialsException(ErrorType.INVALID_OTP);
    }

    Account account = accountService.getByEmail(email);

    if (Objects.nonNull(account)) {
      return account;
    }

    account = new Account();
    account.setEmail(email);

    try {
      account = accountService.save(account);
    } catch (DataIntegrityViolationException e) {
      account = accountService.getByEmail(email);
    }

    return account;
  }

  @Override
  public AuthProviderType getAuthProviderType() {
    return AuthProviderType.EMAIL;
  }

}
