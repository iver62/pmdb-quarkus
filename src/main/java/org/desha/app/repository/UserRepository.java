package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Uni<Long> countUsers(String term) {
        return count("LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', ?1))", "%" + term + "%");
    }

    public Uni<List<User>> findUsers(Page page, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "FROM User u " +
                                "LEFT JOIN FETCH u.movies " +
                                "WHERE LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                )
                        .page(page)
                        .list();
    }

    public Uni<List<User>> findUsers(String sort, Sort.Direction direction, String term) {
        return
                find(
                        "LOWER(FUNCTION('unaccent', username)) LIKE LOWER(FUNCTION('unaccent', :term))",
                        Sort.by(sort, direction),
                        Parameters.with("term", "%" + term + "%")
                ).list();
    }
}
