package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Person;
import org.desha.app.helper.MovieRepositoryHelper;

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

    public Uni<Long> countCategoriesInMovies(String term) {
        return count("""
                        SELECT COUNT(DISTINCT c)
                        FROM Movie m
                        JOIN m.categories c
                        WHERE LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        """,
                Parameters.with("term", "%" + term + "%")
        );
    }

    public Uni<List<Category>> findByIds(List<Long> ids) {
        return list("id IN ?1", ids);
    }

    public Uni<List<Category>> findCategories(Page page, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                )
                        .page(page)
                        .list();
    }

    public Uni<List<Category>> findCategoriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                SELECT DISTINCT c
                FROM Movie m
                JOIN m.categories c
                WHERE LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;
        return
                find(query, Sort.by(sort, direction), Parameters.with("term", "%" + term + "%"))
                        .page(page)
                        .list();
    }

    public Uni<List<Category>> findMovieCategoriesByPerson(Person person, Page page, String sort, Sort.Direction direction, String term) {
        final String query = """
                    SELECT DISTINCT c
                    FROM Movie m
                    JOIN m.categories c
                    WHERE (%s)
                    AND LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """.formatted(MovieRepositoryHelper.buildExistsClause(person));

        Parameters parameters = Parameters.with("person", person)
                .and("term", "%" + StringUtils.defaultString(term + "%"));

        return
                find(query, Sort.by(sort, direction), parameters)
                        .page(page)
                        .list()
                ;
    }
}
