package dev.ercan.auth.boilerplate.model.pojo;

import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;

public record OAuthTokenDetail(AuthProviderType provider, String subject, String email, String name,
                               boolean emailVerified) {

}