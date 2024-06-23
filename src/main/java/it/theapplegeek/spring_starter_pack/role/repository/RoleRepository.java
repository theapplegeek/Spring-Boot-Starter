package it.theapplegeek.spring_starter_pack.role.repository;

import java.util.Optional;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}