package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.domain.record.PersonWithMoviesNumber;
import org.desha.app.domain.record.Repartition;

import java.util.*;

@ApplicationScoped
public class PersonRepository implements PanacheRepositoryBase<Person, Long> {

    /**
     * Compte le nombre de personnes correspondant à un terme donné.
     * <p>
     * Si le paramètre {@link CriteriaDTO#getTerm()} est {@code null}, la méthode retourne le nombre total de personnes existantes.
     * Si un terme est fourni, elle compte uniquement les personnes dont le nom correspond (en ignorant les accents et la casse).
     *
     * @param criteriaDTO Les critères de recherche, notamment le terme {@code term} pour filtrer les personnes.
     *                    Peut être {@code null} pour compter toutes les personnes.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant au critère.
     */
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

    /**
     * Compte le nombre de personnes associées à un film donné correspondant à un terme de recherche.
     * <p>
     * Si le paramètre {@link CriteriaDTO#getTerm()} est {@code null}, la méthode retourne le nombre total de personnes
     * associées au film spécifié par {@code id}.
     * Si un terme est fourni, elle compte uniquement les personnes dont le nom correspond (en ignorant les accents et la casse)
     * et qui sont liées au film.
     *
     * @param id          L'identifiant unique du film pour lequel compter les personnes.
     * @param criteriaDTO Les critères de recherche, notamment le terme {@code term} pour filtrer les personnes.
     *                    Peut être {@code null} pour compter toutes les personnes liées au film.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant au critère pour le film spécifié.
     */
    public Uni<Long> countPersonsByMovie(@NotNull Long id, CriteriaDTO criteriaDTO) {
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

    /**
     * Compte le nombre de personnes associées à un pays donné correspondant à un terme de recherche.
     * <p>
     * Si le paramètre {@link CriteriaDTO#getTerm()} est {@code null}, la méthode retourne le nombre total de personnes
     * associées au pays spécifié par {@code id}.
     * Si un terme est fourni, elle compte uniquement les personnes dont le nom correspond (en ignorant les accents et la casse)
     * et qui sont liées au pays.
     *
     * @param id          L'identifiant unique du pays pour lequel compter les personnes.
     * @param criteriaDTO Les critères de recherche, notamment le terme {@code term} pour filtrer les personnes.
     *                    Peut être {@code null} pour compter toutes les personnes liées au pays.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant au critère pour le pays spécifié.
     */
    public Uni<Long> countPersonsByCountry(@NotNull Long id, CriteriaDTO criteriaDTO) {
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

    /**
     * Récupère une liste de personnes correspondant aux identifiants fournis.
     * <p>
     * Si la liste {@code ids} est {@code null} ou vide, la méthode retourne une liste vide.
     * Sinon, elle renvoie toutes les personnes dont l'identifiant figure dans la liste {@code ids}.
     * <p>
     * Cette méthode nève pas d'exception si aucun identifiant n'est trouvé, elle retourne simplement une liste vide.
     *
     * @param ids La liste des identifiants de personnes à récupérer. Peut être {@code null} ou vide.
     * @return Un {@link Uni} contenant la liste des {@link Person} correspondant aux identifiants fournis.
     */
    public Uni<List<Person>> findByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        }
        return list("id IN ?1", ids);
    }

    /**
     * Recherche et retourne une liste de personnes correspondant aux critères fournis, avec pagination et tri.
     * <p>
     * La recherche se fait sur le nom des personnes, en ignorant la casse et les accents.
     * Les critères supplémentaires peuvent être passés via {@link CriteriaDTO}.
     * Le tri et la pagination sont appliqués selon les paramètres fournis.
     * <p>
     * Si aucun résultat n’est trouvé, la méthode retourne une liste vide.
     *
     * @param page        La page à récupérer (numéro de page et taille de page) pour la pagination.
     * @param sort        Le champ sur lequel trier les résultats. Doit appartenir aux champs autorisés.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage supplémentaires. Peut être {@code null} pour ne pas filtrer.
     * @return Un {@link Uni} contenant la liste des {@link Person} correspondant aux critères.
     */
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

