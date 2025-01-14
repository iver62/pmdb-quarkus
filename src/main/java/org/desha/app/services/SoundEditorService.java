package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.SoundEditor;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class SoundEditorService implements PersonServiceInterface<SoundEditor> {

    private final PersonRepository<SoundEditor> personRepository;

    @Inject
    public SoundEditorService(PersonRepository<SoundEditor> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<SoundEditor> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<SoundEditor>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<SoundEditor>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(SoundEditor soundEditor) {
        return Mutiny.fetch(soundEditor.getMovies());
    }

    @Override
    public Uni<Set<Movie>> addMovie(Long soundEditorId, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(soundEditorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<Set<Movie>> removeMovie(Long soundEditorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(soundEditorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }
}
