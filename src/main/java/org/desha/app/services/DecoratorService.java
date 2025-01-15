/*
package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Decorator;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class DecoratorService implements PersonServiceInterface<Decorator> {

    private final PersonRepository<Decorator> personRepository;

    @Inject
    public DecoratorService(PersonRepository<Decorator> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Decorator> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Decorator>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Decorator>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Decorator decorator) {
        return Mutiny.fetch(decorator.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long decoratorId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(decoratorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long decoratorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(decoratorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
*/
