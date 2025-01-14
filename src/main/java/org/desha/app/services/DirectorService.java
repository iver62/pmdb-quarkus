package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class DirectorService implements PersonServiceInterface<Director> {

    private final PersonRepository<Director> personRepository;

    @Inject
    public DirectorService(PersonRepository<Director> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Director> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Director>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Director>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Director director) {
        return Mutiny.fetch(director.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long directorId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(directorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long directorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(directorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
