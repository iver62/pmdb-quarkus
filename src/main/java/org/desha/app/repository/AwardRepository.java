package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Award;

import java.util.List;

@ApplicationScoped
public class AwardRepository implements PanacheRepository<Award> {

    public Uni<List<Award>> findAwardsByMovie(Long id) {
        return find("SELECT a FROM Award a WHERE a.movie.id = :id", Parameters.with("id", id))
                .list();
    }

    public Uni<List<String>> findCeremonies() {
        return find("SELECT DISTINCT a.ceremony FROM Award a")
                .project(String.class)
                .list();
    }

}
