package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
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
    private UserDTO user;
    private TechnicalTeamDTO technicalTeam;
    private List<MovieActorDTO> movieActors;
    private Set<CountryDTO> countries;
    private Set<GenreDTO> genres;
    private Set<AwardDTO> awards;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private int nbAwards;

    public static MovieDTO fromEntity(Movie movie) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .releaseDate(movie.getReleaseDate())
                .runningTime(movie.getRunningTime())
                .budget(movie.getBudget())
                .boxOffice(movie.getBoxOffice())
                .posterFileName(movie.getPosterFileName())
                .creationDate(movie.getCreationDate())
                .lastUpdate(movie.getLastUpdate())
                .user(UserDTO.fromEntity(movie.getUser()))
                .build();
    }

    public static MovieDTO fromEntity(Movie movie, Set<Award> awards) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .releaseDate(movie.getReleaseDate())
                .runningTime(movie.getRunningTime())
                .budget(movie.getBudget())
                .boxOffice(movie.getBoxOffice())
                .posterFileName(movie.getPosterFileName())
                .creationDate(movie.getCreationDate())
                .lastUpdate(movie.getLastUpdate())
                .user(UserDTO.fromEntity(movie.getUser()))
                .awards(awards.stream().map(AwardDTO::fromEntity).collect(Collectors.toSet()))
                .build();
    }

}
