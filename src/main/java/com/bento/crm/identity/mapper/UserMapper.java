package com.bento.crm.identity.mapper;

import com.bento.crm.identity.dto.UserResponseDto;
import com.bento.crm.identity.model.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "lastActiveAt", expression = "java(user.getLastActiveAt() != null ? user.getLastActiveAt().toString() : null)")
    UserResponseDto toResponseDto(AppUser user);
}
