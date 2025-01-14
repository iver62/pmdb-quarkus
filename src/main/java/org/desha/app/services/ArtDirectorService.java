package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.ArtDirectorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ArtDirectorService implements PersonServiceInterface<ArtDirector> {

    private final ArtDirectorRepository artDirectorRepository;

    @Inject
    public ArtDirectorService(ArtDirectorRepository artDirectorRepository) {
        this.artDirectorRepository = artDirectorRepository;
    }

    @Override
    public Uni<ArtDirector> getOne(Long id) {
        return artDirectorRepository.findById(id);
    }

    @Override
    public Uni<Set<ArtDirector>> getByIds(Set<PersonDTO> persons) {
        return
                artDirectorRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<ArtDirector>> getAll() {
        return
                artDirectorRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(ArtDirector artDirector) {
        return Mutiny.fetch(artDirector.getMovies());
    }
}
