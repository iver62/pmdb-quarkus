package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.CasterRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CasterService implements PersonServiceInterface<Caster> {

    private final CasterRepository casterRepository;

    @Inject
    public CasterService(CasterRepository casterRepository) {
        this.casterRepository = casterRepository;
    }

    @Override
    public Uni<Caster> getOne(Long id) {
        return casterRepository.findById(id);
    }

    @Override
    public Uni<Set<Caster>> getByIds(Set<PersonDTO> persons) {
        return
                casterRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Caster>> getAll() {
        return
                casterRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Caster caster) {
        return Mutiny.fetch(caster.getMovies());
    }
}
