package org.desha.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.MovieTechnician;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieTechnicianDTO {

    protected Long id;
    protected LightMovieDTO movie;
    protected PersonDTO person;
    protected String role;

    public static MovieTechnicianDTO of(MovieTechnician movieTechnician) {
        return
                MovieTechnicianDTO.builder()
                        .id(movieTechnician.getId())
                        .person(PersonDTO.of(movieTechnician.getPerson()))
                        .role(movieTechnician.getRole())
                        .build()
                ;
    }

    public String toString() {
        return String.format("%d / %s: %s -> %s", id, person.getId(), person.getName(), role);
    }

}
