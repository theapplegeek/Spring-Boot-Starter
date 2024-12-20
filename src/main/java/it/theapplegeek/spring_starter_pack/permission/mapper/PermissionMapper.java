package it.theapplegeek.spring_starter_pack.permission.mapper;

import it.theapplegeek.spring_starter_pack.common.util.pagination.IMapper;
import it.theapplegeek.spring_starter_pack.permission.dto.PermissionDto;
import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import it.theapplegeek.spring_starter_pack.role.model.RolePermission;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper extends IMapper<PermissionDto, Permission> {
  PermissionDto toDto(Permission permission);

  List<PermissionDto> toDto(List<Permission> permissions);

  @Mapping(target = "id", source = "id.permissionId")
  @Mapping(target = "name", source = "permission.name")
  PermissionDto toDtoFromRolePermission(RolePermission rolePermission);

  List<PermissionDto> toDtoFromRolePermission(List<RolePermission> rolePermissions);

  @Mapping(target = "id", ignore = true)
  Permission toEntity(PermissionDto permissionDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  Permission partialUpdate(PermissionDto permissionDto, @MappingTarget Permission permission);
}
