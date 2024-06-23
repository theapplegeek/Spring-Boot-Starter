package it.theapplegeek.spring_starter_pack.role.service;

import java.util.List;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.role.mapper.RoleMapper;
import it.theapplegeek.spring_starter_pack.role.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;

  public List<RoleDto> getAllRoles() {
    return roleMapper.toDto(roleRepository.findAll());
  }
}
