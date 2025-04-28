package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.MovieActor;

@Builder
@Getter
public class MovieActorDTO implements Comparable<MovieActorDTO> {

    private Long id;
    private PersonDTO actor;
    private String role;
    private Integer rank;

    public static MovieActorDTO fromEntity(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.getId())
                        .actor(PersonDTO.fromEntity(movieActor.getActor()))
                        .role(movieActor.getRole())
                        .rank(movieActor.getRank())
                        .build()
                ;
    }

    public String toString() {
        return id + " / " + actor.getId() + ": " + actor.getName() + " -> " + role + " (" + rank + ")";
    }

    @Override
    public int compareTo(MovieActorDTO o) {
        return rank.compareTo(o.getRank());
    }
}
