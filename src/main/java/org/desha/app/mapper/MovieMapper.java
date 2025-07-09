package org.desha.app.mapper;

import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.record.MovieWithAwardsNumber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "jakarta",
        uses = {UserMapper.class, CategoryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MovieMapper {

    @Mapping(target = "budget.value", source = "budget")
    @Mapping(target = "budget.currency", source = "budgetCurrency")
    @Mapping(target = "boxOffice.value", source = "boxOffice")
    @Mapping(target = "boxOffice.currency", source = "boxOfficeCurrency")
    @Mapping(target = "user", source = "user", qualifiedByName = "toLiteUserDTO")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "toLiteCategoryDTO")
    @Mapping(target = "movieActors", ignore = true)
    MovieDTO movieToMovieDTO(Movie entity);

    @Mapping(target = "id", source = "movie.id")
    @Mapping(target = "title", source = "movie.title")
    @Mapping(target = "originalTitle", source = "movie.originalTitle")
    @Mapping(target = "synopsis", source = "movie.synopsis")
    @Mapping(target = "releaseDate", source = "movie.releaseDate")
    @Mapping(target = "runningTime", source = "movie.runningTime")
    @Mapping(target = "posterFileName", source = "movie.posterFileName")
    @Mapping(target = "budget.value", source = "movie.budget")
    @Mapping(target = "budget.currency", source = "movie.budgetCurrency")
    @Mapping(target = "boxOffice.value", source = "movie.boxOffice")
    @Mapping(target = "boxOffice.currency", source = "movie.boxOfficeCurrency")
    @Mapping(target = "creationDate", source = "movie.creationDate")
    @Mapping(target = "lastUpdate", source = "movie.lastUpdate")
    @Mapping(target = "numberOfAwards", source = "awardsNumber")
    @Mapping(target = "user", source = "movie.user", qualifiedByName = "toLiteUserDTO")
    @Mapping(target = "movie.movieActors", ignore = true)
    MovieDTO movieWithAwardsNumberToMovieDTO(MovieWithAwardsNumber entity);

    @Mapping(target = "budget", source = "budget.value")
    @Mapping(target = "budgetCurrency", source = "budget.currency")
    @Mapping(target = "boxOffice", source = "boxOffice.value")
    @Mapping(target = "boxOfficeCurrency", source = "boxOffice.currency")
    Movie movieDTOtoMovie(MovieDTO dto);

    List<MovieDTO> movieListToDTOList(List<Movie> movieList);

    List<MovieDTO> toDTOWithAwardsNumberList(List<MovieWithAwardsNumber> entityList);

    List<MovieDTO> movieWithAwardsListToDTOList(List<MovieWithAwardsNumber> movieWithAwardsNumberList);

}
