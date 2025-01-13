package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.DirectorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
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
    public Uni<Set<Director>> getByIds(Set<PersonDTO> directors) {
        return
                directorRepository.findByIds(
                        Optional.ofNullable(directors).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
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
