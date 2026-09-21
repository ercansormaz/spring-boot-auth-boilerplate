package dev.ercan.auth.boilerplate.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAuthRequest extends AbstractAuthRequest {

  @Email
  @NotBlank
  private String email;

  @Valid
  @NotNull
  private OtpRequest otp;

}