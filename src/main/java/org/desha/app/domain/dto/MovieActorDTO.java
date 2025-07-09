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
public class MovieActorDTO extends MovieTechnicianDTO implements Comparable<MovieActorDTO> {

    private Integer rank;

    @Override
    public String toString() {
        return String.format("%s / %s: %s -> %s (%s)", id, person.getId(), person.getName(), role, rank);
    }

    @Override
    public int compareTo(MovieActorDTO o) {
        return rank.compareTo(o.getRank());
    }
}
