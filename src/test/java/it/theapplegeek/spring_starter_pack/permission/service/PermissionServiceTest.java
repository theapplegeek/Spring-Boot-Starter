package it.theapplegeek.spring_starter_pack.permission.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.permission.dto.PermissionDto;
import it.theapplegeek.spring_starter_pack.permission.mapper.PermissionMapper;
import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import it.theapplegeek.spring_starter_pack.permission.repository.PermissionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {
  @Mock PermissionRepository permissionRepository;
  @Mock PermissionMapper permissionMapper;
  @InjectMocks PermissionService permissionService;

  private final Faker faker = new Faker();

  private Permission generatePermission() {
    return Permission.builder()
        .id(faker.number().numberBetween(1L, 100L))
        .name(faker.name().name())
        .build();
  }

  private List<PermissionDto> toPermissionDtoList(List<Permission> permissions) {
    return permissions.stream().map(this::toPermissionDto).toList();
  }

  private PermissionDto toPermissionDto(Permission permission) {
    return PermissionDto.builder().id(permission.getId()).name(permission.getName()).build();
  }

  @Test
  void shouldGetAllPermissions() {
    // Given
    List<Permission> permissions = List.of(generatePermission(), generatePermission());
    List<PermissionDto> permissionDtoList = toPermissionDtoList(permissions);
    given(permissionRepository.findAll()).willReturn(permissions);
    given(permissionMapper.toDto(permissions)).willReturn(permissionDtoList);

    // When
    List<PermissionDto> result = permissionService.getAllPermissions();

    // Then
    verify(permissionRepository, times(1)).findAll();
    verify(permissionMapper, times(1)).toDto(permissions);
    assertEquals(permissionDtoList.size(), result.size());
  }

  @Test
  void shouldGetAllPermissionsByRoleId() {
    // Given
    List<Permission> permissions = List.of(generatePermission(), generatePermission());
    List<PermissionDto> permissionDtoList = toPermissionDtoList(permissions);
    given(permissionRepository.findAllByRolePermissions_Id_RoleId(1L)).willReturn(permissions);
    given(permissionMapper.toDto(permissions)).willReturn(permissionDtoList);

    // When
    List<PermissionDto> result = permissionService.getAllPermissionsByRoleId(1L);

    // Then
    verify(permissionRepository, times(1)).findAllByRolePermissions_Id_RoleId(1L);
    verify(permissionMapper, times(1)).toDto(anyList());
    assertEquals(permissionDtoList.size(), result.size());
  }

  @Test
  void shouldNotGetAllPermissionsByRoleId() {
    // Given
    given(permissionRepository.findAllByRolePermissions_Id_RoleId(1000L)).willReturn(List.of());
    given(permissionMapper.toDto(anyList())).willReturn(List.of());

    // When
    List<PermissionDto> permissionDtoList = permissionService.getAllPermissionsByRoleId(1000L);

    // Then
    verify(permissionRepository, times(1)).findAllByRolePermissions_Id_RoleId(1000L);
    verify(permissionMapper, times(1)).toDto(anyList());
    assertEquals(0, permissionDtoList.size());
  }
}
