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
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class ArtDirectorService implements PersonServiceInterface<ArtDirector> {

    private final PersonRepository<ArtDirector> personRepository;

    @Inject
    public ArtDirectorService(PersonRepository<ArtDirector> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<ArtDirector> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<ArtDirector>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<ArtDirector>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(ArtDirector artDirector) {
        return Mutiny.fetch(artDirector.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long artDirectorId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(artDirectorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long artDirectorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(artDirectorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<ArtDirector> update(Long id, ArtDirector artDirector) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                        .invoke(
                                                entity -> {
                                                    entity.setName(artDirector.getName());
                                                    entity.setDateOfBirth(artDirector.getDateOfBirth());
                                                    entity.setDateOfDeath(artDirector.getDateOfDeath());
                                                    entity.setPhotoPath(artDirector.getPhotoPath());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }
}
*/
