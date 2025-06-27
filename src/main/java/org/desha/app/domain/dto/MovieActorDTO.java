package org.desha.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.MovieActor;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorDTO extends MovieTechnicianDTO implements Comparable<MovieActorDTO> {

    private Integer rank;

    public static MovieActorDTO of(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.getId())
                        .person(PersonDTO.of(movieActor.getActor()))
                        .role(movieActor.getRole())
                        .rank(movieActor.getRank())
                        .build()
                ;
    }

    @Override
    public String toString() {
        return String.format("%s / %s: %s -> %s (%s)", id, person.getId(), person.getName(), role, rank);
    }

    @Override
    public int compareTo(MovieActorDTO o) {
        return rank.compareTo(o.getRank());
    }
}
