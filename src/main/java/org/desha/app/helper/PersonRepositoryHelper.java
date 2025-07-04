package org.desha.app.helper;

import io.quarkus.panache.common.Sort;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Person;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class PersonRepositoryHelper extends SqlHelper {

    public static String addSort(String sort, Sort.Direction direction) {
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

    public static String addClauses(CriteriasDTO criteriasDTO) {
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
}
