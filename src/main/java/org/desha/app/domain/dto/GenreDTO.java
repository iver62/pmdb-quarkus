package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Genre;

import java.time.LocalDateTime;

@Getter
@Builder
public class GenreDTO {

    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static GenreDTO fromEntity(Genre genre) {
        return
                GenreDTO.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .creationDate(genre.getCreationDate())
                        .lastUpdate(genre.getLastUpdate())
                        .build();
    }

}
