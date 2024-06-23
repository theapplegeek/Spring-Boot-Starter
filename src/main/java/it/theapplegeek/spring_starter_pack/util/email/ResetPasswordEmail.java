package it.theapplegeek.spring_starter_pack.util.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.theapplegeek.spring_starter_pack.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordEmail {
  private User user;
  private String token;
}
