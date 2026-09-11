package com.bento.crm.identity.mapper;

import com.bento.crm.identity.dto.UserResponseDto;
import com.bento.crm.identity.model.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "initials", expression = "java(deriveInitials(user))")
    @Mapping(target = "lastActiveAt", expression = "java(user.getLastActiveAt() != null ? user.getLastActiveAt().toString() : null)")
    UserResponseDto toResponseDto(AppUser user);

    /**
     * The {@code initials} column is write-never (nothing ever persists it),
     * so it is null for virtually every row. Deriving here -- at the single
     * choke point every user endpoint (/auth/me, login, /users) maps through
     * -- guarantees the API never emits a null users.initials again instead of
     * pushing null-tolerance onto every client.
     */
    default String deriveInitials(AppUser user) {
        if (user.getInitials() != null && !user.getInitials().isBlank()) {
            return user.getInitials();
        }
        String name = user.getDisplayName() != null ? user.getDisplayName().trim() : "";
        if (name.isEmpty()) {
            return "U";
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
