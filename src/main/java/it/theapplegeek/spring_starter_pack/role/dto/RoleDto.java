package it.theapplegeek.spring_starter_pack.role.dto;

import it.theapplegeek.spring_starter_pack.permission.dto.PermissionDto;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto implements Serializable {
  private Long id;
  @NotNull private String name;
  private List<PermissionDto> permissions;
}
