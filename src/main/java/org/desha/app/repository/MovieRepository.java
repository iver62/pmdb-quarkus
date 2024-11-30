package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.Movie;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class MovieRepository implements PanacheRepository<Movie> {

    public Uni<Set<Movie>> findByTitle(String title) {
        return list("title", title).map(HashSet::new);
    }

}
