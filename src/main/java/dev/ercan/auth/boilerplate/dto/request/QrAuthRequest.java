package dev.ercan.auth.boilerplate.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.ercan.auth.boilerplate.model.entity.Account;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QrAuthRequest extends AbstractAuthRequest {

  @NotEmpty
  private String id;

  @JsonIgnore
  private Account account;

}