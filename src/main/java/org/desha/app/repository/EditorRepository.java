package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Editor;

import java.util.List;

@ApplicationScoped
public class EditorRepository implements PanacheRepository<Editor> {
    
    public Uni<List<Editor>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

}
