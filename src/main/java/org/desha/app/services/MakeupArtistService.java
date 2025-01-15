/*
package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.domain.entity.MakeupArtist;
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
public class MakeupArtistService implements PersonServiceInterface<MakeupArtist> {

    private final PersonRepository<MakeupArtist> personRepository;

    @Inject
    public MakeupArtistService(PersonRepository<MakeupArtist> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<MakeupArtist> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<MakeupArtist>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<MakeupArtist>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(MakeupArtist makeupArtist) {
        return Mutiny.fetch(makeupArtist.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long makeupArtistId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(makeupArtistId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long makeupArtistId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(makeupArtistId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<MakeupArtist> update(Long id, MakeupArtist makeupArtist) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                        .invoke(
                                                entity -> {
                                                    entity.setName(makeupArtist.getName());
                                                    entity.setDateOfBirth(makeupArtist.getDateOfBirth());
                                                    entity.setDateOfDeath(makeupArtist.getDateOfDeath());
                                                    entity.setPhotoPath(makeupArtist.getPhotoPath());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }
}
*/
