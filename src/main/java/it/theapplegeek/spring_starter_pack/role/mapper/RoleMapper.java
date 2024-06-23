package it.theapplegeek.spring_starter_pack.role.mapper;

import java.util.List;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.common.util.pagination.IMapper;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper extends IMapper<RoleDto, Role> {
  RoleDto toDto(Role role);

  List<RoleDto> toDto(List<Role> roles);

  @Mapping(target = "id", ignore = true)
  Role toEntity(RoleDto roleDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  Role partialUpdate(RoleDto roleDto, @MappingTarget Role role);
}
