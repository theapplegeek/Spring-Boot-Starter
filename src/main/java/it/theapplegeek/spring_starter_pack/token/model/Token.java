package it.theapplegeek.spring_starter_pack.token.model;

import it.theapplegeek.spring_starter_pack.user.model.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "token",
    uniqueConstraints =
        @UniqueConstraint(
            name = "UK_TOKEN",
            columnNames = {"token"}))
public class Token implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token", length = 10240, unique = true, nullable = false)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(name = "token_type", nullable = false)
  @Builder.Default
  private TokenType tokenType = TokenType.BEARER;

  @Column(name = "revoked", nullable = false)
  @Builder.Default
  private Boolean revoked = false;

  @Column(name = "expiration", nullable = false)
  private LocalDateTime expiration;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(
      name = "user_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "FK_USER_ID"),
      insertable = false,
      updatable = false)
  @ToString.Exclude
  private User user;

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
    Token token = (Token) o;
    return getId() != null && Objects.equals(getId(), token.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
