package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Person;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class PersonRepository<T extends Person> implements PanacheRepository<T> {

    public abstract Uni<Long> count(CriteriasDTO criteriasDTO);

    public Uni<Long> countByCountry(Class<T> entityClass, Long id, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                        FROM %s p
                        JOIN p.countries c
                        WHERE c.id = :id
                            AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        %s
                        """,
                entityClass.getSimpleName(),
                addClauses(criteriasDTO)
        );

        String term = Optional.ofNullable(criteriasDTO.getTerm()).orElse("");

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + term + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    public abstract Uni<T> findByIdWithMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO);

    public abstract Uni<List<T>> findByName(String name);

    public Uni<List<T>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    public abstract Uni<List<T>> find(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO);

    public Uni<List<T>> findByCountry(Class<T> entityClass, Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = String.format("""
                        FROM %s p
                        JOIN p.countries c
                        WHERE c.id = :id
                            AND LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))
                        %s
                        """,
                entityClass.getSimpleName(),
                addClauses(criteriasDTO)
        );

        String term = Optional.ofNullable(criteriasDTO.getTerm()).orElse("");

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + term + "%"),
                criteriasDTO
        );

        Sort finalSort = Sort.by(sort, direction, Sort.NullPrecedence.NULLS_LAST);

        return find(query, finalSort, params)
                .page(page)
                .list();
    }

    protected String addSort(String sort, Sort.Direction direction) {
        if (StringUtils.isEmpty(sort)) return "";

        String dir = (direction == Sort.Direction.Ascending) ? "ASC" : "DESC";

        // Si le critère de tri est le nombre de films
        if ("moviesCount".equals(sort)) {
            return String.format(" ORDER BY SIZE(p.movies) %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        List<String> allowedFields = Person.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN p.%s IS NULL THEN 1 ELSE 0 END, p.%s %s", sort, sort, dir);
    }

    protected String addClauses(CriteriasDTO criteriasDTO) {
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

        return query.toString();
    }

    protected Parameters addParameters(Parameters params, CriteriasDTO criteriasDTO) {
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
        return params;
    }
}
