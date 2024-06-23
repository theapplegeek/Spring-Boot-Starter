package it.theapplegeek.spring_starter_pack.service;

import java.util.List;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.mapper.RoleMapper;
import it.theapplegeek.spring_starter_pack.repository.RoleRepository;
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
