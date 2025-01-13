package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Musician;
import org.desha.app.repository.MusicianRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class MusicianService implements PersonServiceInterface<Musician> {

    private final MusicianRepository musicianRepository;

    @Inject
    public MusicianService(MusicianRepository musicianRepository) {
        this.musicianRepository = musicianRepository;
    }

    @Override
    public Uni<Musician> getOne(Long id) {
        return musicianRepository.findById(id);
    }

    @Override
    public Uni<Set<Musician>> getByIds(Set<PersonDTO> persons) {
        return
                musicianRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Musician>> getAll() {
        return
                musicianRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Musician musician) {
        return Mutiny.fetch(musician.getMovies());
    }
}
