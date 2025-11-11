package it.alf.springsecurity.mapper;

import it.alf.springsecurity.domain.User;
import it.alf.springsecurity.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.stream.Collectors;

/**
 * MapStruct mapper for User <-> UserDto
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(r->r.getName()).collect(java.util.stream.Collectors.toSet()))")
    UserDto toDto(User user);
}
