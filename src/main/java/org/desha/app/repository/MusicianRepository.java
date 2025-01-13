package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Musician;

import java.util.List;

@ApplicationScoped
public class MusicianRepository implements PanacheRepository<Musician> {
    
    public Uni<List<Musician>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
