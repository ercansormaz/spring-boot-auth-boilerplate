package dev.ercan.auth.boilerplate.provider.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
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
@ConditionalOnProperty(name = "auth.token.type", havingValue = "jwt")
public class JwtTokenProvider implements TokenProvider {

  private static final String SCOPE_CLAIM = "scope";
  private static final String TYPE_CLAIM = "type";
  private static final String SALT_CLAIM = "salt";
  private static final String DEVICE_CLAIM = "device";
  private static final String BEARER = "Bearer";

  @Value("${auth.token.jwt.secret}")
  private String secret;

  @Value("${auth.token.jwt.issuer}")
  private String issuer;

  private Algorithm algorithm;

  @PostConstruct
  public void init() {
    if (!StringUtils.hasText(secret)) {
      throw new IllegalStateException("auth.token.jwt.secret must not be blank");
    }
    algorithm = Algorithm.HMAC256(secret);
  }

  @Override
  public AuthTokenDetail encrypt(AuthTokenDetail authTokenDetail) {
    authTokenDetail.setType(BEARER);

    String token = JWT.create()
        .withJWTId(String.valueOf(authTokenDetail.getId()))
        .withSubject(String.valueOf(authTokenDetail.getAccountId()))
        .withIssuer(issuer)
        .withClaim(DEVICE_CLAIM, authTokenDetail.getDeviceId().toString())
        .withClaim(SCOPE_CLAIM, authTokenDetail.getScope().name())
        .withClaim(TYPE_CLAIM, authTokenDetail.getType())
        .withClaim(SALT_CLAIM, authTokenDetail.getSalt())
        .withIssuedAt(Instant.now())
        .withExpiresAt(authTokenDetail.getExpiresAt())
        .sign(algorithm);

    authTokenDetail.setRaw(token);

    return authTokenDetail;
  }

  @Override
  public AuthTokenDetail decrypt(String raw) {
    JWTVerifier verifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build();

    DecodedJWT decodedJWT;
    try {
      decodedJWT = verifier.verify(raw);
    } catch (Exception ex) {
      return null;
    }

    return parse(decodedJWT);
  }

  private AuthTokenDetail parse(DecodedJWT decodedToken) {
    AuthTokenDetail tokenDetail = new AuthTokenDetail();
    tokenDetail.setId(Long.parseLong(decodedToken.getId()));
    tokenDetail.setAccountId(Long.parseLong(decodedToken.getSubject()));
    tokenDetail.setDeviceId(UUID.fromString(decodedToken.getClaim(DEVICE_CLAIM).asString()));
    tokenDetail.setSalt(decodedToken.getClaim(SALT_CLAIM).asString());
    tokenDetail.setScope(Scope.valueOf(decodedToken.getClaim(SCOPE_CLAIM).asString()));
    tokenDetail.setType(decodedToken.getClaim(TYPE_CLAIM).asString());
    tokenDetail.setExpiresAt(decodedToken.getExpiresAtAsInstant());
    return tokenDetail;
  }

}
