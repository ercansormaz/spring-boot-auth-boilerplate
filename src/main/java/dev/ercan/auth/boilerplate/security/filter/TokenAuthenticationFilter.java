package dev.ercan.auth.boilerplate.security.filter;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import dev.ercan.auth.boilerplate.service.AccessTokenService;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final AccountService accountService;
  private final TokenProvider tokenProvider;
  private final AccessTokenService accessTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (!StringUtils.hasText(authorization)) {
      filterChain.doFilter(request, response);
      return;
    }

    String[] parts = authorization.trim().split("\\s+");

    if (parts.length != 2) {
      log.warn("[TOKEN_AUTHENTICATION_FILTER] [DO_FILTER_INTERNAL] [TOKEN_FORMAT_NOT_MATCH] [SIZE={}]", parts.length);
      filterChain.doFilter(request, response);
      return;
    }

    AuthTokenDetail authTokenDetail = tokenProvider.decrypt(parts[1]);

    if (Objects.isNull(authTokenDetail)) {
      log.warn("[TOKEN_AUTHENTICATION_FILTER] [DO_FILTER_INTERNAL] [TOKEN_CANNOT_VALIDATED]");
      filterChain.doFilter(request, response);
      return;
    }

    if (!parts[0].equalsIgnoreCase(authTokenDetail.getType())) {
      log.warn("[TOKEN_AUTHENTICATION_FILTER] [DO_FILTER_INTERNAL] [TOKEN_TYPE_NOT_MATCH]");
      filterChain.doFilter(request, response);
      return;
    }

    if (!Scope.ACCESS.equals(authTokenDetail.getScope())) {
      log.warn("[TOKEN_AUTHENTICATION_FILTER] [DO_FILTER_INTERNAL] [TOKEN_IS_NOT_ACCESS_TOKEN]");
      filterChain.doFilter(request, response);
      return;
    }

    AccessToken accessToken = accessTokenService.getById(authTokenDetail.getId());
    if (Objects.isNull(accessToken) || !authTokenDetail.getSalt().equals(accessToken.getSalt())) {
      log.warn("[TOKEN_AUTHENTICATION_FILTER] [DO_FILTER_INTERNAL] [TOKEN_REVOKED]");
      filterChain.doFilter(request, response);
      return;
    }

    Account account = accountService.getById(authTokenDetail.getAccountId());
    if (Objects.nonNull(account)) {
      List<SimpleGrantedAuthority> roles = Collections.emptyList();

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(account, null, roles);
      authentication.setDetails(authTokenDetail);

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}