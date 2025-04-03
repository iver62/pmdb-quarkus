package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.VisualEffectsSupervisor;

import java.util.List;

@ApplicationScoped
public class VisualEffectsSupervisorRepository extends PersonRepository<VisualEffectsSupervisor> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = """
                        FROM VisualEffectsSupervisor p
                        WHERE LOWER(FUNCTION('unaccent', name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """ + addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", criteriasDTO.getTerm()),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<VisualEffectsSupervisor> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<VisualEffectsSupervisor>> findByName(String name) {
        String query = """
                FROM VisualEffectsSupervisor ves
                LEFT JOIN FETCH ves.countries
                WHERE LOWER(FUNCTION('unaccent', ves.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :name, '%')))
                """;

        return find(query, Sort.by("name"), Parameters.with("name", name.toLowerCase()))
                .list();
    }

    public Uni<List<VisualEffectsSupervisor>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = """
                FROM VisualEffectsSupervisor p
                LEFT JOIN FETCH p.movies
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :term, '%')))
                """ + addClauses(criteriasDTO);

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
