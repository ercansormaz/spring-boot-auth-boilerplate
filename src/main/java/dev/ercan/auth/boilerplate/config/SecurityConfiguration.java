package dev.ercan.auth.boilerplate.config;

import dev.ercan.auth.boilerplate.constant.ApiEndpoints;
import dev.ercan.auth.boilerplate.security.filter.TokenAuthenticationFilter;
import dev.ercan.auth.boilerplate.security.handler.CustomAccessDeniedHandler;
import dev.ercan.auth.boilerplate.security.handler.CustomAuthenticationEntryPoint;
import dev.ercan.auth.boilerplate.service.AccessTokenService;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfiguration {

  private final JsonMapper jsonMapper;
  private final AccountService accountService;
  private final TokenProvider tokenProvider;
  private final AccessTokenService accessTokenService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new TokenAuthenticationFilter(accountService, tokenProvider, accessTokenService), UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(a -> a.requestMatchers(ApiEndpoints.ERROR).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, ApiEndpoints.ANONYMOUS_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, ApiEndpoints.REFRESH_TOKEN_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, ApiEndpoints.GOOGLE_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, ApiEndpoints.APPLE_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.PUT, ApiEndpoints.EMAIL_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, ApiEndpoints.EMAIL_AUTH).permitAll())
        .authorizeHttpRequests(a -> a.anyRequest().authenticated())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint(jsonMapper)))
        .exceptionHandling(ex -> ex.accessDeniedHandler(new CustomAccessDeniedHandler(jsonMapper)));

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> {
      throw new UsernameNotFoundException("Default user disabled");
    };
  }

}
