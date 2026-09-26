package dev.ercan.auth.boilerplate.security.filter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class TokenAuthenticationFilterTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticatesAccountForValidAccessToken() throws ServletException, IOException {
    AccountService accountService = mock(AccountService.class);
    TokenProvider tokenProvider = mock(TokenProvider.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    ExposedFilter filter = new ExposedFilter(accountService, tokenProvider, accessTokenService);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    AuthTokenDetail detail = tokenDetail(Scope.ACCESS);
    AccessToken storedToken = new AccessToken();
    storedToken.setSalt("salt");
    Account account = new Account();
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer raw-token");
    when(tokenProvider.decrypt("raw-token")).thenReturn(detail);
    when(accessTokenService.getById(1L)).thenReturn(storedToken);
    when(accountService.getById(2L)).thenReturn(account);

    filter.apply(request, response, chain);

    UsernamePasswordAuthenticationToken authentication =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    assertSame(account, authentication.getPrincipal());
    assertSame(detail, authentication.getDetails());
    verify(chain).doFilter(request, response);
  }

  @Test
  void continuesUnauthenticatedWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
    AccountService accountService = mock(AccountService.class);
    TokenProvider tokenProvider = mock(TokenProvider.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    ExposedFilter filter = new ExposedFilter(accountService, tokenProvider, accessTokenService);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    filter.apply(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(tokenProvider, never()).decrypt(org.mockito.ArgumentMatchers.any());
    verify(chain).doFilter(request, response);
  }

  private static AuthTokenDetail tokenDetail(Scope scope) {
    AuthTokenDetail detail = new AuthTokenDetail();
    detail.setId(1L);
    detail.setAccountId(2L);
    detail.setDeviceId(UUID.randomUUID());
    detail.setSalt("salt");
    detail.setType("Bearer");
    detail.setScope(scope);
    return detail;
  }

  private static final class ExposedFilter extends TokenAuthenticationFilter {

    private ExposedFilter(
        AccountService accountService, TokenProvider tokenProvider, AccessTokenService accessTokenService) {
      super(accountService, tokenProvider, accessTokenService);
    }

    private void apply(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
      doFilterInternal(request, response, chain);
    }
  }
}
