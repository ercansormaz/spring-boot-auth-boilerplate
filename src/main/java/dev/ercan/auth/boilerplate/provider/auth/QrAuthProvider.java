package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.QrAuthRequest;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.qr.enabled", havingValue = "true")
public class QrAuthProvider implements AuthProvider {

  @Override
  public Account resolveAccount(AbstractAuthRequest request) {
    QrAuthRequest qrAuthRequest = (QrAuthRequest) request;
    return qrAuthRequest.getAccount();
  }

  @Override
  public AuthProviderType getAuthProviderType() {
    return AuthProviderType.QR;
  }
}
