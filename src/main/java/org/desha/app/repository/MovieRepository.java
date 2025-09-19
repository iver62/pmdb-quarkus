package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.entity.TechnicalTeam;
import org.desha.app.domain.record.MovieWithAwardsNumber;
import org.desha.app.domain.record.Repartition;
import org.desha.app.helper.MovieRepositoryHelper;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    /**
     * Compte le nombre de films correspondant aux critères spécifiés.
     * <p>
     * Le paramètre {@link CriteriaDTO} permet de filtrer les films selon différents critères,
     * tels que le titre (insensible à la casse et aux accents) et d'autres options définies dans {@link CriteriaDTO}.
     * <p>
     * Si aucun terme n'est fourni, tous les films sont comptés.
     *
     * @param criteriaDTO Les critères de filtrage des films. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères.
     */
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

    /**
     * Compte le nombre de films associés à une personne spécifique et correspondant aux critères fournis.
     * <p>
     * La méthode utilise l'identité de la personne pour filtrer les films liés à elle (acteur, réalisateur, etc.).
     * Le titre du film est comparé en ignorant la casse et les accents.
     * Les autres critères de filtrage peuvent être fournis via {@link CriteriaDTO}.
     * <p>
     * Si aucun terme n’est fourni dans {@link CriteriaDTO}, tous les films liés à la personne sont comptés.
     *
     * @param person      Le {@link Person} dont les films sont comptés. Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères supplémentaires pour filtrer les films. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères pour cette personne.
     */
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

    /**
     * Compte le nombre de films associés à un pays spécifique et correspondant à un terme de recherche.
     * <p>
     * La méthode filtre les films liés au pays dont l'identifiant est fourni.
     * Le titre des films est comparé en ignorant la casse et les accents.
     * Si le paramètre {@code term} est {@code null}, tous les films liés au pays sont comptés.
     *
     * @param id   L'identifiant du pays dont les films doivent être comptés. Ne peut pas être {@code null}.
     * @param term Le terme de recherche pour filtrer les titres des films. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères pour ce pays.
     */
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
                                .and("term", "%" + StringUtils.defaultString(term) + "%")
                );
    }

    /**
     * Compte le nombre de films associés à une catégorie spécifique et correspondant à un terme de recherche.
     * <p>
     * La méthode filtre les films liés à la catégorie dont l'identifiant est fourni.
     * Le titre des films est comparé en ignorant la casse et les accents.
     * Si le paramètre {@code term} est {@code null}, tous les films liés à la catégorie sont comptés.
     *
     * @param id   L'identifiant de la catégorie dont les films doivent être comptés. Ne peut pas être {@code null}.
     * @param term Le terme de recherche pour filtrer les titres des films. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères pour cette catégorie.
     */
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
                                .and("term", "%" + StringUtils.defaultString(term) + "%")
                );
    }

    /**
     * Vérifie si un film existe déjà dans la base de données en fonction de son titre et, optionnellement, de son titre original.
     * <p>
     * La comparaison des titres ignore la casse et les accents.
     * Si {@code originalTitle} est {@code null} ou vide, la vérification ne porte que sur le titre principal.
     * <p>
     * Retourne {@code true} si un film correspondant est trouvé, {@code false} sinon.
     *
     * @param title         Le titre du film à vérifier. Ne peut pas être {@code null} ou vide.
     * @param originalTitle Le titre original du film à vérifier. Peut être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si un film correspondant existe, {@code false} sinon.
     */
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
     * @param id L'identifiant du film à rechercher. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le film correspondant si trouvé, ou {@code null} si aucun film n'existe avec cet identifiant.
     */
    @Override
    public Uni<Movie> findById(Long id) {
        return find("id", id).firstResult();
    }

    /**
     * Recherche un film par son identifiant en chargeant également ses pays et catégories associés.
     * <p>
     * Utilise des jointures FETCH pour récupérer en une seule requête les collections {@code countries} et {@code categories} du film.
     *
     * @param id L'identifiant du film à rechercher. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le film correspondant avec ses pays et catégories, ou {@code null} si aucun film n'existe avec cet identifiant.
     */
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

    /**
     * Recherche un film par son identifiant en chargeant également l'ensemble de son équipe technique.
     * <p>
     * Cette méthode récupère le film avec toutes ses collections techniques (producteurs, réalisateurs, assistants-réalisateur,
     * scénaristes, compositeurs, musiciens, photographes, costumiers, décorateurs, monteurs, directeurs de casting, artistes,
     * ingénieurs son, superviseurs VFX/SFX, maquilleurs, coiffeurs et cascadeurs) en utilisant Mutiny pour effectuer des fetch asynchrones.
     *
     * @param id L'identifiant du film à rechercher. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le film correspondant avec toutes ses relations d'équipe technique chargées,
     * ou {@code null} si aucun film n'existe avec cet identifiant.
     */
    public Uni<TechnicalTeam> findTechnicalTeam(Long id) {
        return
                findById(id)
                        .map(Movie::getTechnicalTeam)
                        .call(technicalTeam -> Mutiny.fetch(technicalTeam.getMovieProducers()).invoke(technicalTeam::setMovieProducers)
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieDirectors()).invoke(technicalTeam::setMovieDirectors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieAssistantDirectors()).invoke(technicalTeam::setMovieAssistantDirectors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieScreenwriters()).invoke(technicalTeam::setMovieScreenwriters))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieComposers()).invoke(technicalTeam::setMovieComposers))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieMusicians()).invoke(technicalTeam::setMovieMusicians))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMoviePhotographers()).invoke(technicalTeam::setMoviePhotographers))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieCostumeDesigners()).invoke(technicalTeam::setMovieCostumeDesigners))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieSetDesigners()).invoke(technicalTeam::setMovieSetDesigners))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieEditors()).invoke(technicalTeam::setMovieEditors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieCasters()).invoke(technicalTeam::setMovieCasters))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieArtists()).invoke(technicalTeam::setMovieArtists))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieSoundEditors()).invoke(technicalTeam::setMovieSoundEditors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieVfxSupervisors()).invoke(technicalTeam::setMovieVfxSupervisors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieSfxSupervisors()).invoke(technicalTeam::setMovieSfxSupervisors))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieMakeupArtists()).invoke(technicalTeam::setMovieMakeupArtists))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieHairDressers()).invoke(technicalTeam::setMovieHairDressers))
                                .chain(() -> Mutiny.fetch(technicalTeam.getMovieStuntmen()).invoke(technicalTeam::setMovieStuntmen))
                        )
                ;
    }

    /**
     * Recherche une liste de films avec le nombre de récompenses associées, selon des critères et un tri donnés.
     * <p>
     * Chaque film retourné contient également le nombre de récompenses ('awardsNumber') calculé à partir
     * de la table 'MovieAwardsNumber'. Si aucun enregistrement n'existe pour un film, le nombre de récompenses
     * est considéré comme 0.
     *
     * @param page        La page à récupérer (pagination). Ne peut pas être {@code null}.
     * @param sort        Le nom du champ utilisé pour trier les résultats. Peut être {@code null} pour l'ordre par défaut.
     * @param direction   La direction du tri ({@link Sort.Direction}). Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage des films (titre et autres filtres). Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste des films avec le nombre de récompenses correspondant aux critères et à la page demandée.
     */
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

    /**
     * Recherche une liste de films avec le nombre de récompenses associées, selon des critères et un tri donnés.
     * <p>
     * Chaque film retourné contient également le nombre de récompenses ('awardsNumber') calculé à partir de la table
     * {@code MovieAwardsNumber}. Si aucun enregistrement n'existe pour un film, le nombre de récompenses est considéré comme 0.
     * <p>
     * Contrairement à la version paginée, cette méthode retourne l'ensemble des résultats correspondant aux critères.
     *
     * @param sort        Le nom du champ utilisé pour trier les résultats. Peut être {@code null} pour l'ordre par défaut.
     * @param direction   La direction du tri ({@link Sort.Direction}). Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage des films (titre et autres filtres). Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste des films avec le nombre de récompenses correspondant aux critères.
     */
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

    /**
     * Recherche une liste de films associés à une personne donnée, avec le nombre de récompenses pour chaque film.
     * <p>
     * Cette méthode retourne les films dans lesquels la personne apparaît (acteur, réalisateur, etc.),
     * en appliquant des critères supplémentaires (titre et autres filtres) et un tri donné.
     * Chaque film est accompagné du nombre de récompenses ({@code awardsNumber}) calculé à partir de
     * la table {@code MovieAwardsNumber}. Si aucun enregistrement n'existe pour un film, le nombre de récompenses est 0.
     *
     * @param person      La personne pour laquelle rechercher les films. Ne peut pas être {@code null}.
     * @param page        La page des résultats à retourner.
     * @param sort        Le nom du champ utilisé pour trier les résultats. Peut être {@code null} pour l'ordre par défaut.
     * @param direction   La direction du tri ({@link Sort.Direction}). Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage des films (titre et autres filtres). Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste paginée des films associés à la personne, avec le nombre de récompenses.
     */
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

    /**
     * Recherche une liste de films associés à un pays donné, avec le nombre de récompenses pour chaque film.
     * <p>
     * Cette méthode retourne les films liés au pays identifié par {@code id}, en appliquant des critères supplémentaires
     * (titre et autres filtres) et un tri donné. Chaque film est accompagné du nombre de récompenses ({@code awardsNumber})
     * calculé à partir de la table {@code MovieAwardsNumber}. Si aucun enregistrement n'existe pour un film, le nombre de
     * récompenses est 0.
     *
     * @param id          L'identifiant du pays dont on souhaite obtenir les films. Ne peut pas être {@code null}.
     * @param page        La page des résultats à retourner.
     * @param sort        Le nom du champ utilisé pour trier les résultats. Peut être {@code null} pour l'ordre par défaut.
     * @param direction   La direction du tri ({@link Sort.Direction}). Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage des films (titre et autres filtres). Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste paginée des films associés au pays, avec le nombre de récompenses.
     */
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

    /**
     * Recherche une liste de films associés à une catégorie donnée, avec le nombre de récompenses pour chaque film.
     * <p>
     * Cette méthode retourne les films liés à la catégorie identifiée par {@code id}, en appliquant des critères supplémentaires
     * (titre et autres filtres) et un tri donné. Chaque film est accompagné du nombre de récompenses ({@code awardsNumber})
     * calculé à partir de la table {@code MovieAwardsNumber}. Si aucun enregistrement n'existe pour un film, le nombre de
     * récompenses est 0.
     *
     * @param id          L'identifiant de la catégorie dont on souhaite obtenir les films. Ne peut pas être {@code null}.
     * @param page        La page des résultats à retourner.
     * @param sort        Le nom du champ utilisé pour trier les résultats. Peut être {@code null} pour l'ordre par défaut.
     * @param direction   La direction du tri ({@link Sort.Direction}). Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage des films (titre et autres filtres). Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste paginée des films associés à la catégorie, avec le nombre de récompenses.
     */
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

    /**
     * Récupère l'évolution cumulative du nombre de films créés par mois.
     * <p>
     * Cette méthode calcule le nombre total de films créés pour chaque mois, puis génère un cumul progressif
     * afin de suivre l'évolution au fil du temps.
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément représente un mois
     * (au format "MM-YYYY") et le nombre cumulé de films créés jusqu'à ce mois.
     */
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

    /**
     * Récupère la répartition des films par date de création.
     * <p>
     * Cette méthode compte le nombre de films créés pour chaque mois, au format "MM-YYYY".
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément représente un mois
     * et le nombre de films créés pendant ce mois.
     */
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

    /**
     * Récupère la répartition des films par décennie de sortie.
     * <p>
     * Cette méthode regroupe les films par décennie de leur date de sortie, par exemple 1990, 2000, etc.,
     * et compte le nombre de films pour chaque décennie.
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément représente
     * une décennie et le nombre de films sortis durant cette période.
     */
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

    /**
     * Récupère la répartition des films par catégorie.
     * <p>
     * Cette méthode compte le nombre de films associés à chaque catégorie et retourne la liste triée par nombre de films décroissant.
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément représente
     * une catégorie et le nombre de films qui lui sont associés.
     */
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

    /**
     * Récupère la répartition des films par pays.
     * <p>
     * Cette méthode compte le nombre de films associés à chaque pays et retourne la liste triée par nombre de films décroissant.
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément
     * représente un pays et le nombre de films qui lui sont associés.
     */
    public Uni<List<Repartition>> findMoviesByCountryRepartition() {
        return
                find("""
                        SELECT c.nomFrFr, COUNT(m)
                        FROM Movie m
                        JOIN m.countries c
                        GROUP BY c.id
                        ORDER BY COUNT(m) DESC
                        """
                )
                        .project(Repartition.class)
                        .list()
                ;
    }

    /**
     * Récupère la répartition des films par utilisateur.
     * <p>
     * Cette méthode compte le nombre de films créés ou associés à chaque utilisateur et retourne la liste triée par nombre de films décroissant.
     *
     * @return Un {@link Uni} contenant la liste des {@link Repartition}, où chaque élément
     * représente un utilisateur et le nombre de films qui lui sont associés.
     */
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
