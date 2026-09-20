package dev.ercan.auth.boilerplate.provider.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.AccountIdentity;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.OAuthTokenDetail;
import dev.ercan.auth.boilerplate.service.AccountIdentityService;
import dev.ercan.auth.boilerplate.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class AbstractOAuthProvider implements AuthProvider {

  private static final String NAME_CLAIM = "name";
  private static final String EMAIL_CLAIM = "email";
  private static final String EMAIL_VERIFIED_CLAIM = "email_verified";

  private final AccountService accountService;
  private final AccountIdentityService accountIdentityService;

  abstract String getClientId();
  abstract String[] getIssuers();
  abstract JwkProvider getJwkProvider();
  abstract ErrorType getErrorType();

  protected Account resolveOrCreateAccount(OAuthTokenDetail oAuthTokenDetail) {
    if (Objects.isNull(oAuthTokenDetail)) {
      throw new InvalidCredentialsException(getErrorType());
    }

    AccountIdentity accountIdentity = accountIdentityService.getByProviderAndSubject(getAuthProviderType(), oAuthTokenDetail.subject());

    if (Objects.nonNull(accountIdentity)) {
      return accountIdentity.getAccount();
    }

    if (!StringUtils.hasText(oAuthTokenDetail.email()) || !oAuthTokenDetail.emailVerified()) {
      throw new InvalidCredentialsException(getErrorType());
    }

    Account account = accountService.getByEmail(oAuthTokenDetail.email());

    if (Objects.nonNull(account)) {
      accountIdentityService.addIdentity(account, getAuthProviderType(), oAuthTokenDetail.subject());
      return account;
    }

    account = new Account();
    account.setEmail(oAuthTokenDetail.email());
    account.setName(oAuthTokenDetail.name());

    try {
      account = accountService.save(account);
    } catch (DataIntegrityViolationException e) {
      account = accountService.getByEmail(oAuthTokenDetail.email());
    }

    accountIdentityService.addIdentity(account, getAuthProviderType(), oAuthTokenDetail.subject());

    return account;
  }

  protected OAuthTokenDetail getOauthDetail(String token) {
    try {
      DecodedJWT jwt = JWT.decode(token);

      Jwk jwk = getJwkProvider().get(jwt.getKeyId());
      RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
      Algorithm algorithm = Algorithm.RSA256(publicKey, null);

      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(getIssuers())
          .withAnyOfAudience(getClientId())
          .build();

      verifier.verify(token);

      String name = jwt.getClaim(NAME_CLAIM).asString();
      String email = jwt.getClaim(EMAIL_CLAIM).asString();
      boolean isEmailVerified = jwt.getClaim(EMAIL_VERIFIED_CLAIM).asBoolean();

      return new OAuthTokenDetail(getAuthProviderType(), jwt.getSubject(),
          StringUtils.hasText(email) ? email.toLowerCase() : null, name, isEmailVerified);
    } catch (Exception e) {
      return null;
    }
  }
}
