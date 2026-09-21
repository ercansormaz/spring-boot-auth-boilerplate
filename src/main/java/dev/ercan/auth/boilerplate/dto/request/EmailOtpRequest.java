package dev.ercan.auth.boilerplate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailOtpRequest {

  @Email
  @NotBlank
  private String email;

}