    /**
     * Recherche et retourne une liste de personnes associées à un film spécifique, avec pagination et tri.
     * <p>
     * La recherche se fait sur le nom des personnes, en ignorant la casse et les accents.
     * Les critères supplémentaires peuvent être passés via {@link CriteriaDTO}.
     * Le tri et la pagination sont appliqués selon les paramètres fournis.
     * <p>
     * Si aucun résultat n’est trouvé, la méthode retourne une liste vide.
     *
     * @param id          L'identifiant du film pour lequel récupérer les personnes.
     * @param page        La page à récupérer (numéro de page et taille de page) pour la pagination.
     * @param sort        Le champ sur lequel trier les résultats. Doit appartenir aux champs autorisés.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage supplémentaires. Peut être {@code null} pour ne pas filtrer.
     * @return Un {@link Uni} contenant la liste des {@link Person} associées au film correspondant aux critères.
     */
    public Uni<List<Person>> findPersonsByMovie(@NotNull Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
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

    /**
     * Recherche et retourne une liste de personnes avec le nombre de films associés et le nombre de récompenses,
     * en appliquant la pagination et le tri spécifiés.
     * <p>
     * La recherche se fait sur le nom des personnes, en ignorant la casse et les accents.
     * Le nombre de films par personne est récupéré via la table {@code PersonMoviesNumber}.
     * Le nombre de récompenses est calculé via un {@code LEFT JOIN} sur la relation {@code awards}.
     * <p>
     * Si aucun résultat n’est trouvé, la méthode retourne une liste vide.
     *
     * @param page        La page à récupérer (numéro de page et taille de page) pour la pagination.
     * @param sort        Le champ sur lequel trier les résultats. Doit appartenir aux champs autorisés.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage supplémentaires. Peut être {@code null} pour ne pas filtrer.
     * @return Un {@link Uni} contenant la liste des {@link PersonWithMoviesNumber} correspondant aux critères.
     */
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

    /**
     * Recherche et retourne une liste de personnes associées à un pays donné, en appliquant la pagination et le tri spécifiés.
     * <p>
     * La recherche se fait sur le nom des personnes, en ignorant la casse et les accents.
     * Si aucun résultat n’est trouvé, la méthode retourne une liste vide.
     *
     * @param id          L'identifiant du pays pour lequel rechercher les personnes. Ne peut pas être {@code null}.
     * @param page        La page à récupérer (numéro de page et taille de page) pour la pagination.
     * @param sort        Le champ sur lequel trier les résultats. Doit appartenir aux champs autorisés.
     * @param direction   La direction du tri ({@link Sort.Direction#Ascending} ou {@link Sort.Direction#Descending}).
     * @param criteriaDTO Les critères de filtrage supplémentaires. Peut être {@code null} pour ne pas filtrer.
     * @return Un {@link Uni} contenant la liste des {@link Person} correspondant au critère de pays et aux filtres.
     */
    public Uni<List<Person>> findPersonsByCountry(@NotNull Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
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

    /**
     * Retourne l'évolution cumulée du nombre de personnes de type {@link PersonType#ACTOR} créées par mois.
     * <p>
     * Chaque enregistrement contient le mois (format "MM-YYYY") et le nombre cumulatif d'acteurs créés jusqu'à ce mois.
     * La méthode utilise la fonction SQL de fenêtrage {@code SUM(...) OVER (ORDER BY mois_creation)} pour calculer le cumul.
     * <p>
     * Si aucun acteur n’a été créé, la méthode retourne une liste vide.
     *
     * @return Un {@link Uni} contenant une liste de {@link Repartition}, chaque élément représentant
     * le mois et le nombre cumulé d’acteurs créés.
     */
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
