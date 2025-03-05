package it.theapplegeek.spring_starter_pack.common.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
  @NotNull @NotEmpty private String username;
  @NotNull @NotEmpty private String password;
}
