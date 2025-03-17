package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.MovieActor;

@Builder
@Getter
public class MovieActorDTO {

    private Long id;
    private PersonDTO actor;
    private String role;
    private Integer rank;

    public static MovieActorDTO fromEntity(MovieActor movieActor) {
        return
                MovieActorDTO.builder()
                        .id(movieActor.id)
                        .actor(
                                PersonDTO.builder()
                                        .id(movieActor.getActor().id)
                                        .name(movieActor.getActor().getName())
                                        .photoFileName(movieActor.getActor().getPhotoFileName())
                                        .build()
                        )
                        .role(movieActor.getRole())
                        .rank(movieActor.getRank())
                        .build()
                ;
    }

}
