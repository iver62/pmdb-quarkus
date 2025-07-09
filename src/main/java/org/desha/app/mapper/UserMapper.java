package org.desha.app.mapper;

import org.desha.app.domain.dto.UserDTO;
import org.desha.app.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Named("userMapper")
@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "numberOfMovies", source = "numberOfMovies")
    UserDTO userToUserDTO(User entity);

    @Named("toLiteUserDTO")
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "numberOfMovies", ignore = true)
    UserDTO toLiteUserDTO(User entity);

    List<UserDTO> toDTOList(List<User> userList);

    Set<UserDTO> toDTOSet(Set<User> userSet);
}
