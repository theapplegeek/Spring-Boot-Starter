package it.theapplegeek.spring_starter_pack.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.theapplegeek.spring_starter_pack.user.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordEmail {
  private User user;
  private String token;
}
