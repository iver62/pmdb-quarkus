package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Country;

import java.util.List;

@ApplicationScoped
public class CountryRepository implements PanacheRepository<Country> {

    public Uni<List<Country>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }


}
