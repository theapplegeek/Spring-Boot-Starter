package it.theapplegeek.spring_starter_pack.role.controller;

import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.role.service.RoleService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/role")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {
  private final RoleService roleService;

  @GetMapping
  @PreAuthorize("hasAnyAuthority('ROLE_READ')")
  public List<RoleDto> getAllRoles() {
    return roleService.getAllRoles();
  }
}
