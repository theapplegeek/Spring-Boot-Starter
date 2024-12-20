package it.theapplegeek.spring_starter_pack.permission.controller;

import it.theapplegeek.spring_starter_pack.permission.dto.PermissionDto;
import it.theapplegeek.spring_starter_pack.permission.service.PermissionService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permission")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {
  private final PermissionService permissionService;

  @GetMapping
  @PreAuthorize("hasAnyAuthority('PERMISSION_READ')")
  public List<PermissionDto> getAllPermissions() {
    return permissionService.getAllPermissions();
  }

  @GetMapping("/role/{roleId}")
  @PreAuthorize("hasAnyAuthority('PERMISSION_READ')")
  public List<PermissionDto> getAllPermissionsByRoleId(@PathVariable("roleId") Long roleId) {
    return permissionService.getAllPermissionsByRoleId(roleId);
  }
}
