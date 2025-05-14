package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.SoundEditor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SoundEditorRepository extends PersonRepository<SoundEditor> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = """
                        FROM SoundEditor p
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
    public Uni<SoundEditor> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return null;
    }

    @Override
    public Uni<List<SoundEditor>> findByName(String name) {
        String query = """
                FROM SoundEditor se
                LEFT JOIN FETCH se.countries
                WHERE LOWER(FUNCTION('unaccent', se.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;

        String term = Optional.ofNullable(name).orElse("");

        return find(query, Sort.by("name"), Parameters.with("term", "%" + term + "%"))
                .list();
    }

    public Uni<List<SoundEditor>> find(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = """
                FROM SoundEditor p
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
