package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    private BudgetDTO budget;
    private BoxOfficeDTO boxOffice;
    private String posterFileName;
    private Long numberOfAwards;
    private UserDTO user;
    private TechnicalTeamDTO technicalTeam;
    private List<MovieActorDTO> movieActors;
    private Set<CountryDTO> countries;
    private Set<CategoryDTO> categories;
    private Set<CeremonyAwardsDTO> ceremonyAwards;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static MovieDTO build(Movie movie) {
        return
                MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .synopsis(movie.getSynopsis())
                        .releaseDate(movie.getReleaseDate())
                        .runningTime(movie.getRunningTime())
                        .budget(BudgetDTO.build(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOfficeDTO.build(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .build()
                ;
    }

}
