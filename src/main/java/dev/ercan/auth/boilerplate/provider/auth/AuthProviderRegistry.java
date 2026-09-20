package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.exception.UnsupportedStrategyException;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthProviderRegistry {

  private final Map<AuthProviderType, AuthProvider> strategyMap = new EnumMap<>(AuthProviderType.class);

  public AuthProviderRegistry(List<AuthProvider> strategyList) {
    strategyList.forEach(strategy -> strategyMap.put(strategy.getAuthProviderType(), strategy));
  }

  public AuthProvider getAuthProvider(AuthProviderType authProviderType) {
    return Optional.ofNullable(strategyMap.get(authProviderType)).orElseThrow(
        () -> new UnsupportedStrategyException(ErrorType.UNSUPPORTED_STRATEGY, AuthProvider.class, authProviderType));
  }

}
