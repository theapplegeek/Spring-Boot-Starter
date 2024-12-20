package it.theapplegeek.spring_starter_pack.security.model;

import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.theapplegeek.spring_starter_pack.role.model.Role;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogged {
  private Long id;
  private String email;
  private String name;
  private String surname;
  private String username;
  private List<Role> roles;
  private List<Permission> permissions;
}
