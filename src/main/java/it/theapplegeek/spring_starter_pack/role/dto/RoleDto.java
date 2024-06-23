package it.theapplegeek.spring_starter_pack.role.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto implements Serializable {
  private Long id;
  @NotNull private String name;
}
