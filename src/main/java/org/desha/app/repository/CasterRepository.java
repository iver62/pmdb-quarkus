package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Caster;

import java.util.List;

@ApplicationScoped
public class CasterRepository implements PanacheRepository<Caster> {
    
    public Uni<List<Caster>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
