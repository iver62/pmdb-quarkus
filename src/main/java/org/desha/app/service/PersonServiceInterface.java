package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.Set;

public interface PersonServiceInterface<T extends Person> {

    Uni<T> getOne(Long id);

    Uni<Set<T>> getByIds(Set<PersonDTO> personsDTO);

    Uni<Set<T>> getAll();

    Uni<Set<Movie>> getMovies(T t);

    Uni<Set<Movie>> addMovie(Long personId, Movie movie);

    Uni<Set<Movie>> removeMovie(Long personId, Long movieId);
}
