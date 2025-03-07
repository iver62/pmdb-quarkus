package org.desha.app.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.entity.Actor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Named("actorRepository")
@ApplicationScoped
public class ActorRepository extends PersonRepository<Actor> {

    public Uni<Long> count(
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        StringBuilder query = new StringBuilder("FROM Actor a WHERE LOWER(FUNCTION('unaccent', a.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND a.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND a.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND a.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND a.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND a.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND a.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND a.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND a.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM a.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return count(query.toString(), params);
    }

    @Override
    public Uni<Actor> findByIdWithCountriesAndMovies(long id, Page page, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        StringBuilder query = new StringBuilder(
                "FROM Actor a " +
                        "JOIN FETCH a.movieActors ma " +
                        "JOIN FETCH ma.movie m " +
                        "WHERE a.id = :id " +
                        "AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))"
        );
        Parameters params = Parameters.with("id", id)
                .and("term", "%" + filtersDTO.getTerm() + "%");

        if (Objects.nonNull(filtersDTO.getFromReleaseDate())) {
            query.append(" AND m.releaseDate >= :fromReleaseDate");
            params.and("fromReleaseDate",filtersDTO.getFromReleaseDate());
        }

        if (Objects.nonNull(filtersDTO.getToReleaseDate())) {
            query.append(" AND m.releaseDate <= :toReleaseDate");
            params.and("toReleaseDate", filtersDTO.getToReleaseDate());
        }

        if (Objects.nonNull(filtersDTO.getFromCreationDate())) {
            query.append(" AND m.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", filtersDTO.getFromCreationDate());
        }

        if (Objects.nonNull(filtersDTO.getToCreationDate())) {
            query.append(" AND m.creationDate <= :toCreationDate");
            params.and("toCreationDate",filtersDTO.getToCreationDate());
        }

        if (Objects.nonNull(filtersDTO.getFromLastUpdate())) {
            query.append(" AND m.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", filtersDTO.getFromLastUpdate());
        }

        if (Objects.nonNull(filtersDTO.getToLastUpdate())) {
            query.append(" AND m.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", filtersDTO.getToLastUpdate());
        }

        if (Objects.nonNull(filtersDTO.getGenreIds()) && !filtersDTO.getGenreIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.genres g WHERE g.id IN :genreIds)");
            params.and("genreIds", filtersDTO.getGenreIds());
        }

        if (Objects.nonNull(filtersDTO.getCountryIds()) && !filtersDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", filtersDTO.getCountryIds());
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
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        StringBuilder query = new StringBuilder("FROM Actor a WHERE LOWER(FUNCTION('unaccent', a.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND a.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND a.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND a.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND a.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND a.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND a.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND a.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND a.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM a.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return
                find(query.toString(), Sort.by(sort, direction), params)
                        .page(page)
                        .list()
                ;
    }
}