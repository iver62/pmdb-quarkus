package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.MovieActor;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.enums.NotificationType;
import org.desha.app.domain.record.Repartition;
import org.desha.app.exception.MovieUpdateException;
import org.desha.app.exception.PhotoDeletionException;
import org.desha.app.mapper.*;
import org.desha.app.repository.*;
import org.desha.app.utils.Messages;
import org.desha.app.utils.Utils;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieService {

    private static final String POSTERS_DIR = "posters/";

    private final CategoryMapper categoryMapper;
    private final CeremonyAwardsMapper ceremonyAwardsMapper;
    private final CountryMapper countryMapper;
    private final MovieActorMapper movieActorMapper;
    private final MovieMapper movieMapper;
    private final PersonMapper personMapper;
    private final TechnicalTeamMapper technicalTeamMapper;

    private final AwardService awardService;
    private final CategoryService categoryService;
    private final CeremonyAwardsService ceremonyAwardsService;
    private final CountryService countryService;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final StatsService statsService;
    private final UserNotificationService userNotificationService;

    private final CategoryRepository categoryRepository;
    private final CeremonyAwardsRepository ceremonyAwardsRepository;
    private final CountryRepository countryRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    @Inject
    public MovieService(
            CategoryMapper categoryMapper,
            CeremonyAwardsMapper ceremonyAwardsMapper,
            CountryMapper countryMapper,
            MovieMapper movieMapper,
            MovieActorMapper movieActorMapper,
            PersonMapper personMapper,
            TechnicalTeamMapper technicalTeamMapper,
            AwardService awardService,
            CeremonyAwardsService ceremonyAwardsService,
            CategoryRepository categoryRepository,
            CountryService countryService,
            FileService fileService,
            CategoryService categoryService,
            NotificationService notificationService,
            StatsService statsService,
            UserNotificationService userNotificationService,
            CeremonyAwardsRepository ceremonyAwardsRepository,
            CountryRepository countryRepository,
            MovieActorRepository movieActorRepository,
            MovieRepository movieRepository,
            PersonRepository personRepository,
            UserRepository userRepository
    ) {
        this.categoryMapper = categoryMapper;
        this.ceremonyAwardsMapper = ceremonyAwardsMapper;
        this.countryMapper = countryMapper;
        this.movieMapper = movieMapper;
        this.movieActorMapper = movieActorMapper;
        this.personMapper = personMapper;
        this.technicalTeamMapper = technicalTeamMapper;
        this.ceremonyAwardsRepository = ceremonyAwardsRepository;
        this.awardService = awardService;
        this.ceremonyAwardsService = ceremonyAwardsService;
        this.countryService = countryService;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.fileService = fileService;
        this.categoryService = categoryService;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.notificationService = notificationService;
        this.statsService = statsService;
        this.userNotificationService = userNotificationService;
    }

    /**
     * Compte le nombre de films correspondant aux critères fournis.
     * <p>
     * Si {@code criteriaDTO} contient des filtres spécifiques (par titre, catégorie, pays, personne, etc.),
     * le comptage ne prendra en compte que les films correspondant à ces critères.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param criteriaDTO Les critères de filtrage à appliquer. Peut être {@code null} pour compter tous les films.
     * @return Un {@link Uni} contenant le nombre de films correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> count(CriteriaDTO criteriaDTO) {
        return
                movieRepository.countMovies(criteriaDTO)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur interne lors du comptage des films avec critères={}", criteriaDTO, throwable);
                                    return new WebApplicationException("Impossible de compter les films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de personnes associées à un film spécifique, en fonction des critères fournis.
     * <p>
     * Si {@code criteriaDTO} contient des filtres (par rôle, âge, pays, etc.), le comptage ne prendra en compte
     * que les personnes correspondant à ces critères.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id          L’identifiant du film pour lequel compter les personnes. Ne peut pas être {@code null}.
     * @param criteriaDTO Les critères de filtrage à appliquer. Peut être {@code null} pour compter toutes les personnes liées au film.
     * @return Un {@link Uni} contenant le nombre de personnes correspondant aux critères pour le film donné.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countPersonsByMovie(@NotNull Long id, CriteriaDTO criteriaDTO) {
        return
                personRepository.countPersonsByMovie(id, criteriaDTO)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur interne lors du comptage des personnes associées au film [id={}], critères={}", id, criteriaDTO, throwable);
                                    return new WebApplicationException("Impossible de compter les personnes liées à ce film", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de pays associés aux films correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche s'effectue sur le nom du pays, en ignorant la casse et les accents.
     * Le paramètre {@code lang} permet de sélectionner la langue du nom à utiliser pour la recherche.
     * <p>
     * Si {@code term} est {@code null}, tous les pays associés à au moins un film sont comptés.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param term La chaîne utilisée pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang La langue utilisée pour le nom du pays (par exemple "fr" ou "en").
     * @return Un {@link Uni} contenant le nombre de pays correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countCountriesInMovies(String term, String lang) {
        return
                countryRepository.countCountriesInMovies(term, lang)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des pays dans les films avec filtre [term={}, lang={}]", term, lang, throwable);
                                    return new WebApplicationException("Impossible de compter les pays des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Compte le nombre de catégories associées aux films correspondant éventuellement à un terme de recherche.
     * <p>
     * La recherche s'effectue sur le nom de la catégorie, en ignorant la casse et les accents.
     * <p>
     * Si {@code term} est {@code null}, toutes les catégories associées à au moins un film sont comptées.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param term Le terme de recherche utilisé pour filtrer les catégories. Peut être {@code null}.
     * @return Un {@link Uni} contenant le nombre de catégories correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors du comptage.
     */
    public Uni<Long> countCategoriesInMovies(String term) {
        return
                categoryRepository.countCategoriesInMovies(term)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors du comptage des catégories dans les films avec le filtre {}", term, throwable);
                                    return new WebApplicationException("Impossible de compter les catégories des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère un film ({@link MovieDTO}) à partir de son identifiant.
     * <p>
     * La méthode récupère également les pays et catégories associés au film.
     * Si aucun film ne correspond à l'identifiant fourni, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant du film à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le {@link MovieDTO} correspondant à l’identifiant fourni.
     * @throws NotFoundException       si aucun film n’est trouvé pour l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération du film.
     */
    public Uni<MovieDTO> getById(@NotNull Long id) {
        return
                movieRepository.findByIdWithCountriesAndCategories(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .map(movieMapper::toDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération du film avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer les informations du film", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de films correspondant aux critères fournis.
     * <p>
     * Les films sont récupérés avec leurs informations sur les récompenses et sont mappés en {@link MovieDTO}.
     * Si {@code criteriaDTO} contient des filtres (titre, catégorie, pays, personne, etc.), seuls les films correspondant
     * à ces critères sont retournés.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page        Les informations de pagination à appliquer (index et taille de page).
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères de filtrage à appliquer. Peut être {@code null} pour récupérer tous les films.
     * @return Un {@link Uni} contenant une {@link List} de {@link MovieDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des films.
     */
    public Uni<List<MovieDTO>> getMovies(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository
                        .findMovies(page, sort, direction, criteriaDTO)
                        .map(movieMapper::movieWithAwardsListToDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération de la liste des films avec les critères {}", criteriaDTO, throwable);
                                    return new WebApplicationException("Impossible de récupérer la liste des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste triée de films correspondant aux critères fournis.
     * <p>
     * Les films sont récupérés avec leurs informations sur les récompenses et sont mappés en {@link MovieDTO}.
     * Si {@code criteriaDTO} contient des filtres (titre, catégorie, pays, personne, etc.), seuls les films correspondant
     * à ces critères sont retournés.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères de filtrage à appliquer. Peut être {@code null} pour récupérer tous les films.
     * @return Un {@link Uni} contenant une {@link List} de {@link MovieDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des films.
     */
    public Uni<List<MovieDTO>> getMovies(String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository
                        .findMovies(sort, direction, criteriaDTO)
                        .map(movieMapper::movieWithAwardsListToDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des films", throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_MOVIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste de films correspondant exactement au titre fourni.
     * <p>
     * Les films récupérés sont mappés en {@link MovieDTO}.
     * Si aucun film ne correspond au titre fourni, une liste vide est retournée.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param title Le titre exact des films à rechercher. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant une {@link List} de {@link MovieDTO} correspondant au titre fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des films.
     */
    public Uni<List<MovieDTO>> getByTitle(String title) {
        return
                movieRepository.list("title", title)
                        .map(movieMapper::movieListToDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des films", throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_MOVIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de pays associés à des films, éventuellement filtrée par un terme de recherche.
     * <p>
     * La recherche s'effectue sur le nom du pays, en ignorant la casse et les accents. Le paramètre {@code lang} permet
     * de sélectionner la langue du nom à utiliser pour la recherche.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les pays par nom. Peut être {@code null}.
     * @param lang      La langue utilisée pour le nom du pays (par exemple "fr" ou "en").
     * @return Un {@link Uni} contenant une {@link List} de {@link CountryDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des pays.
     */
    public Uni<List<CountryDTO>> getCountriesInMovies(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findCountriesInMovies(page, sort, direction, term, lang)
                        .map(countryMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des pays", throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_COUNTRIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de catégories associées à des films, éventuellement filtrée par un terme de recherche.
     * <p>
     * La recherche s'effectue sur le nom de la catégorie, en ignorant la casse et les accents.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param sort      Le champ sur lequel appliquer le tri.
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les catégories par nom. Peut être {@code null}.
     * @return Un {@link Uni} contenant une {@link List} de {@link CategoryDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des catégories.
     */
    public Uni<List<CategoryDTO>> getCategoriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        return
                categoryRepository.findCategoriesInMovies(page, sort, direction, term)
                        .map(categoryMapper::toDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des catégories", throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_CATEGORIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de personnes associées à un film spécifique, en fonction des critères fournis.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les personnes récupérées sont mappées en {@link LitePersonDTO}. Les critères de filtrage peuvent inclure
     * le rôle, le pays, l’âge, ou d’autres informations définies dans {@link CriteriaDTO}.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée
     * avec un statut HTTP 500.
     *
     * @param id          L’identifiant du film dont on souhaite récupérer les personnes. Ne peut pas être {@code null}.
     * @param page        Les informations de pagination à appliquer (index et taille de page).
     * @param sort        Le champ sur lequel appliquer le tri.
     * @param direction   La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param criteriaDTO Les critères de filtrage à appliquer. Peut être {@code null} pour récupérer toutes les personnes.
     * @return Un {@link Uni} contenant une {@link List} de {@link LitePersonDTO} correspondant aux critères fournis.
     * @throws NotFoundException       si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des personnes.
     */
    public Uni<List<LitePersonDTO>> getPersonsByMovie(@NotNull Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(movie ->
                                personRepository.findPersonsByMovie(id, page, sort, direction, criteriaDTO)
                                        .map(personMapper::toLiteDTOList)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des personnes pour le film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des personnes", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l'équipe technique associée à un film donné.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link IllegalArgumentException} est levée.
     * <p>
     * L'équipe technique récupérée est ensuite mappée en objet {@link TechnicalTeamDTO}.
     *
     * @param id L’identifiant du film dont on souhaite récupérer l'équipe technique. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant un {@link TechnicalTeamDTO} représentant l'équipe technique du film.
     * @throws IllegalArgumentException si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException  si une erreur survient lors de la récupération de l'équipe technique.
     */
    public Uni<TechnicalTeamDTO> getTechnicalTeam(@NotNull Long id) {
        return
                movieRepository.findTechnicalTeam(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_FILM))
                        .map(technicalTeamMapper::toDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération de l'équipe technique du film avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer l'équipe technique du film", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des acteurs associés à un film donné.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les acteurs récupérés sont ensuite mappés en objets {@link MovieActorDTO}.
     *
     * @param id L’identifiant du film dont on souhaite récupérer le casting. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant une liste de {@link MovieActorDTO} représentant les acteurs du film.
     * @throws NotFoundException       si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération du casting.
     */
    public Uni<List<MovieActorDTO>> getActorsByMovie(@NotNull Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .chain(this::fetchAndMapActorList)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération du casting du film avec l'ID {}", id, throwable);
                                    return new WebApplicationException("Impossible de récupérer le casting du film", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l’ensemble des catégories associées à un film donné.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les catégories sont ensuite mappées en objets {@link CategoryDTO}.
     *
     * @param id L’identifiant du film dont on souhaite récupérer les catégories. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant un ensemble de {@link CategoryDTO} représentant les catégories associées au film.
     * @throws NotFoundException       si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des catégories.
     */
    public Uni<Set<CategoryDTO>> getCategoriesByMovie(@NotNull Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(this::fetchAndMapCategorySet)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des catégories pour le film {}", id, throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_CATEGORIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l’ensemble des pays associés à un film donné.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les pays sont ensuite mappés en objets {@link CountryDTO}.
     *
     * @param id L’identifiant du film dont on souhaite récupérer les pays. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant un ensemble de {@link CountryDTO} représentant les pays associés au film.
     * @throws NotFoundException       si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des pays.
     */
    public Uni<Set<CountryDTO>> getCountriesByMovie(@NotNull Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(this::fetchAndMapCountrySet)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des pays du film {}", id, throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_GETTING_COUNTRIES, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l’ensemble des cérémonies et récompenses associées à un film donné.
     * <p>
     * Si le film correspondant à l’identifiant {@code id} n’existe pas, une exception {@link NotFoundException} est levée.
     * <p>
     * Les récompenses sont ensuite récupérées via le dépôt des cérémonies et transformées en objets {@link CeremonyAwardsDTO}.
     *
     * @param id L’identifiant du film dont on souhaite récupérer les cérémonies et récompenses. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant un ensemble de {@link CeremonyAwardsDTO} représentant les cérémonies et récompenses du film.
     * @throws NotFoundException       si aucun film ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération des récompenses.
     */
    public Uni<Set<CeremonyAwardsDTO>> getCeremoniesAwardsByMovie(@NotNull Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(movie -> ceremonyAwardsRepository.findCeremoniesAwardsByMovie(movie.getId()))
                        .map(ceremonyAwardsMapper::toDTOSet)
                        .onFailure().transform(e -> {
                                    if (e instanceof WebApplicationException) {
                                        return e;
                                    }
                                    log.error("Erreur lors de la récupération des récompenses: {}", e.getMessage());
                                    return new WebApplicationException("Erreur lors de la récupération des récompenses", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère l’évolution du nombre de films en fonction de leur date de création.
     * <p>
     * Cette méthode interroge le dépôt des films pour obtenir une liste de répartitions ({@link Repartition}) représentant l’évolution
     * statistique dans le temps.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, l’exception est interceptée et loggée, puis transmise telle quelle.
     *
     * @return Un {@link Uni} contenant une liste de {@link Repartition} représentant l’évolution des films par date de création.
     */
    public Uni<List<Repartition>> getMoviesCreationDateEvolution() {
        return
                movieRepository.findMoviesCreationDateEvolution()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de l'évolution des films", failure)
                        )
                ;
    }

    /**
     * Récupère la répartition des films en fonction de leur date de création.
     * <p>
     * Cette méthode interroge le dépôt des films afin d’obtenir une liste de répartitions ({@link Repartition}) représentant la distribution
     * des films selon leur année ou période de création.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, l’exception est interceptée, loggée puis transmise telle quelle.
     *
     * @return Un {@link Uni} contenant une liste de {@link Repartition} représentant la répartition des films par date de création.
     */
    public Uni<List<Repartition>> getMoviesCreationDateRepartition() {
        return
                movieRepository.findMoviesByCreationDateRepartition()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de la répartition des films par date de création", failure)
                        )
                ;
    }

    /**
     * Récupère la répartition des films en fonction de leur date de sortie.
     * <p>
     * Cette méthode interroge le dépôt des films afin d’obtenir une liste de répartitions ({@link Repartition}) représentant la distribution
     * des films selon leur date ou année de sortie.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, l’exception est interceptée, loggée puis transmise telle quelle.
     *
     * @return Un {@link Uni} contenant une liste de {@link Repartition} représentant la répartition des films par date de sortie.
     */
    public Uni<List<Repartition>> getMoviesReleaseDateRepartition() {
        return
                movieRepository.findMoviesByReleaseDateRepartition()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de la répartition des films par date de sortie", failure)
                        )
                ;
    }

    /**
     * Récupère l’affiche (poster) d’un film à partir de son nom de fichier.
     * <p>
     * Si le nom du fichier est {@code null} ou vide, la méthode renvoie l’affiche par défaut.
     * Si le fichier spécifié n’est pas trouvé, un avertissement est loggé et l’affiche par défaut est retournée.
     * <p>
     * Cette méthode s’appuie sur le {@code fileService} pour accéder aux fichiers stockés dans le répertoire des affiches.
     *
     * @param fileName Le nom du fichier de l’affiche à récupérer. Peut être {@code null} ou vide.
     * @return Un {@link Uni} contenant l’objet {@link File} correspondant à l’affiche trouvée, ou l’affiche par défaut si
     * le fichier est manquant ou non spécifié.
     */
    public Uni<File> getPoster(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank()) {
            log.warn("Poster name is missing, returning default poster.");
            return fileService.getFile(POSTERS_DIR, Movie.DEFAULT_POSTER);
        }

        return fileService.getFile(POSTERS_DIR, fileName)
                .onFailure(FileNotFoundException.class).recoverWithUni(() -> {
                    log.warn("Poster {} not found, returning default poster.", fileName);
                    return fileService.getFile(POSTERS_DIR, Movie.DEFAULT_POSTER);
                });
    }

    /**
     * Charge un fichier d’affiche (poster) vers le système de stockage.
     * <p>
     * Si le fichier est invalide (nul, vide ou corrompu), la méthode logge un avertissement et retourne le nom de l’affiche par défaut.
     * En cas d’échec lors du téléversement (exception du {@code fileService}), une erreur est loggée et le nom de l’affiche par défaut
     * est retourné.
     * </p>
     *
     * @param file L’objet {@link FileUpload} représentant le fichier à téléverser. Peut être {@code null} ou contenir un fichier vide.
     * @return Un {@link Uni} contenant le nom du fichier uploadé, ou {@link Movie#DEFAULT_POSTER} en cas d’échec ou si le fichier est invalide.
     */
    private Uni<String> uploadPoster(FileUpload file) {
        if (Objects.isNull(file) || Objects.isNull(file.uploadedFile()) || file.fileName().isBlank()) {
            log.warn("Invalid or missing file. Using default poster.");
            return Uni.createFrom().item(Movie.DEFAULT_POSTER);
        }

        return fileService.uploadFile(POSTERS_DIR, file)
                .onFailure().recoverWithItem(error -> {
                    log.error("Poster upload failed: {}", error.getMessage());
                    return Movie.DEFAULT_POSTER;
                });
    }

    public Uni<MovieDTO> saveMovie(FileUpload file, MovieDTO movieDTO) {
        return
                movieRepository.movieExists(movieDTO.getTitle(), movieDTO.getOriginalTitle())
                        .flatMap(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        return Uni.createFrom().failure(new WebApplicationException("Ce film existe déjà", Response.Status.CONFLICT));
                                    }

                                    Movie movie = movieMapper.movieDTOtoMovie(movieDTO);

                                    return
                                            Panache.withTransaction(() ->
                                                            // Récupérer les pays et les catégories et les affecter au film
                                                            countryService.getByIds(movieDTO.getCountries().stream().map(CountryDTO::getId).toList())
                                                                    .invoke(movie::setCountries)
                                                                    .chain(() -> categoryService.getByIds(movieDTO.getCategories().stream().map(CategoryDTO::getId).toList()))
                                                                    .invoke(movie::setCategories)
                                                                    // Affecter l'utilisateur au film
                                                                    .chain(() ->
                                                                            userRepository.findById(movieDTO.getUser().getId())
                                                                                    .invoke(user -> {
                                                                                        log.info("Movie created by {}", user.getUsername());
                                                                                        movie.setUser(user);
                                                                                    })
                                                                    )
                                                                    .chain(() -> {
                                                                        if (Objects.nonNull(file)) {
                                                                            return uploadPoster(file)
                                                                                    .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", movie.getTitle(), error.getMessage()))
                                                                                    .invoke(movie::setPosterFileName);
                                                                        }
                                                                        movie.setPosterFileName(Movie.DEFAULT_POSTER);
                                                                        return Uni.createFrom().voidItem();
                                                                    })
                                                                    .replaceWith(movie)
                                                                    .chain(movieRepository::persist)
                                                                    .call(entity ->
                                                                            statsService.incrementNumberOfMovies()
                                                                                    .chain(() -> {
                                                                                        if (Objects.nonNull(movie.getCountries()) && !movie.getCountries().isEmpty()) {
                                                                                            return statsService.updateMoviesByCountryRepartition();
                                                                                        }
                                                                                        return Uni.createFrom().voidItem();
                                                                                    })
                                                                                    .chain(() -> {
                                                                                        if (Objects.nonNull(movie.getCategories()) && !movie.getCategories().isEmpty()) {
                                                                                            return statsService.updateMoviesByCategoryRepartition();
                                                                                        }
                                                                                        return Uni.createFrom().voidItem();
                                                                                    })
                                                                    )
                                                                    .call(entity -> notificationService.createNotification("Le film " + movie.getTitle() + " a été créé.", NotificationType.INFO)
                                                                            .chain(userNotificationService::notifyAdmins)
                                                                    )
                                                                    .map(movieMapper::toDTO) // Retourne le film après la transaction
                                                    )
                                                    .onFailure().transform(throwable -> {
                                                                log.error("Erreur lors de la création du film", throwable);
                                                                return new WebApplicationException("Erreur lors de la création du film", Response.Status.INTERNAL_SERVER_ERROR);
                                                            }
                                                    )
                                            ;
                                }
                        )
                ;
    }

    /**
     * Met à jour le casting d’un film donné en fonction d’une liste de nouveaux acteurs.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *   <li>Charge le film par son identifiant et échoue avec une {@link NotFoundException} si celui-ci n’existe pas.</li>
     *   <li>Supprime les acteurs obsolètes qui ne figurent plus dans la nouvelle liste.</li>
     *   <li>Met à jour les acteurs existants avec les nouvelles informations fournies.</li>
     *   <li>Ajoute les nouveaux acteurs via la fonction asynchrone fournie ({@code asyncActorFactory}).</li>
     *   <li>Persiste les modifications et force la génération des identifiants.</li>
     *   <li>Met à jour les statistiques des acteurs.</li>
     *   <li>Crée une notification d’information et l’envoie aux administrateurs.</li>
     * </ul>
     * Enfin, les entités d’acteurs sont converties en objets {@link MovieActorDTO} et retournées.
     *
     * @param id                 L’identifiant du film dont le casting doit être mis à jour. Ne peut pas être {@code null}.
     * @param movieActorsDTOList La nouvelle liste des acteurs du film. Peut contenir des acteurs existants (mise à jour) et/ou de nouveaux acteurs (ajout).
     * @param asyncActorFactory  Une fonction asynchrone permettant de créer un {@link MovieActor} à partir d’un {@link Movie} et d’un {@link MovieActorDTO}.
     * @return Un {@link Uni} contenant la liste mise à jour des acteurs du film sous forme de {@link MovieActorDTO}.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si une erreur survient lors du processus de mise à jour du casting.
     */
    public Uni<List<MovieActorDTO>> saveCast(
            @NotNull Long id,
            List<MovieActorDTO> movieActorsDTOList,
            BiFunction<Movie, MovieActorDTO, Uni<MovieActor>> asyncActorFactory
    ) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .invoke(existingActors -> movie.removeObsoleteActors(movieActorsDTOList)) // Supprimer les acteurs obsolètes
                                                        .invoke(existingActors -> movie.updateExistingActors(movieActorsDTOList)) // Mettre à jour les acteurs existants
                                                        .chain(existingActors -> movie.addMovieActors(movieActorsDTOList, asyncActorFactory)) // Ajouter les nouveaux acteurs
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieActorRepository::flush) // Force la génération des IDs
                                        .call(statsService::updateActorsStats)
                                        .call(movie -> notificationService.createNotification("Le casting du film " + movie.getTitle() + " a été modifié.", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(this::fetchAndMapActorList) // Convertit les entités en DTO
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour du casting pour le film {}", id, throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_UPDATING_ACTORS, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Crée ou met à jour les récompenses d’un film pour une cérémonie donnée.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *   <li>Charge le film par son identifiant et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *   <li>Récupère les personnes associées aux récompenses fournies via {@code awardService}.</li>
     *   <li>Vérifie si un {@link org.desha.app.domain.entity.CeremonyAwards} existe déjà pour la cérémonie spécifiée :
     *       <ul>
     *         <li>Si oui, les récompenses existantes sont mises à jour.</li>
     *         <li>Sinon, un nouvel objet {@link org.desha.app.domain.entity.CeremonyAwards} est créé et ajouté au film.</li>
     *       </ul>
     *   </li>
     *   <li>Persiste et force la génération des identifiants des {@link org.desha.app.domain.entity.CeremonyAwards}.</li>
     *   <li>Persiste les modifications du film.</li>
     *   <li>Crée une notification d’information et l’envoie aux administrateurs.</li>
     * </ul>
     * Enfin, l’objet {@link org.desha.app.domain.entity.CeremonyAwards} résultant est transformé en {@link CeremonyAwardsDTO} et retourné.
     *
     * @param movieId           L’identifiant du film pour lequel les récompenses doivent être sauvegardées. Ne peut pas être {@code null}.
     * @param ceremonyAwardsDTO L’objet {@link CeremonyAwardsDTO} contenant les informations des récompenses à créer ou mettre à jour.
     * @return Un {@link Uni} contenant le {@link CeremonyAwardsDTO} créé ou mis à jour.
     * @throws NotFoundException       si le film correspondant à l’identifiant fourni n’existe pas.
     * @throws WebApplicationException si une erreur survient lors du processus de création ou de mise à jour des récompenses.
     */
    public Uni<CeremonyAwardsDTO> saveCeremonyAwards(@NotNull Long movieId, CeremonyAwardsDTO ceremonyAwardsDTO) {
        return
                Panache.withTransaction(() ->
                        movieRepository.findById(movieId)
                                .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                .chain(movie ->
                                        awardService.getPersonsByAwards(ceremonyAwardsDTO.getAwards())
                                                .map(personList ->
                                                        personList.stream()
                                                                .collect(Collectors.toMap(Person::getId, p -> p))
                                                )
                                                .chain(personMap ->
                                                        Mutiny.fetch(movie.getCeremoniesAwards()) // Récupération de l'ensemble des ceremony awards
                                                                // Récupération de l'objet des ceremony awards à mettre à jour
                                                                .map(ceremonyAwardsSet ->
                                                                        ceremonyAwardsSet.stream()
                                                                                .filter(ca -> Objects.equals(ca.getCeremony().getId(), ceremonyAwardsDTO.getCeremony().getId()))
                                                                                .findFirst()
                                                                )
                                                                .chain(optionalCA -> optionalCA
                                                                        .map(existingCA -> existingCA.updateExistingCeremonyAwards(ceremonyAwardsDTO, personMap)) // S'il existe on le met à jour
                                                                        .orElseGet(() -> ceremonyAwardsService.createNewCeremonyAwards(movie, ceremonyAwardsDTO, personMap) // Sinon, on le crée et on l'ajoute à l'ensemble
                                                                                .invoke(ceremonyAwards -> movie.getCeremoniesAwards().add(ceremonyAwards))
                                                                        )
                                                                )
                                                )
                                                .call(ceremonyAwardsRepository::persist)
                                                .call(ceremonyAwardsRepository::flush)
                                                .call(() -> movieRepository.persist(movie))
                                                .call(ceremonyAwards -> notificationService.createNotification("Les récompenses du film " + movie.getTitle() + " ont été modifiées.", NotificationType.INFO)
                                                        .chain(userNotificationService::notifyAdmins)
                                                )
                                                .map(ceremonyAwardsMapper::ceremonyAwardsToCeremonyAwardsDTO)
                                )
                );
    }

    /**
     * Met à jour les catégories associées à un film donné.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *   <li>Charge le film par son identifiant et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *   <li>Crée les nouvelles catégories qui n’ont pas d’identifiant via {@code categoryService.create}.</li>
     *   <li>Récupère les catégories existantes correspondant aux identifiants fournis.</li>
     *   <li>Assigne l’ensemble complet de catégories au film et met à jour la date de dernière modification.</li>
     *   <li>Persiste le film avec ses nouvelles catégories.</li>
     *   <li>Met à jour les statistiques des films par catégorie.</li>
     *   <li>Crée une notification d’information et l’envoie aux administrateurs.</li>
     * </ul>
     * Enfin, les entités {@link Category} associées au film sont converties en {@link CategoryDTO} et retournées.
     *
     * @param id             L’identifiant du film dont les catégories doivent être mises à jour. Ne peut pas être {@code null}.
     * @param categoryDTOSet L’ensemble des {@link CategoryDTO} représentant les catégories à associer au film.
     * @return Un {@link Uni} contenant l’ensemble des {@link CategoryDTO} mises à jour.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws MovieUpdateException    si une erreur survient lors de la mise à jour des catégories du film.
     * @throws WebApplicationException si une autre erreur non prévue survient.
     */
    public Uni<Set<CategoryDTO>> saveCategories(@NotNull Long id, Set<CategoryDTO> categoryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie -> {
                                            // Les catégories existantes
                                            List<Long> existingCategoryIds = categoryDTOSet.stream()
                                                    .map(CategoryDTO::getId)
                                                    .filter(Objects::nonNull)
                                                    .toList();

                                            // Les nouvelles catégories persistées
                                            List<Uni<Category>> newCategoryUnis = categoryDTOSet.stream()
                                                    .filter(categoryDTO -> Objects.isNull(categoryDTO.getId()))
                                                    .map(categoryService::create)
                                                    .toList();

                                            Uni<Set<Category>> newCategoriesUni = newCategoryUnis.isEmpty()
                                                    ? Uni.createFrom().item(new HashSet<>())  // Retourne un empty set s'il n'y a pas de nouvelles catégories
                                                    : Uni.join().all(newCategoryUnis).andCollectFailures()
                                                    .map(HashSet::new); // Convertit List<Category> en Set<Category>

                                            return newCategoriesUni
                                                    .chain(created ->
                                                            categoryService.getByIds(existingCategoryIds)
                                                                    .map(existing -> {
                                                                        Set<Category> allCategories = new HashSet<>(existing);
                                                                        allCategories.addAll(created);
                                                                        movie.setCategories(allCategories);
                                                                        movie.setLastUpdate(LocalDateTime.now());
                                                                        return movie;
                                                                    })

                                                    );
                                        })
                                        .chain(movieRepository::persist)
                                        .call(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .call(movie -> notificationService.createNotification("Les catégories du film " + movie.getTitle() + " ont été modifiées.", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(this::fetchAndMapCategorySet)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la mise à jour des catégories pour le film {}", id, throwable);
                            return new MovieUpdateException(Messages.ERROR_WHILE_UPDATING_CATEGORIES, throwable);
                        })
                ;
    }

    /**
     * Met à jour les pays associés à un film donné.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *   <li>Charge le film par son identifiant et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *   <li>Récupère les pays existants correspondant aux identifiants fournis via {@code countryService.getByIds}.</li>
     *   <li>Assigne l’ensemble complet de pays au film et met à jour la date de dernière modification.</li>
     *   <li>Persiste le film avec ses nouveaux pays.</li>
     *   <li>Met à jour les statistiques des films par pays.</li>
     *   <li>Crée une notification d’information et l’envoie aux administrateurs.</li>
     * </ul>
     * Enfin, les entités {@link org.desha.app.domain.entity.Country} associées au film sont converties en {@link CountryDTO} et retournées.
     *
     * @param id            L’identifiant du film dont les pays doivent être mis à jour. Ne peut pas être {@code null}.
     * @param countryDTOSet L’ensemble des {@link CountryDTO} représentant les pays à associer au film.
     * @return Un {@link Uni} contenant l’ensemble des {@link CountryDTO} mis à jour.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws MovieUpdateException    si une erreur survient lors de la mise à jour des pays du film.
     * @throws WebApplicationException si une autre erreur non prévue survient.
     */
    public Uni<Set<CountryDTO>> saveCountries(@NotNull Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                countryService.getByIds(
                                                                countryDTOSet.stream()
                                                                        .map(CountryDTO::getId)
                                                                        .filter(Objects::nonNull)
                                                                        .toList()
                                                        )
                                                        .invoke(finalCountrySet -> {
                                                            movie.setCountries(new HashSet<>(finalCountrySet));
                                                            movie.setLastUpdate(LocalDateTime.now());
                                                        })
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .call(movie -> notificationService.createNotification("Les pays du film " + movie.getTitle() + " ont été modifiés.", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(this::fetchAndMapCountrySet)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la mise à jour des pays pour le film {}", id, throwable);
                            return new MovieUpdateException(Messages.ERROR_WHILE_UPDATING_COUNTRIES, throwable);
                        })
                ;
    }

    /**
     * Ajoute de nouveaux acteurs à un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *   <li>Charge le film par son identifiant et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *   <li>Récupère la liste des acteurs existants du film et échoue avec une {@link WebApplicationException} si elle est {@code null}.</li>
     *   <li>Ajoute les acteurs fournis dans {@code movieActorDTOList} via la fonction {@code asyncActorFactory}.</li>
     *   <li>Persiste les modifications du film et force la génération des identifiants des nouveaux acteurs.</li>
     *   <li>Met à jour les statistiques des acteurs.</li>
     *   <li>Crée une notification pour signaler l’ajout des acteurs et notifie les administrateurs.</li>
     * </ul>
     * Enfin, les entités {@link MovieActor} sont converties en {@link MovieActorDTO} et retournées.
     *
     * @param id                L’identifiant du film auquel les acteurs doivent être ajoutés. Ne peut pas être {@code null}.
     * @param movieActorDTOList La liste des {@link MovieActorDTO} représentant les acteurs à ajouter.
     * @param asyncActorFactory Une fonction asynchrone permettant de créer un {@link MovieActor} à partir d’un {@link Movie} et d’un {@link MovieActorDTO}.
     * @return Un {@link Uni} contenant la liste des {@link MovieActorDTO} ajoutés au film.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si la liste d’acteurs du film est {@code null} ou qu’une erreur inattendue survient.
     * @throws MovieUpdateException    si une erreur survient lors de l’ajout des acteurs.
     */
    public Uni<List<MovieActorDTO>> addMovieActors(
            @NotNull Long id,
            List<MovieActorDTO> movieActorDTOList,
            BiFunction<Movie, MovieActorDTO, Uni<MovieActor>> asyncActorFactory
    ) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_ACTORS))
                                                        .chain(existingActors -> movie.addMovieActors(movieActorDTOList, asyncActorFactory)) // Ajouter les nouveaux acteurs
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieActorRepository::flush) // Force la génération des IDs
                                        .call(statsService::updateActorsStats)
                                        .call(movie -> notificationService.createNotification("Des acteurs ont été ajoutés au film " + movie.getTitle(), NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(this::fetchAndMapActorList) // Convertit les entités en DTO
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            return new MovieUpdateException(Messages.ERROR_WHILE_ADDING_ACTORS, throwable);
                        })
                ;
    }

    /**
     * Ajoute des catégories à un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère l’ensemble des catégories déjà associées au film et échoue avec une {@link WebApplicationException} si l’ensemble est {@code null}.</li>
     *     <li>Récupère les entités {@link Category} correspondant aux identifiants des {@link CategoryDTO} fournis et échoue avec une {@link IllegalArgumentException} si une ou plusieurs catégories sont introuvables.</li>
     *     <li>Ajoute les catégories récupérées au film.</li>
     *     <li>Persiste les modifications et met à jour les statistiques liées aux répartitions des films par catégorie.</li>
     *     <li>Convertit l’ensemble des catégories du film en {@link CategoryDTO} et les retourne.</li>
     * </ul>
     *
     * @param movieId        L’identifiant du film auquel les catégories doivent être ajoutées. Ne peut pas être {@code null}.
     * @param categoryDTOSet L’ensemble des {@link CategoryDTO} représentant les catégories à ajouter.
     * @return Un {@link Uni} contenant l’ensemble des {@link CategoryDTO} ajoutées au film.
     * @throws NotFoundException        si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException  si l’ensemble des catégories du film est {@code null}.
     * @throws IllegalArgumentException si une ou plusieurs catégories fournies sont introuvables.
     * @throws MovieUpdateException     si une erreur survient lors de l’ajout des catégories.
     */
    public Uni<Set<CategoryDTO>> addCategories(@NotNull Long movieId, Set<CategoryDTO> categoryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_CATEGORIES))
                                                        .chain(categorySet ->
                                                                categoryService.getByIds(categoryDTOSet.stream().map(CategoryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Une ou plusieurs catégories sont introuvables"))
                                                        )
                                                        .invoke(movie::addCategories)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCategorySet)
                                        .invoke(() -> log.info("Catégories ajoutées au film {}", movieId))
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de l'ajout des catégories au film {}", movieId, throwable);
                            return new MovieUpdateException(Messages.ERROR_WHILE_ADDING_CATEGORIES, throwable);
                        })
                ;
    }

    /**
     * Ajoute des pays à un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère l’ensemble des pays déjà associés au film et échoue avec une {@link WebApplicationException} si l’ensemble est {@code null}.</li>
     *     <li>Récupère les entités {@link org.desha.app.domain.entity.Country} correspondant aux identifiants des {@link CountryDTO} fournis et échoue avec une {@link IllegalArgumentException} si un ou plusieurs pays sont introuvables.</li>
     *     <li>Ajoute les pays récupérés au film.</li>
     *     <li>Persiste les modifications et met à jour les statistiques liées aux répartitions des films par pays.</li>
     *     <li>Convertit l’ensemble des pays du film en {@link CountryDTO} et les retourne.</li>
     * </ul>
     *
     * @param movieId       L’identifiant du film auquel les pays doivent être ajoutés. Ne peut pas être {@code null}.
     * @param countryDTOSet L’ensemble des {@link CountryDTO} représentant les pays à ajouter.
     * @return Un {@link Uni} contenant l’ensemble des {@link CountryDTO} ajoutés au film.
     * @throws NotFoundException        si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException  si l’ensemble des pays du film est {@code null}.
     * @throws IllegalArgumentException si un ou plusieurs pays fournis sont introuvables.
     * @throws MovieUpdateException     si une erreur survient lors de l’ajout des pays.
     */
    public Uni<Set<CountryDTO>> addCountries(@NotNull Long movieId, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_COUNTRIES))
                                                        .chain(countrySet ->
                                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                        )
                                                        .invoke(movie::addCountries)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCountrySet)
                                        .invoke(() -> log.info("Pays ajoutés au film {}", movieId))
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de l'ajout des pays au film {}", movieId, throwable);
                            return new MovieUpdateException(Messages.ERROR_WHILE_ADDING_COUNTRIES, throwable);
                        })
                ;
    }

    /**
     * Supprime un acteur d’un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère la liste des acteurs associés au film et échoue avec une {@link WebApplicationException} si la liste est {@code null}.</li>
     *     <li>Supprime l’acteur identifié par {@code movieActorId} de la liste des acteurs du film.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Convertit la liste des acteurs restants en {@link MovieActorDTO} et la retourne.</li>
     * </ul>
     *
     * @param movieId      L’identifiant du film dont l’acteur doit être supprimé. Ne peut pas être {@code null}.
     * @param movieActorId L’identifiant de l’acteur à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste des {@link MovieActorDTO} restants après la suppression.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si la liste des acteurs du film est {@code null}.
     * @throws MovieUpdateException    si une erreur survient lors de la suppression de l’acteur.
     */
    public Uni<List<MovieActorDTO>> removeMovieActor(@NotNull Long movieId, @NotNull Long movieActorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_ACTORS))
                                                        .invoke(movieActorList -> movie.removeMovieActor(movieActorId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .flatMap(this::fetchAndMapActorList)
                        )
                        .onFailure().transform(e -> {
                            if (e instanceof WebApplicationException) {
                                return e;
                            }
                            log.error("Erreur lors de la suppression de l'acteur {} du film {} : {}", movieActorId, movieId, e.getMessage());
                            return new MovieUpdateException(Messages.ERROR_WHILE_REMOVING_ACTOR, e);
                        })
                ;
    }

    /**
     * Supprime une catégorie d’un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère l’ensemble des catégories associées au film et échoue avec une {@link IllegalStateException} si l’ensemble est {@code null}.</li>
     *     <li>Supprime la catégorie identifiée par {@code categoryId} de l’ensemble des catégories du film.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Met à jour les statistiques des films par catégorie.</li>
     *     <li>Convertit l’ensemble des catégories restantes en {@link CategoryDTO} et le retourne.</li>
     * </ul>
     *
     * @param movieId    L’identifiant du film dont la catégorie doit être supprimée. Ne peut pas être {@code null}.
     * @param categoryId L’identifiant de la catégorie à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l’ensemble des {@link CategoryDTO} restants après la suppression.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws IllegalStateException   si l’ensemble des catégories du film est {@code null}.
     * @throws MovieUpdateException    si une erreur survient lors de la suppression de la catégorie.
     * @throws WebApplicationException si une erreur non prévue survient lors de l’opération.
     */
    public Uni<Set<CategoryDTO>> removeCategory(@NotNull Long movieId, @NotNull Long categoryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_CATEGORIES))
                                                        .invoke(categorySet -> movie.removeCategory(categoryId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCategorySet)
                                        .invoke(() -> log.info("Category {} removed from movie {}", categoryId, movieId))
                        )
                        .onFailure().transform(e -> {
                            if (e instanceof WebApplicationException) {
                                return e;
                            }
                            log.error("Erreur lors de la suppression de la catégorie {} du film {} : {}", categoryId, movieId, e.getMessage());
                            return new MovieUpdateException(Messages.ERROR_WHILE_REMOVING_CATEGORY, e);
                        })
                ;
    }

    /**
     * Supprime un pays d’un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère l’ensemble des pays associés au film et échoue avec une {@link WebApplicationException} si l’ensemble est {@code null}.</li>
     *     <li>Supprime le pays identifié par {@code countryId} de l’ensemble des pays du film.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Met à jour les statistiques des films par pays.</li>
     *     <li>Convertit l’ensemble des pays restants en {@link CountryDTO} et le retourne.</li>
     * </ul>
     *
     * @param movieId   L’identifiant du film dont le pays doit être supprimé. Ne peut pas être {@code null}.
     * @param countryId L’identifiant du pays à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l’ensemble des {@link CountryDTO} restants après la suppression.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si l’ensemble des pays du film est {@code null} ou qu’une erreur non prévue survient.
     * @throws MovieUpdateException    si une erreur survient lors de la suppression du pays.
     */
    public Uni<Set<CountryDTO>> removeCountry(@NotNull Long movieId, @NotNull Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_COUNTRIES))
                                                        .invoke(countries -> movie.removeCountry(countryId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCountrySet)
                                        .invoke(() -> log.info("Country {} removed from movie {}", countryId, movieId))
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression du pays {} du film {}", countryId, movieId, throwable);
                            return new MovieUpdateException(Messages.ERROR_WHILE_REMOVING_COUNTRY, throwable);
                        })
                ;
    }

    /**
     * Supprime une cérémonie de récompenses d’un film existant.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code movieId} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Récupère l’ensemble des cérémonies de récompenses associées au film et échoue avec une {@link IllegalStateException} si cet ensemble est {@code null}.</li>
     *     <li>Supprime la cérémonie de récompenses identifiée par {@code ceremonyAwardsId} de l’ensemble du film.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Convertit l’ensemble restant des cérémonies en {@link CeremonyAwardsDTO} et le retourne.</li>
     * </ul>
     *
     * @param movieId          L’identifiant du film dont la cérémonie de récompenses doit être supprimée. Ne peut pas être {@code null}.
     * @param ceremonyAwardsId L’identifiant de la cérémonie de récompenses à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l’ensemble des {@link CeremonyAwardsDTO} restants après la suppression.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws IllegalStateException   si l’ensemble des cérémonies de récompenses du film est {@code null}.
     * @throws WebApplicationException si une erreur survient lors de la suppression de la cérémonie.
     */
    public Uni<Set<CeremonyAwardsDTO>> removeCeremonyAwards(@NotNull Long movieId, @NotNull Long ceremonyAwardsId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_CEREMONY_AWARDS))
                                                        .invoke(ceremonyAwardsSet -> movie.removeCeremonyAward(ceremonyAwardsId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .map(movie -> ceremonyAwardsMapper.toDTOSet(movie.getCeremoniesAwards()))
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            return new WebApplicationException("Erreur lors de la suppression de la cérémonie", throwable);
                        });
    }

    /**
     * Met à jour les informations d’un film existant, y compris ses catégories, pays, date de sortie et affiche.
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code id} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Met à jour les informations générales du film à partir de {@code movieDTO}.</li>
     *     <li>Gère l’affiche du film :
     *         <ul>
     *             <li>Si un nouveau fichier {@code file} est fourni, il remplace l’affiche existante (sauf si c’est l’affiche par défaut) et supprime l’ancienne.</li>
     *             <li>Si aucun fichier n’est fourni mais que le nom de l’affiche change, on remet l’affiche par défaut après suppression de l’affiche actuelle.</li>
     *             <li>Si aucun changement d’affiche n’est nécessaire, on conserve l’affiche actuelle.</li>
     *         </ul>
     *     </li>
     *     <li>Met à jour les catégories, pays et date de sortie si nécessaire.</li>
     *     <li>Envoie une notification indiquant que le film a été modifié.</li>
     *     <li>Retourne le DTO mis à jour du film.</li>
     * </ul>
     *
     * @param id       L’identifiant du film à mettre à jour. Ne peut pas être {@code null}.
     * @param file     Le nouveau fichier d’affiche à uploader. Peut être {@code null} si l’affiche ne change pas.
     * @param movieDTO Les informations du film à mettre à jour. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant le {@link MovieDTO} mis à jour.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si une erreur survient lors de la mise à jour du film ou de l’affiche.
     */
    public Uni<MovieDTO> updateMovie(@NotNull Long id, FileUpload file, MovieDTO movieDTO) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .invoke(movie -> movie.updateGeneralInfos(movieDTO))
                                        .call(movie -> {
                                            final String currentPoster = movie.getPosterFileName();
                                            final String dtoPoster = movieDTO.getPosterFileName();

                                            if (Objects.nonNull(file)) {
                                                // Nouveau fichier uploadé → on remplace l'ancienne affiche si elle n'est pas l'affiche par défaut
                                                return uploadPoster(file)
                                                        .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", id, error.getMessage()))
                                                        .chain(uploadedFileName ->
                                                                deletePosterIfExists(currentPoster)
                                                                        .replaceWith(uploadedFileName)
                                                        )
                                                        .invoke(movie::setPosterFileName);
                                            } else if (!Objects.equals(currentPoster, dtoPoster)) {
                                                // Pas de nouveau fichier, mais différence → on remet l'affiche par défaut
                                                return
                                                        deletePosterIfExists(currentPoster)
                                                                .invoke(() -> movie.setPosterFileName(Movie.DEFAULT_POSTER))
                                                        ;
                                            }
                                            // Aucun changement d'affiche
                                            return Uni.createFrom().item(movie);
                                        })
                                        .chain(movie -> updateCategoriesIfNeeded(movie, movieDTO))
                                        .chain(movie -> updateCountriesIfNeeded(movie, movieDTO))
                                        .chain(movie -> updateReleaseDateIfNeeded(movie, movieDTO))
                                        .call(movie -> notificationService.createNotification("Le film " + movie.getTitle() + " a été modifié.", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .map(movieMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la modification du film {}", id, throwable);
                                    return new WebApplicationException(Messages.ERROR_WHILE_UPDATING_MOVIE, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    private Uni<Movie> updateReleaseDateIfNeeded(Movie movie, MovieDTO movieDTO) {
        boolean shouldUpdate = Objects.nonNull(movieDTO.getReleaseDate()) && !movieDTO.getReleaseDate().equals(movie.getReleaseDate());
        return (shouldUpdate ? statsService.updateMoviesByReleaseDateRepartition() : Uni.createFrom().voidItem())
                .replaceWith(movie);
    }

    private Uni<Movie> updateCategoriesIfNeeded(Movie movie, MovieDTO movieDTO) {
        return
                Mutiny.fetch(movie.getCategories())
                        .chain(categories -> {
                                    if (!Utils.categoriesEquals(movieDTO.getCategories(), categories)) {
                                        return
                                                categoryService.getByIds(movieDTO.getCategories().stream().map(CategoryDTO::getId).toList())
                                                        .invoke(movie::setCategories)
                                                        .chain(statsService::updateMoviesByCategoryRepartition)
                                                ;
                                    }
                                    return Uni.createFrom().nullItem();
                                }
                        )
                        .replaceWith(movie)
                ;
    }

    private Uni<Movie> updateCountriesIfNeeded(Movie movie, MovieDTO movieDTO) {
        return
                Mutiny.fetch(movie.getCountries())
                        .chain(countries -> {
                                    if (!Utils.countriesEquals(movieDTO.getCountries(), countries)) {
                                        return
                                                countryService.getByIds(movieDTO.getCountries().stream().map(CountryDTO::getId).toList())
                                                        .invoke(movie::setCountries)
                                                        .chain(statsService::updateMoviesByCountryRepartition)
                                                ;
                                    }
                                    return Uni.createFrom().nullItem();
                                }
                        )
                        .replaceWith(movie)
                ;
    }

    /**
     * Supprime un film existant ainsi que ses relations associées (pays, catégories, affiche).
     * <p>
     * Cette méthode effectue les opérations suivantes dans une transaction :
     * <ul>
     *     <li>Charge le film correspondant à l’identifiant {@code id} et échoue avec une {@link NotFoundException} si le film n’existe pas.</li>
     *     <li>Supprime les associations entre le film et ses pays.</li>
     *     <li>Supprime les associations entre le film et ses catégories.</li>
     *     <li>Supprime le film de la base de données.</li>
     *     <li>Met à jour les statistiques des films : décrémente le nombre total de films et recalcul les répartitions par pays et par catégories.</li>
     *     <li>Supprime le fichier d’affiche du film si celui-ci n’est pas l’affiche par défaut.</li>
     * </ul>
     *
     * @param id L’identifiant du film à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si la suppression a réussi.
     * @throws NotFoundException       si le film correspondant à l’identifiant n’existe pas.
     * @throws WebApplicationException si une erreur survient lors de la suppression du film ou de ses relations.
     */
    public Uni<Boolean> deleteMovie(@NotNull Long id) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .flatMap(movie -> {
                                                    final String posterFileName = movie.getPosterFileName();
                                                    return Mutiny.fetch(movie.getCountries()).invoke(countrySet -> movie.clearCountries())
                                                            .chain(() -> Mutiny.fetch(movie.getCategories()).invoke(categorySet -> movie.clearCategories()))
                                                            .chain(() ->
                                                                    movieRepository.delete(movie).replaceWith(true)
                                                                            .chain(aBoolean ->
                                                                                    statsService.decrementNumberOfMovies()
                                                                                            .chain(statsService::updateMoviesByCountryRepartition)
                                                                                            .chain(statsService::updateMoviesByCategoryRepartition)
                                                                                            .replaceWith(aBoolean)
                                                                            )
                                                                            .chain(success -> deletePosterIfExists(posterFileName).replaceWith(success))
                                                            );
                                                }
                                        )
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression du film", throwable);
                            return new WebApplicationException("Erreur lors de la suppression du film", Response.Status.INTERNAL_SERVER_ERROR);
                        });
    }

    /**
     * Supprime le fichier d’affiche d’un film si celui-ci existe et n’est pas l’affiche par défaut.
     * <p>
     * Cette méthode vérifie d’abord que le nom de fichier n’est pas {@code null}, vide ou égal à {@link Movie#DEFAULT_POSTER}.
     * Si c’est le cas, aucune suppression n’est effectuée et la méthode retourne immédiatement.
     * <p>
     * En cas d’erreur lors de la suppression du fichier, une {@link PhotoDeletionException} est levée.
     *
     * @param fileName Le nom du fichier à supprimer. Peut être {@code null} ou vide, auquel cas rien n’est fait.
     * @return Un {@link Uni} qui se complète lorsque l’opération de suppression est terminée.
     * @throws PhotoDeletionException si une erreur survient lors de la suppression du fichier.
     */
    public Uni<Void> deletePosterIfExists(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank() || Objects.equals(fileName, Movie.DEFAULT_POSTER)) {
            return Uni.createFrom().voidItem();
        }

        return Uni.createFrom().item(() -> {
            try {
                fileService.deleteFile(POSTERS_DIR, fileName);
                return null;
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de la photo {}: {}", fileName, e.getMessage());
                throw new PhotoDeletionException("Erreur lors de la suppression de l'affiche " + fileName);
            }
        });
    }

    /**
     * Supprime tous les acteurs associés à un film donné.
     * <p>
     * La méthode récupère le film correspondant à l'identifiant fourni et supprime tous les acteurs liés.
     * Si le film n’existe pas, une {@link NotFoundException} est levée.
     * Si la liste des acteurs est {@code null}, une {@link WebApplicationException} est levée.
     * <p>
     * L’opération est effectuée dans une transaction et persiste les changements.
     *
     * @param id L’identifiant du film dont les acteurs doivent être supprimés. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si l’opération a réussi.
     * @throws NotFoundException       si le film n’est pas trouvé.
     * @throws WebApplicationException si la liste des acteurs est {@code null} ou en cas d’erreur lors de la suppression.
     */
    public Uni<Boolean> clearActors(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie -> Mutiny.fetch(movie.getMovieActors())
                                                .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_ACTORS))
                                                .invoke(movieActorList -> movie.clearActors())
                                                .replaceWith(movie)
                                        )
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression des acteurs du film {}", id, throwable);
                            return new WebApplicationException(Messages.ERROR_WHILE_CLEARING_ACTORS, Response.Status.INTERNAL_SERVER_ERROR);
                        });
    }

    /**
     * Supprime toutes les catégories associées à un film donné.
     * <p>
     * La méthode récupère le film correspondant à l'identifiant fourni et supprime toutes les catégories liées. Elle met également à jour
     * la répartition des films par catégorie via le service de statistiques. Si le film n’existe pas, une {@link NotFoundException} est levée.
     * Si la liste des catégories est {@code null}, une {@link IllegalStateException} est levée.
     * <p>
     * L’opération est effectuée dans une transaction et persiste les changements.
     *
     * @param id L’identifiant du film dont les catégories doivent être supprimées. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si l’opération a réussi.
     * @throws NotFoundException       si le film n’est pas trouvé.
     * @throws IllegalStateException   si la liste des catégories est {@code null}.
     * @throws WebApplicationException en cas d’erreur lors de la suppression des catégories.
     */
    public Uni<Boolean> clearCategories(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_CATEGORIES))
                                                        .invoke(categorySet -> movie.clearCategories())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            log.error("Erreur lors de la suppression des catégories du film {}", id, throwable);
                            return new WebApplicationException(Messages.ERROR_WHILE_CLEARING_CATEGORIES, throwable);
                        });
    }

    /**
     * Supprime tous les pays associés à un film donné.
     * <p>
     * La méthode récupère le film correspondant à l'identifiant fourni et supprime tous les pays liés. Elle met également à jour
     * la répartition des films par pays via le service de statistiques. Si le film n’existe pas, une {@link NotFoundException} est levée.
     * Si la liste des pays est {@code null}, une {@link IllegalStateException} est levée.
     * <p>
     * L’opération est effectuée dans une transaction et persiste les changements.
     *
     * @param id L’identifiant du film dont les pays doivent être supprimés. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si l’opération a réussi.
     * @throws NotFoundException       si le film n’est pas trouvé.
     * @throws IllegalStateException   si la liste des pays est {@code null}.
     * @throws WebApplicationException en cas d’erreur lors de la suppression des pays.
     */
    public Uni<Boolean> clearCountries(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                                                        .invoke(countries -> movie.clearCountries())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            log.error("Erreur lors de la suppression des pays du film {}", id, throwable);
                            return new WebApplicationException(Messages.ERROR_WHILE_CLEARING_COUNTRIES, throwable);
                        });
    }

    /**
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * La méthode récupère le film correspondant à l'identifiant fourni et supprime toutes les cérémonies et récompenses liées.
     * L’opération est effectuée dans une transaction et persiste les changements. Si le film n’existe pas, une {@link NotFoundException}
     * est levée. Si la liste des cérémonies et récompenses est {@code null}, une {@link IllegalStateException} est levée.
     *
     * @param id L’identifiant du film dont les récompenses doivent être supprimées. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant {@code true} si l’opération a réussi.
     * @throws NotFoundException       si le film n’est pas trouvé.
     * @throws IllegalStateException   si la liste des cérémonies et récompenses est {@code null}.
     * @throws WebApplicationException en cas d’erreur lors de la suppression des récompenses.
     */
    public Uni<Boolean> clearCeremoniesAwards(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_CEREMONY_AWARDS))
                                                        .invoke(ceremonyAwardsSet -> movie.clearCeremoniesAwards())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            return new WebApplicationException("Erreur lors de la suppression des cérémonies", throwable);
                        });
    }

    /**
     * Récupère et mappe la liste des acteurs d’un film en DTO triés par rang.
     * <p>
     * Cette méthode charge la liste des acteurs associée au film fourni, la convertit en {@link MovieActorDTO} (sans inclure
     * les informations du film), puis trie les DTO par leur rang ({@code rank}) en plaçant les valeurs nulles à la fin.
     * <p>
     * Si la liste des acteurs est {@code null}, une {@link WebApplicationException} est levée.
     *
     * @param movie Le film dont les acteurs doivent être récupérés. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la liste triée des {@link MovieActorDTO}.
     * @throws WebApplicationException si la liste des acteurs du film est {@code null}.
     */
    public Uni<List<MovieActorDTO>> fetchAndMapActorList(Movie movie) {
        return
                Mutiny.fetch(movie.getMovieActors())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_ACTORS))
                        .map(movieActorList ->
                                movieActorMapper.toDTOListWithoutMovie(movieActorList)
                                        .stream()
                                        .sorted(Comparator.comparing(MovieActorDTO::getRank, Comparator.nullsLast(Integer::compareTo)))
                                        .toList()
                        )
                ;
    }

    /**
     * Récupère et mappe l'ensemble des catégories d’un film en DTO.
     * <p>
     * Cette méthode charge l'ensemble des catégories associées au film fourni, puis les convertit en {@link CategoryDTO}.
     * <p>
     * Si l'ensemble des catégories est {@code null}, une {@link WebApplicationException} est levée.
     *
     * @param movie Le film dont les catégories doivent être récupérées. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l'ensemble des {@link CategoryDTO}.
     * @throws WebApplicationException si l'ensemble des catégories du film est {@code null}.
     */
    public Uni<Set<CategoryDTO>> fetchAndMapCategorySet(Movie movie) {
        return
                Mutiny.fetch(movie.getCategories())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_CATEGORIES))
                        .map(categoryMapper::toDTOSet)
                ;
    }

    /**
     * Récupère et mappe l'ensemble des pays d’un film en DTO.
     * <p>
     * Cette méthode charge l'ensemble des pays associés au film fourni, puis les convertit en {@link CountryDTO}.
     * <p>
     * Si l'ensemble des pays est {@code null}, une {@link WebApplicationException} est levée.
     *
     * @param movie Le film dont les pays doivent être récupérés. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant l'ensemble des {@link CountryDTO}.
     * @throws WebApplicationException si l'ensemble des pays du film est {@code null}.
     */
    public Uni<Set<CountryDTO>> fetchAndMapCountrySet(Movie movie) {
        return
                Mutiny.fetch(movie.getCountries())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.NULL_COUNTRIES))
                        .map(countryMapper::toDTOSet)
                ;
    }

}
