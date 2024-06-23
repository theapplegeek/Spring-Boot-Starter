package it.theapplegeek.spring_starter_pack.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
  private String accessToken;
  private String refreshToken;
}
