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

    public String toString() {
        return String.format("%d / %s: %s -> %s", id, person.getId(), person.getName(), role);
    }

}
