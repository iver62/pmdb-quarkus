package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CasterService implements PersonServiceInterface<Caster> {

    private final PersonRepository<Caster> personRepository;

    @Inject
    public CasterService(PersonRepository<Caster> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<Caster> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<Caster>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Caster>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Caster caster) {
        return Mutiny.fetch(caster.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long casterId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(casterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long casterId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(casterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
