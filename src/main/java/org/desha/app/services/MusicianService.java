package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Musician;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class MusicianService implements PersonServiceInterface<Musician> {

    private final PersonRepository<Musician> personRepository;

    @Inject
    public MusicianService(PersonRepository<Musician> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Musician> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Musician>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Musician>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Musician musician) {
        return Mutiny.fetch(musician.getMovies());
    }


    @Override
    public Uni<Set<Movie>> addMovie(Long musicianId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(musicianId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long musicianId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(musicianId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
