package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDate;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LightMovieDTO {

    private Long id;
    private String title;
    private String originalTitle;
    private LocalDate releaseDate;

    public static LightMovieDTO of(Movie movie) {
        return
                LightMovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .releaseDate(movie.getReleaseDate())
                        .build()
                ;
    }

}
