package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.CostumierRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class CostumierService implements PersonServiceInterface<Costumier> {

    private final CostumierRepository costumierRepository;

    @Inject
    public CostumierService(CostumierRepository costumierRepository) {
        this.costumierRepository = costumierRepository;
    }

    @Override
    public Uni<Costumier> getOne(Long id) {
        return costumierRepository.findById(id);
    }

    @Override
    public Uni<Set<Costumier>> getByIds(Set<PersonDTO> persons) {
        return
                costumierRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<Costumier>> getAll() {
        return
                costumierRepository
                        .listAll()
                        .map(HashSet::new)
                ;
    }

    @Override
    public Uni<Set<Movie>> getMovies(Costumier costumier) {
        return Mutiny.fetch(costumier.getMovies());
    }
}
