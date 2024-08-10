package it.theapplegeek.spring_starter_pack.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordEmail {
  private String email;
  private String name;
  private String token;
}
