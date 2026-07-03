package com.aruclinic.mapper;

import com.aruclinic.dto.UserDto;
import com.aruclinic.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface UserMapper {

    @Mapping(target = "confirmPassword", ignore = true)
    @Mapping(target = "role", expression = "java(getPrimaryRole(user))")
    UserDto toUserDto(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserDto userDto);

    default String getPrimaryRole(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return null;
        }
        return user.getRoles().iterator().next().getName();
    }
}