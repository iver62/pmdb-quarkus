package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Movie;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    public Uni<Set<Movie>> findByTitle(String title) {
        return list("title", title).map(HashSet::new);
    }

}
