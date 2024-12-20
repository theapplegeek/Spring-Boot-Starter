package it.theapplegeek.spring_starter_pack.user.repository;

import it.theapplegeek.spring_starter_pack.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRolePK> {}
