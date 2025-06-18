package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;

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
    private Budget budget;
    private BoxOffice boxOffice;
    private String posterFileName;
    private Integer numberOfAwards;
    private UserDTO user;
    private TechnicalTeamDTO technicalTeam;
    private List<MovieActorDTO> movieActors;
    private Set<CountryDTO> countries;
    private Set<CategoryDTO> categories;
    private Set<AwardDTO> awards;
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
                        .budget(Budget.of(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOffice.of(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .user(UserDTO.fromEntity(movie.getUser()))
                        .countries(CountryDTO.fromCountryEntitySet(movie.getCountries()))
                        .categories(CategoryDTO.fromCategorySetEntity(movie.getCategories()))
                        .awards(AwardDTO.fromEntitySet(movie.getAwards()))
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .build()
                ;
    }

    public static MovieDTO of(Movie movie) {
        return
                MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .releaseDate(movie.getReleaseDate())
                        .runningTime(movie.getRunningTime())
                        .budget(Budget.of(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOffice.of(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .user(UserDTO.fromEntity(movie.getUser()))
                        .build()
                ;
    }

    public static MovieDTO of(Movie movie, Set<Category> categorySet, Set<Country> countrySet) {
        return
                MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .releaseDate(movie.getReleaseDate())
                        .synopsis(movie.getSynopsis())
                        .runningTime(movie.getRunningTime())
                        .budget(Budget.of(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOffice.of(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .categories(CategoryDTO.fromCategorySetEntity(categorySet))
                        .countries(CountryDTO.fromCountryEntitySet(countrySet))
                        .user(UserDTO.fromEntity(movie.getUser()))
                        .build()
                ;
    }

    public static MovieDTO of(Movie movie, Set<Category> categorySet, Set<Country> countrySet, Set<Award> awardSet) {
        return
                MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .releaseDate(movie.getReleaseDate())
                        .runningTime(movie.getRunningTime())
                        .budget(Budget.of(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOffice.of(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .categories(CategoryDTO.fromCategorySetEntity(categorySet))
                        .countries(CountryDTO.fromCountryEntitySet(countrySet))
                        .awards(AwardDTO.fromEntitySet(awardSet))
                        .user(UserDTO.fromEntity(movie.getUser()))
                        .build()
                ;
    }

    public static MovieDTO of(Movie movie, Integer numberOfAwards) {
        return
                MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .originalTitle(movie.getOriginalTitle())
                        .releaseDate(movie.getReleaseDate())
                        .runningTime(movie.getRunningTime())
                        .budget(Budget.of(movie.getBudget(), movie.getBudgetCurrency()))
                        .boxOffice(BoxOffice.of(movie.getBoxOffice(), movie.getBoxOfficeCurrency()))
                        .posterFileName(movie.getPosterFileName())
                        .creationDate(movie.getCreationDate())
                        .lastUpdate(movie.getLastUpdate())
                        .user(UserDTO.fromEntity(movie.getUser()))
                        .numberOfAwards(numberOfAwards)
                        .build()
                ;
    }

}
