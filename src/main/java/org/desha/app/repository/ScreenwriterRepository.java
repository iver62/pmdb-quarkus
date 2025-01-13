package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Producer;
import org.desha.app.domain.entity.Screenwriter;

import java.util.List;

@ApplicationScoped
public class ScreenwriterRepository implements PanacheRepository<Screenwriter> {
    
    public Uni<List<Screenwriter>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
