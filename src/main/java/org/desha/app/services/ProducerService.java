package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Producer;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ProducerService implements PersonServiceInterface<Producer> {

    private final PersonRepository<Producer> personRepository;

    @Inject
    public ProducerService(PersonRepository<Producer> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Producer> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Producer>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Producer>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Producer producer) {
        return Mutiny.fetch(producer.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long producerId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(producerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long producerId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(producerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
