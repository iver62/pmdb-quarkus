package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Costumier;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CostumierRepository extends PersonRepository<Costumier> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = """
                FROM Costumier p
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO);

        String term = Optional.ofNullable(criteriasDTO.getTerm()).orElse("");

        Parameters params = addParameters(
                Parameters.with("term", "%" + term + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<Costumier> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<Costumier>> findByName(String name) {
        String query = """
                FROM Costumier c
                LEFT JOIN FETCH c.countries
                WHERE LOWER(FUNCTION('unaccent', c.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;

        String term = Optional.ofNullable(name).orElse("");

        return find(query, Sort.by("name"), Parameters.with("term", "%" + term + "%"))
                .list();
    }

    public Uni<List<Costumier>> find(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = """
                FROM Costumier p
                LEFT JOIN FETCH p.movies
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO);

        String term = Optional.ofNullable(criteriasDTO.getTerm()).orElse("");

        Parameters params = addParameters(
                Parameters.with("term", "%" + term + "%"),
                criteriasDTO
        );

        return
                find(query, Sort.by("p." + sort, direction, Sort.NullPrecedence.NULLS_LAST), params)
                        .page(page)
                        .list()
                ;
    }
}