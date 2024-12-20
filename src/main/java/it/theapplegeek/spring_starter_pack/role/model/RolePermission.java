package it.theapplegeek.spring_starter_pack.role.model;

import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "role_permission",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_ROLE_ID_PERMISSION_ID",
          columnNames = {"role_id", "permission_id"})
    })
public class RolePermission {
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Embeddable
  public static class RolePermissionPK implements Serializable {
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
  }

  @EmbeddedId private RolePermissionPK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "role_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "FK_ROLE_ID"),
      insertable = false,
      updatable = false)
  @ToString.Exclude
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "permission_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "FK_PERMISSION_ID"),
      insertable = false,
      updatable = false)
  @ToString.Exclude
  private Permission permission;

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
    RolePermission that = (RolePermission) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id);
  }
}
