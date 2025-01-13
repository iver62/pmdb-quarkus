package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.repository.ScreenwriterRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ScreenwriterService implements PersonServiceInterface<Screenwriter> {

    private final ScreenwriterRepository screenwriterRepository;

    @Inject
    public ScreenwriterService(ScreenwriterRepository screenwriterRepository) {
        this.screenwriterRepository = screenwriterRepository;
    }

    @Override
    public Uni<Screenwriter> getOne(Long id) {
        return screenwriterRepository.findById(id);
    }

    @Override
    public Uni<Set<Screenwriter>> getByIds(Set<PersonDTO> persons) {
        return
                screenwriterRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Screenwriter>> getAll() {
        return
                screenwriterRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Screenwriter screenwriter) {
        return Mutiny.fetch(screenwriter.getMovies());
    }
}
