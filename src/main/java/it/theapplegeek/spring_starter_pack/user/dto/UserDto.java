package it.theapplegeek.spring_starter_pack.user.dto;

import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.model.UserRole;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto implements Serializable, Specification<User> {
  private Long id;

  @NotNull
  @Size(min = 1, max = 255)
  private String username;

  @NotNull
  @Size(min = 8, max = 255)
  private String password;

  @NotNull
  @Size(min = 1, max = 255)
  private String name;

  @NotNull
  @Size(min = 1, max = 255)
  private String surname;

  @NotNull
  @Email
  @Size(min = 1, max = 255)
  private String email;

  @Builder.Default private Boolean enabled = true;

  @NotNull @NotEmpty private List<RoleDto> roles;

  private boolean allRolesHaveId() {
    return roles.stream().allMatch(role -> role.getId() != null);
  }

  @Override
  public Predicate toPredicate(
      @NonNull Root<User> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
    ArrayList<Predicate> predicates = new ArrayList<>();
    if (username != null) {
      predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
    }
    if (email != null) {
      predicates.add(criteriaBuilder.like(root.get("email"), "%" + email + "%"));
    }
    if (name != null) {
      predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
    }
    if (surname != null) {
      predicates.add(criteriaBuilder.like(root.get("surname"), "%" + surname + "%"));
    }
    if (roles != null && !roles.isEmpty() && allRolesHaveId()) {
      Join<User, UserRole> userRoleJoin = root.join("userRoles");
      CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(userRoleJoin.get("id").get("roleId"));
      roles.forEach(role -> inClause.value(role.getId()));
      predicates.add(inClause);
    }
    return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
