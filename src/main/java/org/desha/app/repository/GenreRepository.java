package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Genre;

import java.util.List;

@ApplicationScoped
public class GenreRepository implements PanacheRepository<Genre> {

    public Uni<List<Genre>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    public Uni<List<Genre>> findGenres(String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))",
                        Sort.by(sort, direction),
                        Parameters.with("term", term)
                ).list();
    }
}
