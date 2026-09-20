package dev.ercan.auth.boilerplate.model.pojo;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;

public record IssuedTokens(AccessToken accessToken, RefreshToken refreshToken) {

}
