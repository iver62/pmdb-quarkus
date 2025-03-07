package org.desha.app.service;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PersonServiceInterface<T extends Person> {

    Uni<Long> count(
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    );

    Uni<Long> countMovies(long personId, FiltersDTO filtersDTO);

    Uni<T> getById(Long id);

    Uni<List<T>> getByIds(List<Long> ids);

    Uni<List<PersonDTO>> get(
            int pageIndex,
            int size,
            String sort,
            Sort.Direction direction,
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    );

    Uni<List<PersonDTO>> getAll();

    Uni<List<MovieDTO>> getMovies(long id, int page, int size, String sort, Sort.Direction sortDirection, FiltersDTO filtersDTO);

    Uni<List<Movie>> addMovie(Long personId, Movie movie);

    Uni<List<Movie>> removeMovie(Long personId, Long movieId);
}
