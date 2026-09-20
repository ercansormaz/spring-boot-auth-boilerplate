package dev.ercan.auth.boilerplate.security.handler;

import dev.ercan.auth.boilerplate.dto.response.ErrorResponse;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;

@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final JsonMapper jsonMapper;

  @Override
  public void handle(@NonNull HttpServletRequest request, HttpServletResponse response,
      @NonNull AccessDeniedException ex) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse = new ErrorResponse(ErrorType.INSUFFICIENT_AUTHORITY);

    response.getWriter().write(jsonMapper.writeValueAsString(errorResponse));
  }
}