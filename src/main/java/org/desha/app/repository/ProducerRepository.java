package org.desha.app.repository;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.entity.Producer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ProducerRepository extends PersonRepository<Producer> {

    /**
     * Compte le nombre de producteurs correspondant aux critères de recherche spécifiés.
     *
     * @param term             Terme de recherche appliqué au nom du producteur (insensible à la casse et aux accents).
     * @param countryIds       Liste des identifiants des pays associés aux producteurs recherchés.
     * @param fromBirthDate    Date minimale de naissance du producteur.
     * @param toBirthDate      Date maximale de naissance du producteur.
     * @param fromDeathDate    Date minimale de décès du producteur.
     * @param toDeathDate      Date maximale de décès du producteur.
     * @param fromCreationDate Date minimale de création de l'enregistrement du producteur.
     * @param toCreationDate   Date maximale de création de l'enregistrement du producteur.
     * @param fromLastUpdate   Date minimale de dernière mise à jour de l'enregistrement du producteur.
     * @param toLastUpdate     Date maximale de dernière mise à jour de l'enregistrement du producteur.
     * @return Un objet {@code Uni<Long>} représentant le nombre de producteurs correspondant aux critères.
     */
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
        StringBuilder query = new StringBuilder("FROM Producer p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND p.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND p.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND p.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND p.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND p.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND p.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND p.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND p.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return count(query.toString(), params);
    }

    @Override
    public Uni<Producer> findByIdWithCountriesAndMovies(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return null;
    }

    /**
     * Recherche une liste de producteurs en fonction des critères spécifiés.
     *
     * @param pageIndex        Index de la page à récupérer (utilisé pour la pagination).
     * @param size             Nombre d'éléments par page.
     * @param sort             Champ sur lequel appliquer le tri.
     * @param direction        Direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term             Terme de recherche appliqué au nom du producteur (insensible à la casse et aux accents).
     * @param countryIds       Liste des identifiants des pays associés aux producteurs recherchés.
     * @param fromBirthDate    Date minimale de naissance du producteur.
     * @param toBirthDate      Date maximale de naissance du producteur.
     * @param fromDeathDate    Date minimale de décès du producteur.
     * @param toDeathDate      Date maximale de décès du producteur.
     * @param fromCreationDate Date minimale de création de l'enregistrement du producteur.
     * @param toCreationDate   Date maximale de création de l'enregistrement du producteur.
     * @param fromLastUpdate   Date minimale de dernière mise à jour de l'enregistrement du producteur.
     * @param toLastUpdate     Date maximale de dernière mise à jour de l'enregistrement du producteur.
     * @return Un objet {@link Uni<List<Producer>>} contenant la liste des producteurs correspondant aux critères.
     */
    public Uni<List<Producer>> find(
            int pageIndex,
            int size,
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
        StringBuilder query = new StringBuilder("FROM Producer p WHERE LOWER(FUNCTION('unaccent', p.name)) LIKE LOWER(FUNCTION('unaccent', :term))");
        Parameters params = Parameters.with("term", "%" + term + "%");

        if (Objects.nonNull(fromBirthDate)) {
            query.append(" AND p.dateOfBirth >= :fromBirthDate");
            params.and("fromBirthDate", fromBirthDate);
        }

        if (Objects.nonNull(toBirthDate)) {
            query.append(" AND p.dateOfBirth <= :toBirthDate");
            params.and("toBirthDate", toBirthDate);
        }

        if (Objects.nonNull(fromDeathDate)) {
            query.append(" AND p.dateOfDeath >= :fromDeathDate");
            params.and("fromDeathDate", fromDeathDate);
        }

        if (Objects.nonNull(toDeathDate)) {
            query.append(" AND p.dateOfDeath <= :toDeathDate");
            params.and("toDeathDate", toDeathDate);
        }

        if (Objects.nonNull(fromCreationDate)) {
            query.append(" AND p.creationDate >= :fromCreationDate");
            params.and("fromCreationDate", fromCreationDate);
        }

        if (Objects.nonNull(toCreationDate)) {
            query.append(" AND p.creationDate <= :toCreationDate");
            params.and("toCreationDate", toCreationDate);
        }

        if (Objects.nonNull(fromLastUpdate)) {
            query.append(" AND p.lastUpdate >= :fromLastUpdate");
            params.and("fromLastUpdate", fromLastUpdate);
        }

        if (Objects.nonNull(toLastUpdate)) {
            query.append(" AND p.lastUpdate <= :toLastUpdate");
            params.and("toLastUpdate", toLastUpdate);
        }

        if (Objects.nonNull(countryIds) && !countryIds.isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM p.countries c WHERE c.id IN :countryIds)");
            params.and("countryIds", countryIds);
        }

        return
                find(query.toString(), Sort.by(sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }
}
