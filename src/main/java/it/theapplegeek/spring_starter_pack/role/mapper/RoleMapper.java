package it.theapplegeek.spring_starter_pack.role.mapper;

import it.theapplegeek.spring_starter_pack.common.util.pagination.IMapper;
import it.theapplegeek.spring_starter_pack.permission.mapper.PermissionMapper;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.user.model.UserRole;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {PermissionMapper.class})
public interface RoleMapper extends IMapper<RoleDto, Role> {
  @Mapping(target = "permissions", source = "rolePermissions")
  RoleDto toDto(Role role);

  List<RoleDto> toDto(List<Role> roles);

  @Mapping(target = "id", source = "id.roleId")
  @Mapping(target = "name", source = "role.name")
  @Mapping(target = "permissions", source = "role.rolePermissions")
  RoleDto toDtoFromUserRole(UserRole userRole);

  List<RoleDto> toDtoFromUserRole(List<UserRole> userRoles);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "rolePermissions", ignore = true)
  Role toEntity(RoleDto roleDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "rolePermissions", ignore = true)
  Role partialUpdate(RoleDto roleDto, @MappingTarget Role role);
}
