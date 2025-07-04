package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Uni<Long> countUsers(String term) {
        return count("LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', ?1))", "%" + StringUtils.defaultString(term) + "%");
    }

    public Uni<List<User>> findUsers(Page page, String sort, Sort.Direction direction, String term) {
        String query = """
                FROM User u
                LEFT JOIN FETCH u.movies
                WHERE LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addSort(sort, direction);

        Parameters params = Parameters.with("term", "%" + StringUtils.defaultString(term) + "%");

        return find(query, params).page(page).list();
    }

    public Uni<List<User>> findUsers(String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', :term)) ",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + StringUtils.defaultString(term) + "%")
                ).list();
    }

    private String addSort(String sort, Sort.Direction direction) {
        if (StringUtils.isEmpty(sort)) return "";

        String dir = (direction == Sort.Direction.Ascending) ? "ASC" : "DESC";

        // Si le critère de tri est le nombre de films
        if ("moviesCount".equals(sort)) {
            return String.format(" ORDER BY SIZE(u.movies) %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        Set<String> allowedFields = User.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN u.%s IS NULL THEN 1 ELSE 0 END, u.%s %s", sort, sort, dir);
    }
}
