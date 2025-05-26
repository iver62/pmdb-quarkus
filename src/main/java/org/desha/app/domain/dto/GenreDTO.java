package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Genre;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static Set<GenreDTO> fromGenreSetEntity(Set<Genre> genreSet) {
        return
                Optional.ofNullable(genreSet).orElse(Set.of())
                        .stream()
                        .map(GenreDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }

    public String toString() {
        return id + ": " + name;
    }

}
