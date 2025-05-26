package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.record.PersonWithMoviesNumber;
import org.desha.app.domain.record.Repartition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository implements PanacheRepositoryBase<Person, Long> {

    public Uni<Long> countAll() {
        return count();
    }

    public Uni<Long> countPersons(CriteriasDTO criteriasDTO) {
        String query = String.format("""
                     FROM Person p
                     WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriasDTO)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    public Uni<Long> countPersonsByMovie(Long id, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                FROM Person p
                JOIN MoviePerson mp ON p.id = mp.personId
                WHERE mp.movieId = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriasDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    public Uni<Long> countByCountry(Long id, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                FROM Person p
                JOIN p.countries c
                WHERE c.id = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriasDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    public Uni<Person> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return findById(id);
    }

    public Uni<List<Person>> findByName(String name) {
        return findAll().list();
    }

    public Uni<List<Person>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    public Uni<List<Person>> findPersons(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                FROM Person p
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriasDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return find(query, params).page(page).list();
    }

    public Uni<List<Person>> findPersonsByMovie(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                SELECT p
                FROM Person p
                JOIN MoviePerson mp ON p.id = mp.personId
                WHERE mp.movieId = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriasDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return find(query, params).page(page).list();
    }

    public Uni<List<PersonWithMoviesNumber>> findPersonsWithMoviesNumber(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                SELECT p, COALESCE((SELECT moviesNumber FROM PersonMoviesNumber pmn WHERE pmn.personId = p.id), 0) AS moviesNumber, COUNT(a) AS awardsNumber
                FROM Person p
                LEFT JOIN p.awardSet a
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                GROUP BY p
                %s
                """, addClauses(criteriasDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return find(query, params).page(page).project(PersonWithMoviesNumber.class).list();
    }

    public Uni<List<Person>> findPersonsByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                FROM Person p
                JOIN p.countries c
                WHERE c.id = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriasDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        Sort finalSort = Sort.by(sort, direction, Sort.NullPrecedence.NULLS_LAST);

        return find(query, finalSort, params)
                .page(page)
                .list();
    }

    public Uni<List<Repartition>> findActorsCreationDateEvolution() {
        return
                find("""
                        SELECT CAST(FUNCTION('TO_CHAR', p.creationDate, 'MM-YYYY') AS string) AS mois_creation,
                            SUM(COUNT(*)) OVER (ORDER BY FUNCTION('TO_CHAR', p.creationDate, 'MM-YYYY')) AS cumulative_count
                        FROM Person p
                        WHERE ?1 MEMBER OF p.types
                        GROUP BY mois_creation
                        ORDER BY mois_creation
                        """, PersonType.ACTOR
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    private String addSort(String sort, Sort.Direction direction) {
        if (StringUtils.isEmpty(sort)) return "";

        String dir = (direction == Sort.Direction.Ascending) ? "ASC" : "DESC";

        // Si le critère de tri est le nombre de films
        if ("moviesCount".equals(sort)) {
            return String.format(" ORDER BY moviesNumber %s", dir);
        }

        // Si le critère de tri est le nombre de récompenses
        if ("awardsCount".equals(sort)) {
            return String.format(" ORDER BY awardsNumber %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        List<String> allowedFields = Person.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN p.%s IS NULL THEN 1 ELSE 0 END, p.%s %s", sort, sort, dir);
    }

    private String addClauses(CriteriasDTO criteriasDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(criteriasDTO.getFromBirthDate()).ifPresent(date -> query.append(" AND p.dateOfBirth >= :fromBirthDate"));
        Optional.ofNullable(criteriasDTO.getToBirthDate()).ifPresent(date -> query.append(" AND p.dateOfBirth <= :toBirthDate"));
        Optional.ofNullable(criteriasDTO.getFromDeathDate()).ifPresent(date -> query.append(" AND p.dateOfDeath >= :fromDeathDate"));
        Optional.ofNullable(criteriasDTO.getToDeathDate()).ifPresent(date -> query.append(" AND p.dateOfDeath <= :toDeathDate"));
        Optional.ofNullable(criteriasDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND p.creationDate >= :fromCreationDate"));
        Optional.ofNullable(criteriasDTO.getToCreationDate()).ifPresent(date -> query.append(" AND p.creationDate <= :toCreationDate"));
        Optional.ofNullable(criteriasDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND p.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(criteriasDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND p.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.countries c WHERE c.id IN :countryIds)");
        }

        if (Objects.nonNull(criteriasDTO.getPersonTypes()) && !criteriasDTO.getPersonTypes().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.types t WHERE t IN :personTypes)");
        }

        return query.toString();
    }

    private Parameters addParameters(Parameters params, CriteriasDTO criteriasDTO) {
        if (Objects.nonNull(criteriasDTO.getFromBirthDate())) {
            params.and("fromBirthDate", criteriasDTO.getFromBirthDate());
        }
        if (Objects.nonNull(criteriasDTO.getToBirthDate())) {
            params.and("toBirthDate", criteriasDTO.getToBirthDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromDeathDate())) {
            params.and("fromDeathDate", criteriasDTO.getFromDeathDate());
        }
        if (Objects.nonNull(criteriasDTO.getToDeathDate())) {
            params.and("toDeathDate", criteriasDTO.getToDeathDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromCreationDate())) {
            params.and("fromCreationDate", criteriasDTO.getFromCreationDate());
        }
        if (Objects.nonNull(criteriasDTO.getToCreationDate())) {
            params.and("toCreationDate", criteriasDTO.getToCreationDate());
        }
        if (Objects.nonNull(criteriasDTO.getFromLastUpdate())) {
            params.and("fromLastUpdate", criteriasDTO.getFromLastUpdate());
        }
        if (Objects.nonNull(criteriasDTO.getToLastUpdate())) {
            params.and("toLastUpdate", criteriasDTO.getToLastUpdate());
        }
        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", criteriasDTO.getCountryIds());
        }
        if (Objects.nonNull(criteriasDTO.getPersonTypes()) && !criteriasDTO.getPersonTypes().isEmpty()) {
            params.and("personTypes", criteriasDTO.getPersonTypes());
        }
        return params;
    }
}
