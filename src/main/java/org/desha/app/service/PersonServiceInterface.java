package org.desha.app.service;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.List;

public interface PersonServiceInterface<T extends Person> {

    Uni<Long> count(CriteriasDTO criteriasDTO);

    Uni<Long> countCountries(String term);

    Uni<Long> countMovies(long personId, CriteriasDTO criteriasDTO);

    Uni<T> getById(Long id);

    Uni<List<PersonDTO>> searchByName(String name);

    Uni<List<T>> getByIds(List<Long> ids);

    Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term);

    Uni<List<PersonDTO>> get(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO);

    Uni<List<PersonDTO>> getAll();

    Uni<List<MovieDTO>> getMovies(long id, Page page, String sort, Sort.Direction sortDirection, CriteriasDTO criteriasDTO);

    Uni<List<Movie>> addMovie(Long personId, Movie movie);

    Uni<List<Movie>> removeMovie(Long personId, Long movieId);

    Uni<T> save(PersonDTO personDTO);
}
