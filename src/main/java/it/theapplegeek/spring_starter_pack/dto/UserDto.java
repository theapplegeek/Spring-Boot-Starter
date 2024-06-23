package it.theapplegeek.spring_starter_pack.dto;

import it.theapplegeek.spring_starter_pack.model.User;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;

/** DTO for {@link User} */
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

  private Boolean enabled;

  private RoleDto role;

  @Override
  public Predicate toPredicate(
      @NonNull Root<User> root,
      @NonNull CriteriaQuery<?> query,
      @NonNull CriteriaBuilder criteriaBuilder) {
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
    if (role != null && role.getId() != null) {
      predicates.add(criteriaBuilder.equal(root.get("roleId"), role.getId()));
    }
    return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
