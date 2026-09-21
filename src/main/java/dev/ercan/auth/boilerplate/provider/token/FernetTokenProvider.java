package dev.ercan.auth.boilerplate.provider.token;

import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import dev.ercan.fernet.Fernet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.token.type", havingValue = "fernet")
public class FernetTokenProvider implements TokenProvider {

  private static final String SEPARATOR = ":";
  private static final String BEARER = "Bearer";

  @Value("${auth.token.fernet.secret}")
  private String secret;

  private Fernet fernet;

  @PostConstruct
  public void init() {
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("auth.token.fernet.secret must not be blank");
    }
    fernet = Fernet.of(secret);
  }

  @Override
  public AuthTokenDetail encrypt(AuthTokenDetail authTokenDetail) {
    authTokenDetail.setType(BEARER);

    String message = authTokenDetail.getId() + SEPARATOR
        + authTokenDetail.getAccountId() + SEPARATOR
        + authTokenDetail.getDeviceId() + SEPARATOR
        + authTokenDetail.getSalt() + SEPARATOR
        + authTokenDetail.getScope() + SEPARATOR
        + authTokenDetail.getType() + SEPARATOR
        + authTokenDetail.getExpiresAt().getEpochSecond();

    authTokenDetail.setRaw(fernet.encryptUtf8(message));

    return authTokenDetail;
  }

  @Override
  public AuthTokenDetail decrypt(String raw) {
    return decryptFernet(raw);
  }

  private AuthTokenDetail decryptFernet(String token) {
    try {
      String decrypted = fernet.decryptUtf8(token);

      AuthTokenDetail tokenDetail = parse(decrypted);

      if (tokenDetail.getExpiresAt().isBefore(Instant.now())) {
        return null;
      }

      tokenDetail.setRaw(token);
      return tokenDetail;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private AuthTokenDetail parse(String decodedToken) {
    AuthTokenDetail tokenDetail = new AuthTokenDetail();
    String[] parts = decodedToken.split(SEPARATOR);

    tokenDetail.setId(Long.parseLong(parts[0]));
    tokenDetail.setAccountId(Long.parseLong(parts[1]));
    tokenDetail.setDeviceId(UUID.fromString(parts[2]));
    tokenDetail.setSalt(parts[3]);
    tokenDetail.setScope(Scope.valueOf(parts[4]));
    tokenDetail.setType(parts[5]);

    Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(parts[6]));
    tokenDetail.setExpiresAt(expiresAt);

    return tokenDetail;
  }

}
