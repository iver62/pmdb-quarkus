package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Decorator;

import java.util.List;

@ApplicationScoped
public class DecoratorRepository implements PanacheRepository<Decorator> {
    
    public Uni<List<Decorator>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
