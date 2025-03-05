package it.theapplegeek.spring_starter_pack.role.service;

import static org.mockito.Mockito.*;

import it.theapplegeek.spring_starter_pack.role.mapper.RoleMapper;
import it.theapplegeek.spring_starter_pack.role.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
  @Mock RoleRepository roleRepository;
  @Mock RoleMapper roleMapper;
  @InjectMocks RoleService roleService;

  @Test
  void shouldGetAllRoles() {
    // Given
    // When
    roleService.getAllRoles();

    // Then
    verify(roleRepository, times(1)).findAll();
  }
}
