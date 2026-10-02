package dev.ercan.auth.boilerplate.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QrApproveRequest {

  @NotEmpty
  private String data;

}
