package it.theapplegeek.spring_starter_pack.security.service;

import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoadUserFromUsernameService {
  private final UserRepository userRepository;

  @Transactional
  public User loadUserFromUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Hibernate.initialize(user.getUserRoles());
    user.getUserRoles()
        .forEach(
            userRole -> {
              Hibernate.initialize(userRole.getRole());
              Role role = userRole.getRole();
              Hibernate.initialize(role.getRolePermissions());
              role.getRolePermissions()
                  .forEach(rolePermission -> Hibernate.initialize(rolePermission.getPermission()));
            });

    return user;
  }
}
