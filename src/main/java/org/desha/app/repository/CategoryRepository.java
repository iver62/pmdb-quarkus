package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.Category;

import java.util.List;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {

    public Uni<Long> countCategories(String term) {
        return
                count(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Parameters.with("term", "%" + term + "%")
                );
    }

    public Uni<List<Category>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    public Uni<List<Category>> findCategories(String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                ).list();
    }
}
