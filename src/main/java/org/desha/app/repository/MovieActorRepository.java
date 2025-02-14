package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.MovieActor;

import java.util.List;

@Slf4j
@ApplicationScoped
public class MovieActorRepository implements PanacheRepository<MovieActor> {

    public Uni<Long> deleteByIds(List<Long> ids) {
        return delete("id IN ?1", ids);
    }

}