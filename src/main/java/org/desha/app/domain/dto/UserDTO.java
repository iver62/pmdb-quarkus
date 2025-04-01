package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.desha.app.domain.entity.User;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private UUID id;
    private String username;
    private String email;
    private Boolean emailVerified;
    private String name;
    private Long numberOfMovies;

    public static UserDTO fromEntity(User user) {
        return
                UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .emailVerified(user.getEmailVerified())
                        .name(user.getName())
                        .build()
                ;
    }

    public static UserDTO fromEntity(User user, long nbMovies) {
        return
                UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .emailVerified(user.getEmailVerified())
                        .name(user.getName())
                        .numberOfMovies(nbMovies)
                        .build()
                ;
    }
}