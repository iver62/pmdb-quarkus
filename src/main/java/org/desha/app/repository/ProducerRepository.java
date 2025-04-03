package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Producer;

import java.util.List;

@ApplicationScoped
public class ProducerRepository extends PersonRepository<Producer> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = "FROM Producer p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + criteriasDTO.getTerm() + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<Producer> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<Producer>> findByName(String name) {
        String query = """
                        FROM Producer p
                        LEFT JOIN FETCH p.countries
                        WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
                """;

        return find(query, Sort.by("name"), Parameters.with("name", name.toLowerCase()))
                .list();
    }

    public Uni<List<Producer>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = "FROM Producer p " +
                "LEFT JOIN FETCH p.movies " +
                "WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + criteriasDTO.getTerm() + "%"),
                criteriasDTO
        );

        return
                find(query, Sort.by("p." + sort, direction, Sort.NullPrecedence.NULLS_LAST), params)
                        .page(page)
                        .list()
                ;
    }
}
