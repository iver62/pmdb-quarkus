package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;

import java.util.Set;

public interface PersonServiceInterface<T> {

    Uni<T> getOne(Long id);

    Uni<Set<T>> getByIds(Set<PersonDTO> personsDTO);

    Uni<Set<T>> getAll();

    Uni<Set<Movie>> getMovies(T t);
}
