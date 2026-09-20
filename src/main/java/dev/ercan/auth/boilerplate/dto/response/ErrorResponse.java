package dev.ercan.auth.boilerplate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import dev.ercan.auth.boilerplate.dto.ValidationErrorDto;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

  private int code;
  private String message;
  private Instant timestamp;
  private List<ValidationErrorDto> details;

  public ErrorResponse(ErrorType error) {
    this.code = error.getCode();
    this.message = error.getMessage();
    this.timestamp = Instant.now();
  }

  public ErrorResponse(ErrorType error, List<ValidationErrorDto> details) {
    this.code = error.getCode();
    this.message = error.getMessage();
    this.timestamp = Instant.now();
    this.details = details;
  }

}