package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.DirectorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DirectorService implements PersonServiceInterface<Director> {

    private final DirectorRepository directorRepository;

    @Inject
    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    @Override
    public Uni<Director> getOne(Long id) {
        return directorRepository.findById(id);
    }

    @Override
    public Uni<Set<Director>> getByIds(Set<Director> personSet) {
        return
                directorRepository.findByIds(
                        Optional.ofNullable(personSet).orElse(Collections.emptySet())
                                .stream()
                                .map(p -> p.id)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Director>> getAll() {
        return
                directorRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Director director) {
        return Mutiny.fetch(director.getMovies());
    }
}
