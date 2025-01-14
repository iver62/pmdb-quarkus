package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ScreenwriterService implements PersonServiceInterface<Screenwriter> {

    private final PersonRepository<Screenwriter> personRepository;

    @Inject
    public ScreenwriterService(PersonRepository<Screenwriter> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Screenwriter> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Screenwriter>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Screenwriter>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Screenwriter screenwriter) {
        return Mutiny.fetch(screenwriter.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long screenwriterId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(screenwriterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long screenwriterId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(screenwriterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
