package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
    @NotBlank(message = "Le titre est obligatoire")
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

    public static MovieDTO build(Long id, String title, String originalTitle, String synopsis, LocalDate releaseDate, Long runningTime, Long budgetValue, String budgetCurrency, Long boxOfficeValue, String boxOfficeCurrency, String posterFileName, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                MovieDTO.builder()
                        .id(id)
                        .title(title)
                        .originalTitle(originalTitle)
                        .synopsis(synopsis)
                        .releaseDate(releaseDate)
                        .runningTime(runningTime)
                        .budget(BudgetDTO.build(budgetValue, budgetCurrency))
                        .boxOffice(BoxOfficeDTO.build(boxOfficeValue, boxOfficeCurrency))
                        .posterFileName(posterFileName)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
                        .build()
                ;
    }

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
