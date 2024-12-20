package it.theapplegeek.spring_starter_pack.role.repository;

import it.theapplegeek.spring_starter_pack.role.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository
    extends JpaRepository<RolePermission, RolePermission.RolePermissionPK> {}
