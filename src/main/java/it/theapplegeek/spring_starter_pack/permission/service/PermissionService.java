package it.theapplegeek.spring_starter_pack.permission.service;

import it.theapplegeek.spring_starter_pack.permission.dto.PermissionDto;
import it.theapplegeek.spring_starter_pack.permission.mapper.PermissionMapper;
import it.theapplegeek.spring_starter_pack.permission.repository.PermissionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PermissionService {
  private final PermissionRepository permissionRepository;
  private final PermissionMapper permissionMapper;

  public List<PermissionDto> getAllPermissions() {
    return permissionMapper.toDto(permissionRepository.findAll());
  }

  public List<PermissionDto> getAllPermissionsByRoleId(Long roleId) {
    return permissionMapper.toDto(permissionRepository.findAllByRolePermissions_Id_RoleId(roleId));
  }
}
