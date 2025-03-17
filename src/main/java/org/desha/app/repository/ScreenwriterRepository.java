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
    public Uni<Screenwriter> findByIdWithCountriesAndMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    public Uni<List<Screenwriter>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = "FROM Screenwriter p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + criteriasDTO.getTerm() + "%"),
                criteriasDTO
        );

        return
                find(query, Sort.by(sort, direction), params)
                        .page(page)
                        .list()
                ;
    }
}
