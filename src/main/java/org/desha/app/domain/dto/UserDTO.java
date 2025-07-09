package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String lastname;
    private String firstname;
    private Boolean emailVerified;
    private Integer numberOfMovies;

    public static UserDTO build(UUID id, String username, String email, Boolean emailVerified, String lastname, String firstname, Integer numberOfMovies) {
        return
                UserDTO.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .emailVerified(emailVerified)
                        .lastname(lastname)
                        .firstname(firstname)
                        .numberOfMovies(numberOfMovies)
                        .build()
                ;
    }
}