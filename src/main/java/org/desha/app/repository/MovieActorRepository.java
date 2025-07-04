package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.MovieActor;

import java.util.List;

@Slf4j
@ApplicationScoped
public class MovieActorRepository implements PanacheRepositoryBase<MovieActor, Long> {

    public Uni<Long> countMovieActorsByActor(Long id) {
        return count("actor.id", id);
    }

    public Uni<List<MovieActor>> findMovieActorsByActor(Long id, Page page, String sortField, Sort.Direction direction) {
        return
                find(
                        """
                                SELECT ma
                                FROM MovieActor ma
                                JOIN FETCH ma.movie
                                WHERE ma.actor.id = :id
                                """, Sort.by(sortField, direction), Parameters.with("id", id)
                )
                        .page(page)
                        .list()
                ;
    }
}