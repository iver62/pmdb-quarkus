package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Photographer;

import java.util.List;

@ApplicationScoped
public class PhotographerRepository implements PanacheRepository<Photographer> {
    
    public Uni<List<Photographer>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
