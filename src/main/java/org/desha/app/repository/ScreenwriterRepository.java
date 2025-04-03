package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Screenwriter;

import java.util.List;

@ApplicationScoped
public class ScreenwriterRepository extends PersonRepository<Screenwriter> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = "FROM Screenwriter p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + criteriasDTO.getTerm() + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<Screenwriter> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<Screenwriter>> findByName(String name) {
        String query = """
                        FROM Screenwriter s
                        LEFT JOIN FETCH s.countries
                        WHERE LOWER(FUNCTION('unaccent', s.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
                """;

        return find(query, Sort.by("name"), Parameters.with("name", name.toLowerCase()))
                .list();
    }

    public Uni<List<Screenwriter>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = "FROM Screenwriter p " +
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
