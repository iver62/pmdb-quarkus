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
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    public Uni<Long> countMovies(CriteriasDTO criteriasDTO) {
        String query = """
                FROM Movie m
                WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByPerson(Person person, CriteriasDTO criteriasDTO) {
        final String query = """
                FROM Movie m
                WHERE m.id IN :ids
                  AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO);

        return
                person.getAllRelatedMovies()
                        .map(movies -> movies.stream().map(Movie::getId).toList())
                        .onItem().transformToUni(movieIds -> {
                                    Parameters params = addParameters(
                                            Parameters.with("ids", movieIds)
                                                    .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                                            criteriasDTO
                                    );

                                    return count(query, params);
                                }
                        );
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

    public Uni<Long> countMoviesByGenre(Long id, String term) {
        return
                count("""
                                SELECT COUNT(m)
                                FROM Movie m
                                JOIN m.genres g
                                WHERE g.id = :id
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

    public Uni<List<Movie>> searchByTitle(String term) {
        String query = "LOWER(FUNCTION('unaccent', title)) LIKE LOWER(FUNCTION('unaccent', ?1))";

        return list(query, Sort.by("title"), "%" + term + "%");
    }

    public Uni<Movie> findByIdWithCountriesAndGenres(Long id) {
        return
                find("""
                        FROM Movie m
                        LEFT JOIN FETCH m.countries
                        LEFT JOIN FETCH m.genres
                        WHERE m.id = ?1
                        """, id
                ).firstResult();
    }

    public Uni<Movie> findByIdWithTechnicalTeam(Long id) {
        return
                findById(id)
                        .call(movie ->
                                Mutiny.fetch(movie.getProducers()).invoke(movie::setProducers)
                                        .call(() -> Mutiny.fetch(movie.getDirectors()).invoke(movie::setDirectors))
                                        .call(() -> Mutiny.fetch(movie.getScreenwriters()).invoke(movie::setScreenwriters))
                                        .call(() -> Mutiny.fetch(movie.getDialogueWriters()).invoke(movie::setDialogueWriters))
                                        .call(() -> Mutiny.fetch(movie.getMusicians()).invoke(movie::setMusicians))
                                        .call(() -> Mutiny.fetch(movie.getPhotographers()).invoke(movie::setPhotographers))
                                        .call(() -> Mutiny.fetch(movie.getCostumiers()).invoke(movie::setCostumiers))
                                        .call(() -> Mutiny.fetch(movie.getDecorators()).invoke(movie::setDecorators))
                                        .call(() -> Mutiny.fetch(movie.getEditors()).invoke(movie::setEditors))
                                        .call(() -> Mutiny.fetch(movie.getCasters()).invoke(movie::setCasters))
                                        .call(() -> Mutiny.fetch(movie.getArtDirectors()).invoke(movie::setArtDirectors))
                                        .call(() -> Mutiny.fetch(movie.getSoundEditors()).invoke(movie::setSoundEditors))
                                        .call(() -> Mutiny.fetch(movie.getVisualEffectsSupervisors()).invoke(movie::setVisualEffectsSupervisors))
                                        .call(() -> Mutiny.fetch(movie.getMakeupArtists()).invoke(movie::setMakeupArtists))
                                        .call(() -> Mutiny.fetch(movie.getHairDressers()).invoke(movie::setHairDressers))
                                        .call(() -> Mutiny.fetch(movie.getStuntmen()).invoke(movie::setStuntmen))
                        )
                ;
    }

    public Uni<List<Movie>> findMovies(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = """
                FROM Movie m
                LEFT JOIN FETCH m.awards
                WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO) + addSort(sort, direction);

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return find(query, params).page(page).list();
    }

    public Uni<List<Movie>> findMovies(String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        String query = """
                FROM Movie m
                LEFT JOIN FETCH m.awards
                WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                """ + addClauses(criteriasDTO) + addSort(sort, direction);

        Parameters params = addParameters(
                Parameters.with("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                criteriasDTO
        );

        return find(query, params).list();
    }

    public Uni<List<Movie>> findMoviesByPerson(Person person, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        final String body = """
                FROM Movie m
                LEFT JOIN FETCH m.awards
                WHERE m.id IN :ids
                  AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                """;

        return
                person.getAllRelatedMovies()
                        .map(movies -> movies.stream().map(Movie::getId).toList())
                        .onItem().transformToUni(movieIds -> {
                                    if (Objects.isNull(movieIds) || movieIds.isEmpty()) {
                                        return Uni.createFrom().item(List.of());
                                    }

                                    String query = body + addClauses(criteriasDTO) + addSort(sort, direction);

                                    Parameters params = addParameters(
                                            Parameters.with("ids", movieIds)
                                                    .and("term", "%" + StringUtils.defaultString(criteriasDTO.getTerm()) + "%"),
                                            criteriasDTO
                                    );

                                    return find(query, params).page(page).list();
                                }
                        );
    }

    public Uni<List<Movie>> findMoviesByCountry(Long id, String sort, Sort.Direction direction, String term) {
        return
                find("""
                                FROM Movie m
                                JOIN m.countries c
                                LEFT JOIN FETCH m.awards
                                WHERE c.id = :id
                                    AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                                """,
                        Sort.by(sort, direction, Sort.NullPrecedence.NULLS_LAST),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                ).list();
    }

    public Uni<List<Movie>> findMoviesByCountry(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                find("""
                                FROM Movie m
                                JOIN m.countries c
                                LEFT JOIN FETCH m.awards
                                WHERE c.id = :id
                                    AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                                """,
                        Sort.by(sort, direction, Sort.NullPrecedence.NULLS_LAST),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                )
                        .page(page)
                        .list();
    }

    public Uni<List<Movie>> findMoviesByGenre(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                find("""
                                FROM Movie m
                                JOIN m.genres g
                                LEFT JOIN FETCH m.awards
                                WHERE g.id = :id
                                    AND LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))
                                """,
                        Sort.by("m." + sort, direction, Sort.NullPrecedence.NULLS_LAST),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                )
                        .page(page)
                        .list()
                ;
    }

    public Uni<List<Repartition>> findMoviesCreationDateEvolution() {
        return
                find("""
                        SELECT CAST(FUNCTION('TO_CHAR', m.creationDate, 'MM-YYYY') AS string) AS mois_creation,
                            SUM(COUNT(*)) OVER (ORDER BY FUNCTION('TO_CHAR', m.creationDate, 'MM-YYYY')) AS cumulative_count
                        FROM Movie m
                        GROUP BY mois_creation
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

    public Uni<List<Repartition>> findMoviesByGenreRepartition() {
        return
                find("""
                        SELECT g.name, COUNT(m)
                        FROM Movie m
                        JOIN m.genres g
                        GROUP BY g.name
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
        if ("awardsCount".equals(sort)) {
            return String.format(" ORDER BY SIZE(m.awards) %s", dir);
        }

        // Protection basique contre injection ou champ non mappé
        List<String> allowedFields = Movie.ALLOWED_SORT_FIELDS;
        if (!allowedFields.contains(sort)) {
            throw new IllegalArgumentException("Champ de tri non autorisé : " + sort);
        }

        // Cas générique pour trier par un autre champ, avec gestion des NULL
        return String.format(" ORDER BY CASE WHEN m.%s IS NULL THEN 1 ELSE 0 END, m.%s %s", sort, sort, dir);
    }

    private String addClauses(CriteriasDTO criteriasDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(criteriasDTO.getFromReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate >= :fromReleaseDate"));
        Optional.ofNullable(criteriasDTO.getToReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate <= :toReleaseDate"));
        Optional.ofNullable(criteriasDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND m.creationDate >= :fromCreationDate"));
        Optional.ofNullable(criteriasDTO.getToCreationDate()).ifPresent(date -> query.append(" AND m.creationDate <= :toCreationDate"));
        Optional.ofNullable(criteriasDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(criteriasDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(criteriasDTO.getGenreIds()) && !criteriasDTO.getGenreIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.genres g WHERE g.id IN :genreIds)");
        }

        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
        }

        if (Objects.nonNull(criteriasDTO.getUserIds()) && !criteriasDTO.getUserIds().isEmpty()) {
            query.append(" AND m.user.id IN :userIds");
        }

        return query.toString();
    }

    private Parameters addParameters(Parameters params, CriteriasDTO criteriasDTO) {
        if (Objects.nonNull(criteriasDTO.getFromReleaseDate())) {
            params.and("fromReleaseDate", criteriasDTO.getFromReleaseDate());
        }
        if (Objects.nonNull(criteriasDTO.getToReleaseDate())) {
            params.and("toReleaseDate", criteriasDTO.getToReleaseDate());
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

        if (Objects.nonNull(criteriasDTO.getGenreIds()) && !criteriasDTO.getGenreIds().isEmpty()) {
            params.and("genreIds", criteriasDTO.getGenreIds());
        }
        if (Objects.nonNull(criteriasDTO.getCountryIds()) && !criteriasDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", criteriasDTO.getCountryIds());
        }
        if (Objects.nonNull(criteriasDTO.getUserIds()) && !criteriasDTO.getUserIds().isEmpty()) {
            params.and("userIds", criteriasDTO.getUserIds());
        }

        return params;
    }
}
