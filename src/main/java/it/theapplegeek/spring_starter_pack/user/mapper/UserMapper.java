package it.theapplegeek.spring_starter_pack.user.mapper;

import it.theapplegeek.spring_starter_pack.common.util.pagination.IMapper;
import it.theapplegeek.spring_starter_pack.role.mapper.RoleMapper;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.model.User;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {RoleMapper.class})
public interface UserMapper extends IMapper<UserDto, User> {
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "roles", source = "userRoles")
  UserDto toDto(User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userRoles", ignore = true)
  User toEntity(UserDto userDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "userRoles", ignore = true)
  User partialUpdate(UserDto userDto, @MappingTarget User user);
}
