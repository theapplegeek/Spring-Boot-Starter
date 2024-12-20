package it.theapplegeek.spring_starter_pack.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionDto implements Serializable {
  private Long id;
  @NotNull private String name;
}
