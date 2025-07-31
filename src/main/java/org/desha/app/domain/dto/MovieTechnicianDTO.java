package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieTechnicianDTO {

    protected Long id;
    protected LiteMovieDTO movie;
    protected LitePersonDTO person;
    protected String role;

    public static MovieTechnicianDTO build(Long id, LitePersonDTO personDTO, String role) {
        return
                MovieActorDTO.builder()
                        .id(id)
                        .person(personDTO)
                        .role(role)
                        .build()
                ;
    }

    public String toString() {
        return String.format("%d / %s: %s -> %s", id, person.getId(), person.getName(), role);
    }

}
