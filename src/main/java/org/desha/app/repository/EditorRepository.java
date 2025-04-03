package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Editor;

import java.util.List;

@ApplicationScoped
public class EditorRepository extends PersonRepository<Editor> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = "LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", criteriasDTO.getTerm()),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<Editor> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<Editor>> findByName(String name) {
        String query = """
                        FROM Editor e
                        LEFT JOIN FETCH e.countries
                        WHERE LOWER(FUNCTION('unaccent', e.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """;

        return find(query, Sort.by("name"), Parameters.with("term", name.toLowerCase()))
                .list();
    }

    public Uni<List<Editor>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = "FROM Editor p " +
                "LEFT JOIN FETCH p.movies " +
                "WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", criteriasDTO.getTerm()),
                criteriasDTO
        );

        return
                find(query, Sort.by("p." + sort, direction, Sort.NullPrecedence.NULLS_LAST), params)
                        .page(page)
                        .list()
                ;
    }
}