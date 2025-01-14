package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Decorator;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.DecoratorRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class DecoratorService implements PersonServiceInterface<Decorator> {

    private final DecoratorRepository decoratorRepository;

    @Inject
    public DecoratorService(DecoratorRepository decoratorRepository) {
        this.decoratorRepository = decoratorRepository;
    }

    @Override
    public Uni<Decorator> getOne(Long id) {
        return decoratorRepository.findById(id);
    }

    @Override
    public Uni<Set<Decorator>> getByIds(Set<PersonDTO> persons) {
        return
                decoratorRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Decorator>> getAll() {
        return
                decoratorRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Decorator decorator) {
        return Mutiny.fetch(decorator.getMovies());
    }
}
