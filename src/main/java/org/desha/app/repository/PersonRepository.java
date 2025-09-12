package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.domain.record.PersonWithMoviesNumber;
import org.desha.app.domain.record.Repartition;

import java.util.*;

@ApplicationScoped
public class PersonRepository implements PanacheRepositoryBase<Person, Long> {

    public Uni<Long> countAll() {
        return count();
    }

    public Uni<Long> countPersons(CriteriaDTO criteriaDTO) {
        String query = String.format("""
                     FROM Person p
                     WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriaDTO)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return count(query, params);
    }

    public Uni<Long> countPersonsByMovie(Long id, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                FROM Person p
                JOIN MoviePerson mp ON p.id = mp.personId
                WHERE mp.movieId = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriaDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return count(query, params);
    }

    public Uni<Long> countByCountry(Long id, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                FROM Person p
                JOIN p.countries c
                WHERE c.id = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriaDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return count(query, params);
    }

    public Uni<List<Person>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    public Uni<List<Person>> findPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                FROM Person p
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return find(query, params).page(page).list();
    }

    public Uni<List<Person>> findPersonsByMovie(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                SELECT p
                FROM Person p
                JOIN MoviePerson mp ON p.id = mp.personId
                WHERE mp.movieId = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return find(query, params).page(page).list();
    }

    public Uni<List<PersonWithMoviesNumber>> findPersonsWithMoviesNumber(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                SELECT p, COALESCE((SELECT moviesNumber FROM PersonMoviesNumber pmn WHERE pmn.personId = p.id), 0) AS moviesNumber, COUNT(a) AS awardsNumber
                FROM Person p
                LEFT JOIN p.awards a
                WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                GROUP BY p
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return
                find(query, params)
                        .page(page)
                        .project(PersonWithMoviesNumber.class)
                        .list()
                ;
    }

    public Uni<List<Person>> findPersonsByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                FROM Person p
                JOIN p.countries c
                WHERE c.id = :id
                    AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriaDTO)
        );

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        Sort finalSort = Sort.by(sort, direction, Sort.NullPrecedence.NULLS_LAST);

        return find(query, finalSort, params)
                .page(page)
                .list();
    }

    public Uni<List<Repartition>> findActorsCreationDateEvolution() {
        return
                find("""
                         SELECT
                            mois_creation,
                            SUM(monthly_count) OVER (ORDER BY mois_creation) AS cumulative_count
                        FROM (
                            SELECT
                                TO_CHAR(p.creationDate, 'MM-YYYY') AS mois_creation,
                                COUNT(*) AS monthly_count
                            FROM Person p
                            WHERE ?1 MEMBER OF p.types
                            GROUP BY TO_CHAR(p.creationDate, 'MM-YYYY')
                        ) AS sub
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
        Set<String> allowedFields = Person.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN p.%s IS NULL THEN 1 ELSE 0 END, p.%s %s", sort, sort, dir);
    }

    private String addClauses(CriteriaDTO criteriaDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(criteriaDTO.getFromBirthDate()).ifPresent(date -> query.append(" AND p.dateOfBirth >= :fromBirthDate"));
        Optional.ofNullable(criteriaDTO.getToBirthDate()).ifPresent(date -> query.append(" AND p.dateOfBirth <= :toBirthDate"));
        Optional.ofNullable(criteriaDTO.getFromDeathDate()).ifPresent(date -> query.append(" AND p.dateOfDeath >= :fromDeathDate"));
        Optional.ofNullable(criteriaDTO.getToDeathDate()).ifPresent(date -> query.append(" AND p.dateOfDeath <= :toDeathDate"));
        Optional.ofNullable(criteriaDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND p.creationDate >= :fromCreationDate"));
        Optional.ofNullable(criteriaDTO.getToCreationDate()).ifPresent(date -> query.append(" AND p.creationDate <= :toCreationDate"));
        Optional.ofNullable(criteriaDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND p.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(criteriaDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND p.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(criteriaDTO.getCountryIds()) && !criteriaDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.countries c WHERE c.id IN :countryIds)");
        }

        if (Objects.nonNull(criteriaDTO.getPersonTypes()) && !criteriaDTO.getPersonTypes().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.types t WHERE t IN :personTypes)");
        }

        return query.toString();
    }

    private Parameters addParameters(Parameters params, CriteriaDTO criteriaDTO) {
        if (Objects.nonNull(criteriaDTO.getFromBirthDate())) {
            params.and("fromBirthDate", criteriaDTO.getFromBirthDate());
        }
        if (Objects.nonNull(criteriaDTO.getToBirthDate())) {
            params.and("toBirthDate", criteriaDTO.getToBirthDate());
        }
        if (Objects.nonNull(criteriaDTO.getFromDeathDate())) {
            params.and("fromDeathDate", criteriaDTO.getFromDeathDate());
        }
        if (Objects.nonNull(criteriaDTO.getToDeathDate())) {
            params.and("toDeathDate", criteriaDTO.getToDeathDate());
        }
        if (Objects.nonNull(criteriaDTO.getFromCreationDate())) {
            params.and("fromCreationDate", criteriaDTO.getFromCreationDate());
        }
        if (Objects.nonNull(criteriaDTO.getToCreationDate())) {
            params.and("toCreationDate", criteriaDTO.getToCreationDate());
        }
        if (Objects.nonNull(criteriaDTO.getFromLastUpdate())) {
            params.and("fromLastUpdate", criteriaDTO.getFromLastUpdate());
        }
        if (Objects.nonNull(criteriaDTO.getToLastUpdate())) {
            params.and("toLastUpdate", criteriaDTO.getToLastUpdate());
        }
        if (Objects.nonNull(criteriaDTO.getCountryIds()) && !criteriaDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", criteriaDTO.getCountryIds());
        }
        if (Objects.nonNull(criteriaDTO.getPersonTypes()) && !criteriaDTO.getPersonTypes().isEmpty()) {
            params.and("personTypes", criteriaDTO.getPersonTypes());
        }
        return params;
    }
}
