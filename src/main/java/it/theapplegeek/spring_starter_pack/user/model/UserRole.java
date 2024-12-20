package it.theapplegeek.spring_starter_pack.user.model;

import it.theapplegeek.spring_starter_pack.role.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "user_role",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_USER_ID_ROLE_ID",
          columnNames = {"user_id", "role_id"})
    })
public class UserRole implements Serializable {
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Embeddable
  public static class UserRolePK {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
  }

  @EmbeddedId private UserRolePK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "FK_USER_ID"),
      insertable = false,
      updatable = false)
  @ToString.Exclude
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "role_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "FK_ROLE_ID"),
      insertable = false,
      updatable = false)
  @ToString.Exclude
  private Role role;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    UserRole userRole = (UserRole) o;
    return getId() != null && Objects.equals(getId(), userRole.getId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id);
  }
}
