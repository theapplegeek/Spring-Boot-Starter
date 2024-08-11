package it.theapplegeek.spring_starter_pack.token.repository;

import it.theapplegeek.spring_starter_pack.token.model.Token;
import it.theapplegeek.spring_starter_pack.token.model.TokenType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Long> {
  Optional<Token> findByToken(String jwt);

  @Modifying
  @Transactional
  @Query("delete from Token t where t.expiration < :expirationDate")
  void deleteAllByExpirationDate(LocalDateTime expirationDate);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(
      "update Token t set t.revoked = true where t.userId = :userId and t.tokenType = :tokenType")
  void revokeAllByUserIdAndType(Long userId, TokenType tokenType);

  Optional<Token> findByTokenAndTokenTypeAndRevokedIsFalse(String token, TokenType tokenType);
}
