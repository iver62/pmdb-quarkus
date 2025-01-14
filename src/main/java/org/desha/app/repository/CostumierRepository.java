package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Director;

import java.util.List;

@ApplicationScoped
public class CostumierRepository implements PanacheRepository<Costumier> {
    
    public Uni<List<Costumier>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
