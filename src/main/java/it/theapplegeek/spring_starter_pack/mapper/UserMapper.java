package it.theapplegeek.spring_starter_pack.mapper;

import it.theapplegeek.spring_starter_pack.dto.UserDto;
import it.theapplegeek.spring_starter_pack.model.User;
import it.theapplegeek.spring_starter_pack.util.pagination.IMapper;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {RoleMapper.class})
public interface UserMapper extends IMapper<UserDto, User> {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "roleId", source = "role.id")
  User toEntity(UserDto userDto);

  @Mapping(target = "password", ignore = true)
  UserDto toDto(User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "roleId", source = "role.id")
  User partialUpdate(UserDto userDto, @MappingTarget User user);
}
