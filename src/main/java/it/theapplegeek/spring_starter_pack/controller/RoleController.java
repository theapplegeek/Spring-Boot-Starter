package it.theapplegeek.spring_starter_pack.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.service.RoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/role")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {
  private final RoleService roleService;

  @GetMapping
  public List<RoleDto> getAllRoles() {
    return roleService.getAllRoles();
  }
}
