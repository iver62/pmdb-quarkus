package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Photographer;
import org.desha.app.repository.PhotographerRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PhotographerService implements PersonServiceInterface<Photographer> {

    private final PhotographerRepository photographerRepository;

    @Inject
    public PhotographerService(PhotographerRepository photographerRepository) {
        this.photographerRepository = photographerRepository;
    }

    @Override
    public Uni<Photographer> getOne(Long id) {
        return photographerRepository.findById(id);
    }

    @Override
    public Uni<Set<Photographer>> getByIds(Set<PersonDTO> persons) {
        return
                photographerRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Photographer>> getAll() {
        return
                photographerRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Photographer photographer) {
        return Mutiny.fetch(photographer.getMovies());
    }
}
