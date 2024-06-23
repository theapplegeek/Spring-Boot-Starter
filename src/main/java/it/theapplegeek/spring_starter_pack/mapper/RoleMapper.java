package it.theapplegeek.spring_starter_pack.mapper;

import java.util.List;
import it.theapplegeek.spring_starter_pack.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.model.Role;
import it.theapplegeek.spring_starter_pack.util.pagination.IMapper;
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
