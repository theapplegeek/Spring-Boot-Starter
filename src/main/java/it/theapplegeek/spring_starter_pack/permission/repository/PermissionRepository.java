package it.theapplegeek.spring_starter_pack.permission.repository;

import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  List<Permission> findAllByRolePermissions_Id_RoleId(Long roleId);
}
