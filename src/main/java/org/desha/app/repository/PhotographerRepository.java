package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Photographer;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PhotographerRepository extends PersonRepository<Photographer> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = """
                FROM Photographer p
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
    public Uni<Photographer> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<Photographer>> findByName(String name) {
        String query = """
                FROM Photographer p
                LEFT JOIN FETCH p.countries
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;

        String term = Optional.ofNullable(name).orElse("");

        return find(query, Sort.by("name"), Parameters.with("name", "%" + term + "%"))
                .list();
    }

    public Uni<List<Photographer>> find(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = """
                FROM Photographer p
                LEFT JOIN FETCH p.movies
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO) + addSort(sort, direction);

        String term = Optional.ofNullable(criteriasDTO.getTerm()).orElse("");

        Parameters params = addParameters(
                Parameters.with("term", "%" + term + "%"),
                criteriasDTO
        );

        return find(query, params).page(page).list();
    }
}
