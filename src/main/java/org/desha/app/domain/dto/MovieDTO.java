package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDTO {

    private Long id;
    private String title;
    private String originalTitle;
    private String synopsis;
    private LocalDate releaseDate;
    private Long runningTime;
    private Long budget;
    private Long boxOffice;
    private String posterFileName;
    private TechnicalTeamDTO technicalTeam;
    private List<MovieActorDTO> movieActors;
    private Set<CountryDTO> countries;
    private Set<GenreDTO> genres;
    private Set<Award> awards;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static MovieDTO fromEntity(Movie movie) {
        return MovieDTO.builder()
                .id(movie.id)
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .releaseDate(movie.getReleaseDate())
                .posterFileName(movie.getPosterFileName())
                .build();
    }

}
