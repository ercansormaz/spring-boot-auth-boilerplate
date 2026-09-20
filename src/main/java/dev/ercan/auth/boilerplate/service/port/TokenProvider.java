package dev.ercan.auth.boilerplate.service.port;

import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;

public interface TokenProvider {

  AuthTokenDetail encrypt(AuthTokenDetail authTokenDetail);
  AuthTokenDetail decrypt(String raw);

}
