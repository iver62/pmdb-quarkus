package org.desha.app.service;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.List;
import java.util.Set;

public interface PersonServiceInterface<T extends Person> {

    Uni<Long> count(String name);

    Uni<T> getOne(Long id);

    Uni<List<T>> getByIds(List<Long> ids);

    Uni<List<T>> get(int pageIndex, int size, String sort, Sort.Direction direction, String name);

    Uni<List<T>> getAll();

    Uni<List<Movie>> getMovies(Long id, Class<T> personType, int page, int size, String sort, Sort.Direction sortDirection, String title);

    Uni<Set<Country>> getCountries(Long id);

    Uni<Set<Movie>> addMovie(Long personId, Movie movie);

    Uni<Set<Movie>> removeMovie(Long personId, Long movieId);
}
