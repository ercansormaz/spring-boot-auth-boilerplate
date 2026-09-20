package dev.ercan.auth.boilerplate.provider.auth;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.GoogleAuthRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.OAuthTokenDetail;
import dev.ercan.auth.boilerplate.service.AccountIdentityService;
import dev.ercan.auth.boilerplate.service.AccountService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "auth.google.enabled", havingValue = "true")
public class GoogleAuthProvider extends AbstractOAuthProvider {

  @Value("${auth.google.client-id}")
  private String clientId;

  @Value("${auth.google.issuers}")
  private String[] issuers;

  @Value("${auth.google.jwk-url}")
  private String jwkUrl;

  private JwkProvider jwkProvider;

  public GoogleAuthProvider(AccountService accountService, AccountIdentityService accountIdentityService) {
    super(accountService, accountIdentityService);
  }

  @PostConstruct
  public void init() throws URISyntaxException, MalformedURLException {
    URL jwksUrl = new URI(jwkUrl).toURL();
    this.jwkProvider = new JwkProviderBuilder(jwksUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .build();
  }

  @Override
  public Account resolveAccount(AbstractAuthRequest request) {
    GoogleAuthRequest googleAuthRequest = (GoogleAuthRequest) request;
    OAuthTokenDetail oauthTokenDetail = getOauthDetail(googleAuthRequest.getToken());
    return resolveOrCreateAccount(oauthTokenDetail);
  }

  @Override
  public AuthProviderType getAuthProviderType() {
    return AuthProviderType.GOOGLE;
  }

  @Override
  String getClientId() {
    return clientId;
  }

  @Override
  String[] getIssuers() {
    return issuers;
  }

  @Override
  JwkProvider getJwkProvider() {
    return jwkProvider;
  }

  @Override
  ErrorType getErrorType() {
    return ErrorType.INVALID_GOOGLE_TOKEN;
  }

}
