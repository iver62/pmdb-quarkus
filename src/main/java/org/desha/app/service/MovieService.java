package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    private final MovieMapper movieMapper;
    private final MovieActorMapper movieActorMapper;
    private final MovieTechnicianMapper movieTechnicianMapper;
    private final PersonMapper personMapper;

    private final AwardService awardService;
    private final CeremonyAwardsService ceremonyAwardsService;
    private final CountryService countryService;
    private final FileService fileService;
    private final CategoryService categoryService;
    private final NotificationService notificationService;
    private final StatsService statsService;
    private final UserNotificationService userNotificationService;

    private final CeremonyAwardsRepository ceremonyAwardsRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    @Inject
    public MovieService(
            CategoryMapper categoryMapper,
            CeremonyAwardsMapper ceremonyAwardsMapper,
            CountryMapper countryMapper,
            MovieMapper movieMapper,
            MovieActorMapper movieActorMapper,
            MovieTechnicianMapper movieTechnicianMapper,
            PersonMapper personMapper,
            CeremonyAwardsRepository ceremonyAwardsRepository,
            AwardService awardService,
            CeremonyAwardsService ceremonyAwardsService,
            CountryService countryService,
            CategoryRepository categoryRepository,
            CountryRepository countryRepository,
            FileService fileService,
            CategoryService categoryService,
            NotificationService notificationService,
            StatsService statsService,
            UserNotificationService userNotificationService,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            UserRepository userRepository
    ) {
        this.categoryMapper = categoryMapper;
        this.ceremonyAwardsMapper = ceremonyAwardsMapper;
        this.countryMapper = countryMapper;
        this.movieMapper = movieMapper;
        this.movieActorMapper = movieActorMapper;
        this.movieTechnicianMapper = movieTechnicianMapper;
        this.personMapper = personMapper;
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

    public Uni<Long> count(CriteriaDTO criteriaDTO) {
        return movieRepository.countMovies(criteriaDTO);
    }

    public Uni<Long> countPersonsByMovie(Long id, CriteriaDTO criteriaDTO) {
        return personRepository.countPersonsByMovie(id, criteriaDTO);
    }

    public Uni<Long> countCountriesInMovies(String term, String lang) {
        return countryRepository.countCountriesInMovies(term, lang);
    }

    public Uni<Long> countCategoriesInMovies(String term) {
        return categoryRepository.countCategoriesInMovies(term);
    }

    public Uni<MovieDTO> getById(Long id) {
        return
                movieRepository.findByIdWithCountriesAndCategories(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .map(movieMapper::movieToMovieDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération du film", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération du film", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository
                        .findMovies(page, sort, direction, criteriaDTO)
                        .map(movieMapper::movieWithAwardsListToDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des films", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                movieRepository
                        .findMovies(sort, direction, criteriaDTO)
                        .map(movieMapper::movieWithAwardsListToDTOList)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des films", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des films", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<MovieDTO>> getByTitle(String title) {
        return
                movieRepository.list("title", title)
                        .map(movieMapper::movieListToDTOList)
                ;
    }

    public Uni<List<CountryDTO>> getCountriesInMovies(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                Panache.withTransaction(() ->
                        countryRepository.findCountriesInMovies(page, sort, direction, term, lang)
                                .invoke(countries -> log.info("COUNTRIES -> {}", countries))
                                .map(countryMapper::toDTOList)
                                .onFailure().transform(throwable -> {
                                            log.error("Erreur lors de la récupération des pays", throwable);
                                            return new WebApplicationException("Erreur lors de la récupération des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                        }
                                )
                );
    }

    public Uni<List<CategoryDTO>> getCategoriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        return
                categoryRepository.findCategoriesInMovies(page, sort, direction, term)
                        .map(categoryMapper::toDTOList)
//                        .onFailure().transform(throwable -> {
//                                    log.error("Erreur lors de la récupération des catégories", throwable);
//                                    return new WebApplicationException("Erreur lors de la récupération des catégories", Response.Status.INTERNAL_SERVER_ERROR);
//                                }
//                        )
                ;
    }

    public Uni<List<LitePersonDTO>> getPersonsByMovie(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
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

    public Uni<TechnicalTeamDTO> getTechnicalTeam(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findByIdWithTechnicalTeam(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_FILM))
                                        .map(movie ->
                                                TechnicalTeamDTO.build(
                                                        movieTechnicianMapper.toDTOList(movie.getMovieProducers()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieDirectors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieAssistantDirectors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieScreenwriters()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieComposers()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieMusicians()),
                                                        movieTechnicianMapper.toDTOList(movie.getMoviePhotographers()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieCostumeDesigners()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieSetDesigners()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieEditors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieCasters()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieArtists()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieSoundEditors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieVfxSupervisors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieSfxSupervisors()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieMakeupArtists()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieHairDressers()),
                                                        movieTechnicianMapper.toDTOList(movie.getMovieStuntmen())
                                                )
                                        )
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> getActorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .chain(this::fetchAndMapActorList)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération du casting du film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération du casting", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des catégories associées à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les catégories.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CategoryDTO} représentant les catégories du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si l'ensemble des catégories n'est pas initialisé pour ce film.
     */
    public Uni<Set<CategoryDTO>> getCategoriesByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(this::fetchAndMapCategorySet)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des catégories du film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des catégories", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère la liste des pays associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les pays.
     * @return Un {@link Uni} contenant une liste de {@link CountryDTO} représentant les pays du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si l'ensemble des pays n'est pas initialisé pour ce film.
     */
    public Uni<Set<CountryDTO>> getCountriesByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(this::fetchAndMapCountrySet)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des pays du film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des pays", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<Set<CeremonyAwardsDTO>> getCeremoniesAwardsByMovie(Long id) {
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

    public Uni<List<Repartition>> getMoviesCreationDateEvolution() {
        return
                movieRepository.findMoviesCreationDateEvolution()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de l'évolution des films", failure)
                        )
                ;
    }

    public Uni<List<Repartition>> getMoviesCreationDateRepartition() {
        return
                movieRepository.findMoviesByCreationDateRepartition()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de la répartition des films par date de création", failure)
                        )
                ;
    }

    public Uni<List<Repartition>> getMoviesReleaseDateRepartition() {
        return
                movieRepository.findMoviesByReleaseDateRepartition()
                        .onFailure().invoke(failure ->
                                log.error("Erreur lors de la récupération de la répartition des films par date de sortie", failure)
                        )
                ;
    }

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
                                                                    .map(movieMapper::movieToMovieDTO) // Retourne le film après la transaction
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

    public Uni<List<MovieActorDTO>> saveCast(
            Long id,
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
                                    return new WebApplicationException("Erreur lors de la mise à jour du casting", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<CeremonyAwardsDTO> saveCeremonyAwards(Long movieId, CeremonyAwardsDTO ceremonyAwardsDTO) {
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
     * Met à jour les catégories associés à un film donné.
     * <p>
     * Cette méthode associe de nouvelles catégories ou met à jour les catégories existantes
     * d'un film en fonction des identifiants fournis dans {@code categoryDTOSet}.
     * Les catégories sans identifiant sont créées avant d'être associées au film.
     *
     * @param id             L'identifiant du film dont les catégories doivent être mises à jour.
     * @param categoryDTOSet Un ensemble de {@link CategoryDTO} représentant les catégories à associer.
     * @return Un {@link Uni} contenant l'ensemble des catégories mises à jour sous forme de {@link CategoryDTO}.
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     */
    public Uni<Set<CategoryDTO>> saveCategories(Long id, Set<CategoryDTO> categoryDTOSet) {
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
                            return new MovieUpdateException(Messages.ERROR_WHILE_UPDATING_CATEGORIES, throwable);
                        })
                ;
    }

    /**
     * Met à jour les pays associés à un film donné.
     * <p>
     * Cette méthode met à jour les pays associés à un film en fonction des identifiants fournis dans {@code countryDTOSet}.
     *
     * @param id            L'identifiant du film dont les pays doivent être mis à jour.
     * @param countryDTOSet Un ensemble de {@link CountryDTO} représentant les pays à associer.
     * @return Un {@link Uni} contenant l'ensemble des pays mis à jour sous forme de {@link CountryDTO}.
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     */
    public Uni<Set<CountryDTO>> saveCountries(Long id, Set<CountryDTO> countryDTOSet) {
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
                            return new MovieUpdateException(Messages.ERROR_WHILE_UPDATING_COUNTRIES, throwable);
                        })
                ;
    }

    /**
     * Ajoute une liste d'acteurs à un film existant.
     *
     * @param id                L'identifiant du film auquel les acteurs doivent être ajoutés.
     * @param movieActorDTOList Un ensemble d'objets {@link MovieActorDTO} représentant les acteurs à ajouter.
     * @return Un {@link Uni} contenant un {@link List} de {@link MovieActorDTO} mis à jour après l'ajout des acteurs.
     * @throws IllegalArgumentException si le film ou certains acteurs ne sont pas trouvés.
     * @throws IllegalStateException    si la liste des acteurs n'est pas initialisée pour ce film.
     */
    public Uni<List<MovieActorDTO>> addMovieActors(
            Long id,
            List<MovieActorDTO> movieActorDTOList,
            BiFunction<Movie, MovieActorDTO, Uni<MovieActor>> asyncActorFactory
    ) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.ACTORS_NOT_INITIALIZED))
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
     * Ajoute une ou plusieurs catégories à un film existant.
     *
     * @param movieId        L'identifiant du film auquel les catégories doivent être ajoutées.
     * @param categoryDTOSet Un ensemble d'objets {@link CategoryDTO} représentant les catégories à ajouter.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CategoryDTO} mis à jour après l'ajout des catégories.
     * @throws IllegalArgumentException si le film ou certaines catégories ne sont pas trouvés.
     * @throws IllegalStateException    si l'ensemble des catégories n'est pas initialisé pour ce film.
     */
    public Uni<Set<CategoryDTO>> addCategories(Long movieId, Set<CategoryDTO> categoryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.CATEGORIES_NOT_INITIALIZED))
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
     * Ajoute un ou plusieurs pays à un film existant.
     *
     * @param movieId       L'identifiant du film auquel les pays doivent être ajoutés.
     * @param countryDTOSet Un ensemble d'objets {@link CountryDTO} représentant les pays à ajouter.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CountryDTO} mis à jour après l'ajout des pays.
     * @throws IllegalArgumentException si le film ou certains pays ne sont pas trouvés.
     * @throws IllegalStateException    si l'ensemble des pays n'est pas initialisé pour ce film.
     */
    public Uni<Set<CountryDTO>> addCountries(Long movieId, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.COUNTRIES_NOT_INITIALIZED))
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
     * Supprime une association entre un film et un acteur spécifique.
     *
     * @param movieId      L'identifiant du film dont l'acteur doit être retiré.
     * @param movieActorId L'identifiant de l'association acteur-film à supprimer.
     * @return Une instance de {@link Uni} contenant la liste mise à jour des associations film-acteur sous forme de {@link MovieActorDTO}.
     * @throws NotFoundException       Si le film est introuvable.
     * @throws WebApplicationException Si la liste des acteurs du film ne peut pas être initialisée.
     */
    public Uni<List<MovieActorDTO>> removeMovieActor(Long movieId, Long movieActorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.ACTORS_NOT_INITIALIZED))
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
     * Supprime une catégorie d'un film existant.
     *
     * @param movieId    L'identifiant du film dont la catégorie doit être supprimée.
     * @param categoryId L'identifiant de la catégorie à supprimer du film.
     * @return Un {@link Uni} contenant un objet {@link MovieDTO} mis à jour après la suppression de la catégorie.
     * - Provoque une erreur avec un message explicite si le film ou certaines catégories ne sont pas trouvés.
     */
    public Uni<Set<CategoryDTO>> removeCategory(Long movieId, Long categoryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CATEGORIES_NOT_INITIALIZED))
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
     * Supprime l'association entre un film et un pays donné.
     *
     * @param movieId   L'identifiant du film dont on veut retirer un pays associé.
     * @param countryId L'identifiant du pays à dissocier du film.
     * @return Un {@link Uni} contenant un {@link MovieDTO} mis à jour après suppression de l'association.
     * - Provoque une erreur si le film n'est pas trouvé.
     */
    public Uni<Set<CountryDTO>> removeCountry(Long movieId, Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.COUNTRIES_NOT_INITIALIZED))
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
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de vider la collection des récompenses associées à un film en supprimant toutes les entrées
     * de cette collection. Elle effectue cette opération dans une transaction et persiste les changements
     * dans la base de données. Si le film avec l'ID spécifié n'existe pas, une exception est levée.
     *
     * @param movieId L'identifiant du film pour lequel les récompenses doivent être supprimées.
     * @return Un {@link Uni} contenant {@code true} si la suppression des récompenses a réussi,
     * ou une exception sera levée en cas d'erreur.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des récompenses (par exemple,
     *                                 en cas de film introuvable ou d'erreur de persistance).
     */
    public Uni<Set<CeremonyAwardsDTO>> removeCeremonyAwards(Long movieId, Long ceremonyAwardsId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CEREMONY_AWARDS_NOT_INITIALIZED))
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

    public Uni<MovieDTO> updateMovie(Long id, FileUpload file, MovieDTO movieDTO) {
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
                                        .map(movieMapper::movieToMovieDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la modification du film", throwable);
                                    return new WebApplicationException("Erreur lors de la modification du film", Response.Status.INTERNAL_SERVER_ERROR);
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
     * Supprime un film par son identifiant.
     * <p>
     * Cette méthode effectue la suppression d'un film dans une transaction.
     * Si l'identifiant fourni ne correspond à aucun film, la suppression échoue
     * et retourne false.
     *
     * @param id L'identifiant du film à supprimer.
     * @return Un {@link Uni} contenant `true` si la suppression a réussi,
     * `false` si aucun film avec cet identifiant n'existe.
     */
    public Uni<Boolean> deleteMovie(Long id) {
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

    public Uni<Boolean> clearActors(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie -> Mutiny.fetch(movie.getMovieActors())
                                                .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.ACTORS_NOT_INITIALIZED))
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
                            log.error("Erreur lors de la suppression des acteurs", throwable);
                            return new WebApplicationException("Erreur lors de la suppression des acteurs", Response.Status.INTERNAL_SERVER_ERROR);
                        });
    }

    /**
     * Supprime toutes les catégories associées à un film donné.
     * <p>
     * Cette méthode permet de vider la collection des catégories associée à un film en supprimant toutes les entrées
     * de cette collection. Elle effectue cette opération dans une transaction et persiste les changements
     * dans la base de données. Si le film avec l'ID spécifié n'existe pas, une exception est levée.
     *
     * @param id L'identifiant du film pour lequel les catégories doivent être supprimées.
     * @return Un {@link Uni} contenant {@code true} si la suppression des catégories a réussi,
     * ou une exception sera levée en cas d'erreur.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des catégories (par exemple,
     *                                 en cas de film introuvable ou d'erreur de persistance).
     */
    public Uni<Boolean> clearCategories(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CATEGORIES_NOT_INITIALIZED))
                                                        .invoke(categorySet -> movie.clearCategories())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            return new WebApplicationException("Erreur lors de la suppression des catégories", throwable);
                        });
    }

    /**
     * Supprime tous les pays associés à un film donné.
     * <p>
     * Cette méthode permet de vider la collection des pays associés à un film en supprimant toutes les entrées
     * de cette collection. Elle effectue cette opération dans une transaction et persiste les changements
     * dans la base de données. Si le film avec l'ID spécifié n'existe pas, une exception est levée.
     *
     * @param id L'identifiant du film pour lequel les pays doivent être supprimés.
     * @return Un {@link Uni} contenant {@code true} si la suppression des pays a réussi,
     * ou une exception sera levée en cas d'erreur.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des pays (par exemple,
     *                                 en cas de film introuvable ou d'erreur de persistance).
     */
    public Uni<Boolean> clearCountries(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .invoke(countries -> movie.clearCountries())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            return new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    public Uni<Boolean> clearCeremoniesAwards(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CEREMONY_AWARDS_NOT_INITIALIZED))
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

    public Uni<List<MovieActorDTO>> fetchAndMapActorList(Movie movie) {
        return
                Mutiny.fetch(movie.getMovieActors())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.ACTORS_NOT_INITIALIZED))
                        .map(movieActorList ->
                                movieActorMapper.toDTOListWithoutMovie(movieActorList)
                                        .stream()
                                        .sorted(Comparator.comparing(MovieActorDTO::getRank, Comparator.nullsLast(Integer::compareTo)))
                                        .toList()
                        )
                ;
    }

    /**
     * Récupère et convertit les catégories associées à un film en objets {@link CategoryDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les catégories d'un film
     * et les transformer en un ensemble de DTOs. Si la liste des catégories est null,
     * une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble de {@link CategoryDTO}.
     * @throws IllegalStateException si l'ensemble des catégories n'est pas initialisé.
     */
    public Uni<Set<CategoryDTO>> fetchAndMapCategorySet(Movie movie) {
        return
                Mutiny.fetch(movie.getCategories())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.CATEGORIES_NOT_INITIALIZED))
                        .map(categoryMapper::toDTOSet)
                ;
    }

    /**
     * Récupère et convertit les pays associés à un film en objets {@link CountryDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les pays liés à un film
     * et les transformer en un ensemble de DTOs. Si la liste des pays est null,
     * une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble de {@link CountryDTO}.
     * @throws IllegalStateException si la liste des pays n'est pas initialisée.
     */
    public Uni<Set<CountryDTO>> fetchAndMapCountrySet(Movie movie) {
        return
                Mutiny.fetch(movie.getCountries())
                        .onItem().ifNull().failWith(() -> new WebApplicationException(Messages.COUNTRIES_NOT_INITIALIZED))
                        .map(countryMapper::toDTOSet)
                ;
    }

}
