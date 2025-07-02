package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.MovieActor;

import java.util.List;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActorDTO extends MovieTechnicianDTO implements Comparable<MovieActorDTO> {

    private Integer rank;

    public static MovieActorDTO fromActor(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.getId())
                        .person(PersonDTO.of(movieActor.getActor()))
                        .role(movieActor.getRole())
                        .rank(movieActor.getRank())
                        .build()
                ;
    }

    public static MovieActorDTO fromMovie(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.getId())
                        .movie(LightMovieDTO.of(movieActor.getMovie()))
                        .role(movieActor.getRole())
                        .build()
                ;
    }

    public static List<MovieActorDTO> fromEntityList(List<MovieActor> movieActorList) {
        return
                movieActorList
                        .stream()
                        .map(MovieActorDTO::fromActor)
                        .toList()
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
