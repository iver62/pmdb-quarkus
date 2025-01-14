package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PersonRepository<T> implements PanacheRepository<T> {

    public Uni<List<T>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
