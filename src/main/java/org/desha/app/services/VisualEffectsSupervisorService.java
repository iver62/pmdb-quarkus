/*
package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class VisualEffectsSupervisorService implements PersonServiceInterface<VisualEffectsSupervisor> {

    private final PersonRepository<VisualEffectsSupervisor> personRepository;

    @Inject
    public VisualEffectsSupervisorService(PersonRepository<VisualEffectsSupervisor> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<VisualEffectsSupervisor> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<VisualEffectsSupervisor>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<VisualEffectsSupervisor>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(VisualEffectsSupervisor visualEffectsSupervisor) {
        return Mutiny.fetch(visualEffectsSupervisor.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long visualEffectsSupervisorId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(visualEffectsSupervisorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long visualEffectsSupervisorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(visualEffectsSupervisorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<VisualEffectsSupervisor> update(Long id, VisualEffectsSupervisor visualEffectsSupervisor) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                        .invoke(
                                                entity -> {
                                                    entity.setName(visualEffectsSupervisor.getName());
                                                    entity.setDateOfBirth(visualEffectsSupervisor.getDateOfBirth());
                                                    entity.setDateOfDeath(visualEffectsSupervisor.getDateOfDeath());
                                                    entity.setPhotoPath(visualEffectsSupervisor.getPhotoPath());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }
}
*/
