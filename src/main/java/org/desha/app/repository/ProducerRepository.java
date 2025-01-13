package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Producer;

import java.util.List;

@ApplicationScoped
public class ProducerRepository implements PanacheRepository<Producer> {
    
    public Uni<List<Producer>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
