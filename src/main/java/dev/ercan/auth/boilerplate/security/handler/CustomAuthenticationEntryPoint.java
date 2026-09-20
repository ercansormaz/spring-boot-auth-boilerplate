package dev.ercan.auth.boilerplate.security.handler;

import dev.ercan.auth.boilerplate.dto.response.ErrorResponse;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final JsonMapper jsonMapper;

  @Override
  public void commence(@NonNull HttpServletRequest request, HttpServletResponse response,
      @NonNull AuthenticationException authException) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse = new ErrorResponse(ErrorType.INVALID_OR_EXPIRED_TOKEN);

    response.getWriter().write(jsonMapper.writeValueAsString(errorResponse));
  }
}