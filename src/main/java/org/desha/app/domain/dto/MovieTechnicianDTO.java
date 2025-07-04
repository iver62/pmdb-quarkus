package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.MovieTechnician;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieTechnicianDTO {

    protected Long id;
    protected LightMovieDTO movie;
    protected LightPersonDTO person;
    protected String role;

    public static MovieTechnicianDTO of(MovieTechnician movieTechnician) {
        return
                MovieTechnicianDTO.builder()
                        .id(movieTechnician.getId())
                        .person(LightPersonDTO.of(movieTechnician.getPerson()))
                        .role(movieTechnician.getRole())
                        .build()
                ;
    }

    public String toString() {
        return String.format("%d / %s: %s -> %s", id, person.getId(), person.getName(), role);
    }

}
