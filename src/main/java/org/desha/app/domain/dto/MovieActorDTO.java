package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.MovieActor;

@Builder
@Getter
public class MovieActorDTO {

    private Long id;
    private MovieDTO movie;
    private PersonDTO actor;
    private String role;
    private Integer rank;

    public static MovieActorDTO fromEntity(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.getId())
                        .actor(PersonDTO.fromEntity(movieActor.getActor()))
                        .movie(MovieDTO.fromEntity(movieActor.getMovie()))
                        .role(movieActor.getRole())
                        .rank(movieActor.getRank())
                        .build()
                ;
    }

}
