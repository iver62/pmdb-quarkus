package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Photographer;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PhotographerService implements PersonServiceInterface<Photographer> {

    private final PersonRepository<Photographer> personRepository;

    @Inject
    public PhotographerService(PersonRepository<Photographer> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Photographer> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Photographer>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Photographer>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Photographer photographer) {
        return Mutiny.fetch(photographer.getMovies());
    }


    @Override
    public Uni<Set<Movie>> addMovie(Long photographerId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(photographerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long photographerId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(photographerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
