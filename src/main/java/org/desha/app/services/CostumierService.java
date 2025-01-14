package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CostumierService implements PersonServiceInterface<Costumier> {

    private final PersonRepository<Costumier> personRepository;

    @Inject
    public CostumierService(PersonRepository<Costumier> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Costumier> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Costumier>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Costumier>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Costumier costumier) {
        return Mutiny.fetch(costumier.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long costumierId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(costumierId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long costumierId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(costumierId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
