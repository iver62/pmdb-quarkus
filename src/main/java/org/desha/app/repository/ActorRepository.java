package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Actor;

import java.util.List;
import java.util.Objects;

@Named("actorRepository")
@ApplicationScoped
public class ActorRepository extends PersonRepository<Actor> {

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        String query = "FROM Actor p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + criteriasDTO.getTerm() + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    @Override
    public Uni<Actor> findByIdWithCountriesAndMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        StringBuilder query = new StringBuilder(
                "FROM Actor a " +
                        "JOIN FETCH a.movieActors ma " +
                        "JOIN FETCH ma.movie m " +
                        "WHERE a.id = :id " +
                        "AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))"
        );
        Parameters params = Parameters.with("id", id)
                .and("term", "%" + criteriasDTO.getTerm() + "%");

        if (Objects.nonNull(criteriasDTO.getFromReleaseDate())) {
            query.append(" AND m.releaseDate >= :fromReleaseDate");
            params.and("fromReleaseDate", criteriasDTO.getFromReleaseDate());
        }

        if (Objects.nonNull(criteriasDTO.getToReleaseDate())) {
            query.append(" AND m.releaseDate <= :toReleaseDate");
            params.and("toReleaseDate", criteriasDTO.getToReleaseDate());
        }

        if (Objects.nonNull(criteriasDTO.getFromCreationDate())) {
            query.append(" AND m.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", criteriasDTO.getFromCreationDate());
        }

        if (Objects.nonNull(criteriasDTO.getToCreationDate())) {
            query.append(" AND m.creationDate <= :toCreationDate");
            params.and("toCreationDate", criteriasDTO.getToCreationDate());
        }

        if (Objects.nonNull(criteriasDTO.getFromLastUpdate())) {
            query.append(" AND m.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", criteriasDTO.getFromLastUpdate());
        }

        if (Objects.nonNull(criteriasDTO.getToLastUpdate())) {
            query.append(" AND m.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", criteriasDTO.getToLastUpdate());
        }

        if (Objects.nonNull(criteriasDTO.getGenreIds()) && !criteriasDTO.getGenreIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.genres g WHERE g.id IN :genreIds)");
            params.and("genreIds", criteriasDTO.getGenreIds());
        }

        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", criteriasDTO.getCountryIds());
        }

        return
                find(query.toString(), params)
                        .firstResult()
                ;
    }

    public Uni<List<Actor>> find(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        String query = "FROM Actor p " +
                "LEFT JOIN FETCH p.movieActors ma " +
                "LEFT JOIN FETCH ma.movie m " +
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