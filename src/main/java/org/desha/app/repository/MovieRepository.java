package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.MovieWithAwardsNumber;
import org.desha.app.domain.record.Repartition;
import org.desha.app.helper.MovieRepositoryHelper;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    public Uni<Long> countMovies(CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                FROM Movie m
                WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, addClauses(criteriaDTO)
        );

        final Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByPerson(Person person, CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                       FROM Movie m
                       WHERE (%s)
                         AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                """, MovieRepositoryHelper.buildExistsClause(person), addClauses(criteriaDTO)
        );

        Parameters params = addParameters(
                Parameters.with("person", person)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByCountry(Long id, String term) {
        return
                count("""
                                SELECT COUNT(m)
                                FROM Movie m
                                JOIN m.countries c
                                WHERE c.id = :id
                                    AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                                """,
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                );
    }

    public Uni<Long> countMoviesByCategory(Long id, String term) {
        return
                count("""
                                SELECT COUNT(m)
                                FROM Movie m
                                JOIN m.categories c
                                WHERE c.id = :id
                                    AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                                """,
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                );
    }

    public Uni<Boolean> movieExists(String title, @Nullable String originalTitle) {
        if (StringUtils.isEmpty(originalTitle)) {
            return find("LOWER(FUNCTION('unaccent', title)) = LOWER(FUNCTION('unaccent', ?1))", title.trim())
                    .firstResult()
                    .map(Objects::nonNull); // Retourne true si un film existe déjà
        }
        return find("LOWER(FUNCTION('unaccent', title)) = LOWER(FUNCTION('unaccent', ?1)) AND LOWER(FUNCTION('unaccent', originalTitle)) = LOWER(FUNCTION('unaccent', ?2))", title.trim(), originalTitle.trim())
                .firstResult()
                .map(Objects::nonNull); // Retourne true si un film existe déjà
    }

    /**
     * Recherche un film par son identifiant.
     *
     * @param id L'identifiant du film recherché.
     * @return Une instance de {@link Uni} contenant le film trouvé.
     */
    @Override
    public Uni<Movie> findById(Long id) {
        return find("id", id).firstResult();
    }

    public Uni<Movie> findByIdWithCountriesAndCategories(Long id) {
        return
                find("""
                        SELECT DISTINCT m
                        FROM Movie m
                        LEFT JOIN FETCH m.countries
                        LEFT JOIN FETCH m.categories
                        WHERE m.id = ?1
                        """, id
                ).firstResult();
    }

    public Uni<Movie> findByIdWithTechnicalTeam(Long id) {
        return
                findById(id)
                        .call(movie ->
                                Mutiny.fetch(movie.getMovieProducers()).invoke(movie::setMovieProducers)
                                        .call(() -> Mutiny.fetch(movie.getMovieDirectors()).invoke(movie::setMovieDirectors))
                                        .call(() -> Mutiny.fetch(movie.getMovieAssistantDirectors()).invoke(movie::setMovieAssistantDirectors))
                                        .call(() -> Mutiny.fetch(movie.getMovieScreenwriters()).invoke(movie::setMovieScreenwriters))
                                        .call(() -> Mutiny.fetch(movie.getMovieComposers()).invoke(movie::setMovieComposers))
                                        .call(() -> Mutiny.fetch(movie.getMovieMusicians()).invoke(movie::setMovieMusicians))
                                        .call(() -> Mutiny.fetch(movie.getMoviePhotographers()).invoke(movie::setMoviePhotographers))
                                        .call(() -> Mutiny.fetch(movie.getMovieCostumeDesigners()).invoke(movie::setMovieCostumeDesigners))
                                        .call(() -> Mutiny.fetch(movie.getMovieSetDesigners()).invoke(movie::setMovieSetDesigners))
                                        .call(() -> Mutiny.fetch(movie.getMovieEditors()).invoke(movie::setMovieEditors))
                                        .call(() -> Mutiny.fetch(movie.getMovieCasters()).invoke(movie::setMovieCasters))
                                        .call(() -> Mutiny.fetch(movie.getMovieArtists()).invoke(movie::setMovieArtists))
                                        .call(() -> Mutiny.fetch(movie.getMovieSoundEditors()).invoke(movie::setMovieSoundEditors))
                                        .call(() -> Mutiny.fetch(movie.getMovieVfxSupervisors()).invoke(movie::setMovieVfxSupervisors))
                                        .call(() -> Mutiny.fetch(movie.getMovieSfxSupervisors()).invoke(movie::setMovieSfxSupervisors))
                                        .call(() -> Mutiny.fetch(movie.getMovieMakeupArtists()).invoke(movie::setMovieMakeupArtists))
                                        .call(() -> Mutiny.fetch(movie.getMovieHairDressers()).invoke(movie::setMovieHairDressers))
                                        .call(() -> Mutiny.fetch(movie.getMovieStuntmen()).invoke(movie::setMovieStuntmen))
                        )
                ;
    }

    public Uni<List<MovieWithAwardsNumber>> findMovies(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                       SELECT m, COALESCE((SELECT awardsNumber FROM MovieAwardsNumber man WHERE man.movieId = m.id), 0) AS awardsNumber
                       FROM Movie m
                       WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        final Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return
                find(query, params)
                        .page(page)
                        .project(MovieWithAwardsNumber.class)
                        .list()
                ;
    }

    public Uni<List<MovieWithAwardsNumber>> findMovies(String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        String query = String.format("""
                SELECT m, COALESCE((SELECT awardsNumber FROM MovieAwardsNumber man WHERE man.movieId = m.id), 0) AS awardsNumber
                FROM Movie m
                WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return
                find(query, params)
                        .project(MovieWithAwardsNumber.class)
                        .list()
                ;
    }

    public Uni<List<MovieWithAwardsNumber>> findMoviesByPerson(Person person, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                       SELECT m, COALESCE((SELECT awardsNumber FROM MovieAwardsNumber man WHERE man.movieId = m.id), 0) AS awardsNumber
                       FROM Movie m
                       WHERE (%s)
                         AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, MovieRepositoryHelper.buildExistsClause(person), addClauses(criteriaDTO), addSort(sort, direction)
        );

        Parameters params = addParameters(
                Parameters.with("person", person)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return
                find(query, params)
                        .project(MovieWithAwardsNumber.class)
                        .page(page)
                        .list()
                ;
    }

    public Uni<List<MovieWithAwardsNumber>> findMoviesByCountry(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                       SELECT m, COALESCE((SELECT awardsNumber FROM MovieAwardsNumber man WHERE man.movieId = m.id), 0) AS awardsNumber
                       FROM Movie m
                       JOIN m.countries c
                       WHERE c.id = :id
                         AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        final Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return find(query, params)
                .page(page)
                .project(MovieWithAwardsNumber.class)
                .list()
                ;
    }

    public Uni<List<MovieWithAwardsNumber>> findMoviesByCategory(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        final String query = String.format("""
                       SELECT m, COALESCE((SELECT awardsNumber FROM MovieAwardsNumber man WHERE man.movieId = m.id), 0) AS awardsNumber
                       FROM Movie m
                       JOIN m.categories c
                       WHERE c.id = :id
                         AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                %s
                %s
                """, addClauses(criteriaDTO), addSort(sort, direction)
        );

        final Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + StringUtils.defaultString(criteriaDTO.getTerm()) + "%"),
                criteriaDTO
        );

        return
                find(query, params)
                        .page(page)
                        .project(MovieWithAwardsNumber.class)
                        .list()
                ;
    }

    public Uni<List<Repartition>> findMoviesCreationDateEvolution() {
        return
                find("""
                        SELECT
                            mois_creation,
                            SUM(monthly_count) OVER (ORDER BY mois_creation) AS cumulative_count
                        FROM (
                            SELECT
                                TO_CHAR(m.creationDate, 'MM-YYYY') AS mois_creation,
                                COUNT(*) AS monthly_count
                            FROM Movie m
                            GROUP BY TO_CHAR(m.creationDate, 'MM-YYYY')
                        ) AS sub
                        ORDER BY mois_creation
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    public Uni<List<Repartition>> findMoviesByCreationDateRepartition() {
        return
                find("""
                        SELECT CAST(FUNCTION('TO_CHAR', m.creationDate, 'MM-YYYY') AS string) AS mois_creation, COUNT(m)
                        FROM Movie m
                        GROUP BY mois_creation
                        ORDER BY mois_creation
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    public Uni<List<Repartition>> findMoviesByReleaseDateRepartition() {
        return
                find("""
                        SELECT CAST((YEAR(m.releaseDate) - MOD(YEAR(m.releaseDate), 10)) AS string) AS decade, COUNT(m)
                        FROM Movie m
                        GROUP BY decade
                        ORDER BY decade
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    public Uni<Map<Country, Long>> findMoviesByCountryRepartitionBis() {
        return
                find("select distinct m from Movie m join fetch m.countries")
                        .list()
                        .map(movies ->
                                movies.stream()
                                        .flatMap(movie -> movie.getCountries().stream())
                                        .collect(Collectors.groupingBy(
                                                country -> country,
                                                Collectors.counting()
                                        ))
                        )
                        .map(map -> map.entrySet().stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new))
                        )
                ;
    }

    public Uni<List<Repartition>> findMoviesByCategoryRepartition() {
        return
                find("""
                        SELECT c.name, COUNT(m)
                        FROM Movie m
                        JOIN m.categories c
                        GROUP BY c.name
                        ORDER BY COUNT(m) DESC
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    public Uni<List<CountryRepartition>> findMoviesByCountryRepartition() {
        return
                find("""
                        SELECT c, COUNT(m)
                        FROM Movie m
                        JOIN m.countries c
                        GROUP BY c.id
                        ORDER BY COUNT(m) DESC
                        """
                )
                        .project(CountryRepartition.class)
                        .list()
                ;
    }

    public Uni<List<Repartition>> findMoviesByUserRepartition() {
        return
                find("""
                        SELECT u.username, COUNT(m)
                        FROM Movie m
                        JOIN m.user u
                        GROUP BY u.username
                        ORDER BY COUNT(m) DESC
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    private String addSort(String sort, Sort.Direction direction) {
        if (StringUtils.isEmpty(sort)) return "";

        String dir = (direction == Sort.Direction.Ascending) ? "ASC" : "DESC";

        // Si le critère de tri est le nombre de récompenses
        if (Objects.equals("awardsCount", sort)) {
            return String.format(" ORDER BY awardsNumber %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        Set<String> allowedFields = Movie.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN m.%s IS NULL THEN 1 ELSE 0 END, m.%s %s", sort, sort, dir);
    }

    private String addClauses(CriteriaDTO criteriaDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(criteriaDTO.getFromReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate >= :fromReleaseDate"));
        Optional.ofNullable(criteriaDTO.getToReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate <= :toReleaseDate"));
        Optional.ofNullable(criteriaDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND m.creationDate >= :fromCreationDate"));
        Optional.ofNullable(criteriaDTO.getToCreationDate()).ifPresent(date -> query.append(" AND m.creationDate <= :toCreationDate"));
        Optional.ofNullable(criteriaDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(criteriaDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(criteriaDTO.getCategoryIds()) && !criteriaDTO.getCategoryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.categories ca WHERE ca.id IN :categoryIds)");
        }

        if (Objects.nonNull(criteriaDTO.getCountryIds()) && !criteriaDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
        }

        if (Objects.nonNull(criteriaDTO.getUserIds()) && !criteriaDTO.getUserIds().isEmpty()) {
            query.append(" AND m.user.id IN :userIds");
        }

        return query.toString();
    }

    private Parameters addParameters(Parameters params, CriteriaDTO criteriaDTO) {
        if (Objects.nonNull(criteriaDTO.getFromReleaseDate())) {
            params.and("fromReleaseDate", criteriaDTO.getFromReleaseDate());
        }
        if (Objects.nonNull(criteriaDTO.getToReleaseDate())) {
            params.and("toReleaseDate", criteriaDTO.getToReleaseDate());
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

        if (Objects.nonNull(criteriaDTO.getCategoryIds()) && !criteriaDTO.getCategoryIds().isEmpty()) {
            params.and("categoryIds", criteriaDTO.getCategoryIds());
        }
        if (Objects.nonNull(criteriaDTO.getCountryIds()) && !criteriaDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", criteriaDTO.getCountryIds());
        }
        if (Objects.nonNull(criteriaDTO.getUserIds()) && !criteriaDTO.getUserIds().isEmpty()) {
            params.and("userIds", criteriaDTO.getUserIds());
        }

        return params;
    }
}
