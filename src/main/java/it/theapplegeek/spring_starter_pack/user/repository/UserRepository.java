package it.theapplegeek.spring_starter_pack.user.repository;

import it.theapplegeek.spring_starter_pack.user.model.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Page<User> findAll(Specification<User> search, Pageable pageable);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmailAndEnabledIsTrue(String email);
}
