package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
public class PersonServiceImpl<T> implements PersonServiceInterface<T> {

    private final PersonRepository<T> personRepository;

    @Inject
    public PersonServiceImpl(PersonRepository<T> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<T> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<T>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<T>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(T t) {
        return Mutiny.fetch(t.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long id, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Person> update(Long id, T t) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                        .invoke(
                                                entity -> {
                                                    entity.setName(t.getName());
                                                    entity.setDateOfBirth(t.getDateOfBirth());
                                                    entity.setDateOfDeath(t.getDateOfDeath());
                                                    entity.setPhotoPath(t.getPhotoPath());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }

    public Uni<Response> delete(Long id) {
        return
                Panache
                        .withTransaction(() -> personRepository.deleteById(id))
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build());
    }
}
