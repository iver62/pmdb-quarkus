package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.record.Repartition;
import org.desha.app.repository.*;
import org.desha.app.utils.Messages;
import org.desha.app.utils.Utils;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieService {

    private static final String POSTERS_DIR = "posters/";
    private static final String DEFAULT_POSTER = "default-poster.jpg";

    private final AwardService awardService;
    private final CeremonyAwardsService ceremonyAwardsService;
    private final CountryService countryService;
    private final FileService fileService;
    private final CategoryService categoryService;
    private final PersonService personService;
    private final StatsService statsService;

    private final CeremonyAwardsRepository ceremonyAwardsRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    @Inject
    public MovieService(
            CeremonyAwardsRepository ceremonyAwardsRepository,
            AwardService awardService,
            CeremonyAwardsService ceremonyAwardsService,
            CountryService countryService,
            CountryRepository countryRepository,
            FileService fileService,
            CategoryService categoryService,
            PersonService personService,
            StatsService statsService,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            UserRepository userRepository
    ) {
        this.ceremonyAwardsRepository = ceremonyAwardsRepository;
        this.awardService = awardService;
        this.ceremonyAwardsService = ceremonyAwardsService;
        this.countryService = countryService;
        this.countryRepository = countryRepository;
        this.fileService = fileService;
        this.categoryService = categoryService;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.personService = personService;
        this.statsService = statsService;
    }

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        return movieRepository.countMovies(criteriasDTO);
    }

    public Uni<Long> countPersonsByMovie(long id, CriteriasDTO criteriasDTO) {
        return personRepository.countPersonsByMovie(id, criteriasDTO);
    }

    public Uni<Long> countCountriesInMovies(String term, String lang) {
        return countryRepository.countCountriesInMovies(term, lang);
    }

    public Uni<MovieDTO> getById(Long id) {
        return
                movieRepository.findByIdWithCountriesAndCategories(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .map(movie -> MovieDTO.of(movie, movie.getCategories(), movie.getCountries()))
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMovies(page, sort, direction, criteriasDTO)
                        .map(movieWithAwardsNumberList ->
                                movieWithAwardsNumberList
                                        .stream()
                                        .map(movieWithAwardsNumber ->
                                                MovieDTO.of(
                                                        movieWithAwardsNumber.movie(),
                                                        movieWithAwardsNumber.awardsNumber()
                                                )
                                        )
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMovies(sort, direction, criteriasDTO)
                        .map(
                                movieWithAwardsNumberList ->
                                        movieWithAwardsNumberList
                                                .stream()
                                                .map(movieWithAwardsNumber ->
                                                        MovieDTO.of(
                                                                movieWithAwardsNumber.movie(),
                                                                movieWithAwardsNumber.awardsNumber()
                                                        )
                                                )
                                                .toList()
                        )
                ;
    }

    public Uni<List<Movie>> getByTitle(String title) {
        return movieRepository.list("title", title);
    }

    public Uni<List<MovieDTO>> searchByTitle(String query) {
        return
                movieRepository.searchByTitle(query.trim())
                        .onItem().ifNotNull()
                        .transform(movieList ->
                                movieList.stream()
                                        .map(MovieDTO::of)
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<List<CountryDTO>> getCountriesInMovies(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findCountriesInMovies(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::of)
                                                .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getPersonsByMovie(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .flatMap(movie ->
                                personRepository.findPersonsByMovie(id, page, sort, direction, criteriasDTO)
                                        .map(personList -> personService.fromPersonListEntity(personList, PersonDTO::of))
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> getActorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .chain(movie ->
                                Mutiny.fetch(movie.getMovieActors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.ACTORS_NOT_INITIALIZED))
                                        .flatMap(movieActorList -> movie.fetchAndMapActorList())
                        )

                ;
    }

    /**
     * Récupère un ensemble de techniciens associés à un film spécifique.
     *
     * @param id                L'identifiant du film.
     * @param techniciansGetter Fonction permettant de récupérer le bon ensemble de techniciens depuis l'entité {@link Movie}.
     * @param errorMessage      Message d'erreur en cas de liste non initialisée.
     * @return Un {@link Uni} contenant un {@link Set} de {@link PersonDTO} correspondant aux techniciens du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si la liste des techniciens n'est pas initialisée pour ce film.
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> getMovieTechniciansByMovie(Long id, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .flatMap(movie ->
                                Mutiny.fetch(techniciansGetter.apply(movie))
                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                        .map(ts ->
                                                ts.stream()
                                                        .map(MovieTechnicianDTO::of)
                                                        .toList()
                                        )
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .flatMap(Movie::fetchAndMapCategorySet)
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getCountries())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés pour ce film"))
                                        .map(CountryDTO::fromCountryEntitySet)
                        )
                ;
    }

    public Uni<TechnicalTeamDTO> getTechnicalTeam(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findByIdWithTechnicalTeam(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .map(movie ->
                                                TechnicalTeamDTO.build(
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieProducers),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieDirectors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieAssistantDirectors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieScreenwriters),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieComposers),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieMusicians),
                                                        personService.fromMovieTechnicianListEntity(movie::getMoviePhotographers),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieCostumeDesigners),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieSetDesigners),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieEditors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieCasters),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieArtists),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieSoundEditors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieVfxSupervisors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieSfxSupervisors),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieMakeupArtists),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieHairDressers),
                                                        personService.fromMovieTechnicianListEntity(movie::getMovieStuntmen)
                                                )
                                        )
                        )
                ;
    }

    public Uni<Set<CeremonyAwardsDTO>> getCeremoniesAwardsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .flatMap(movie -> ceremonyAwardsRepository.findCeremoniesAwardsByMovie(movie.getId()))
                        .map(CeremonyAwardsDTO::fromEntityList)
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
            return fileService.getFile(POSTERS_DIR, DEFAULT_POSTER);
        }

        return fileService.getFile(POSTERS_DIR, fileName)
                .onFailure(FileNotFoundException.class).recoverWithUni(() -> {
                    log.warn("Poster {} not found, returning default poster.", fileName);
                    return fileService.getFile(POSTERS_DIR, DEFAULT_POSTER);
                });
    }

    private Uni<String> uploadPoster(FileUpload file) {
        if (Objects.isNull(file) || Objects.isNull(file.uploadedFile()) || file.fileName().isBlank()) {
            log.warn("Invalid or missing file. Using default poster.");
            return Uni.createFrom().item(DEFAULT_POSTER);
        }

        return fileService.uploadFile(POSTERS_DIR, file)
                .onFailure().recoverWithItem(error -> {
                    log.error("Poster upload failed: {}", error.getMessage());
                    return DEFAULT_POSTER;
                });
    }

    public Uni<MovieDTO> saveMovie(FileUpload file, MovieDTO movieDTO) {
        return
                movieRepository.movieExists(movieDTO.getTitle(), movieDTO.getOriginalTitle())
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return Uni.createFrom().failure(new WebApplicationException("Le film existe déjà.", 409));
                            }

                            Movie movie = Movie.of(movieDTO);

                            return Panache.withTransaction(() ->
                                    // Récupérer les pays et les catégories
                                    countryService.getByIds(movieDTO.getCountries())
                                            .invoke(movie::setCountries)
                                            .chain(() -> categoryService.getByIds(movieDTO.getCategories()))
                                            .invoke(movie::setCategories)
                                            .chain(() ->
                                                    userRepository.findById(movieDTO.getUser().getId())
                                                            .invoke(user -> log.info("Movie created by {}", user.getUsername()))
                                            )
                                            .invoke(movie::setUser)
                                            .chain(() -> {
                                                if (Objects.nonNull(file)) {
                                                    return uploadPoster(file)
                                                            .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", movie.getTitle(), error.getMessage()))
                                                            .invoke(movie::setPosterFileName);
                                                }
                                                movie.setPosterFileName(DEFAULT_POSTER);
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
                                                            .replaceWith(entity)
                                            )
                                            .map(entity -> MovieDTO.of(entity, entity.getCategories(), entity.getCountries())) // Retourne le film après la transaction
                            );
                        });
    }

    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> saveTechnicians(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory,
            String errorMessage
    ) {
        return Panache.withTransaction(() ->
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                        .chain(movie ->
                                Mutiny.fetch(techniciansGetter.apply(movie))
                                        .invoke(existingTechnicians -> movie.removeObsoleteTechnicians(existingTechnicians, movieTechnicianDTOList)) // Supprimer les techniciens obsolètes
                                        .invoke(existingTechnicians -> movie.updateExistingTechnicians(existingTechnicians, movieTechnicianDTOList)) // Mettre à jour les techniciens existants
                                        .chain(existingTechnicians -> movie.addTechnicians(movieTechnicianDTOList, techniciansGetter, asyncTechnicianFactory)) // Ajouter les nouveaux techniciens
                                        .replaceWith(movie)
                        )
                        .chain(movieRepository::persist)
                        .call(movieActorRepository::flush) // Force la génération des IDs
                        .flatMap(movie -> movie.fetchAndMapTechniciansList(techniciansGetter, errorMessage))
        );
    }

    public Uni<List<MovieActorDTO>> saveCast(
            Long id,
            List<MovieActorDTO> movieActorsDTOList,
            BiFunction<Movie, MovieActorDTO, Uni<MovieActor>> asyncActorFactory
    ) {
        return
                Panache.withTransaction(() ->
                        movieRepository.findById(id)
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                                .flatMap(Movie::fetchAndMapActorList) // Convertit les entités en DTO
                );
    }

    public Uni<CeremonyAwardsDTO> saveCeremonyAwards(Long movieId, CeremonyAwardsDTO ceremonyAwardsDTO) {
        return
                Panache.withTransaction(() ->
                        movieRepository.findById(movieId)
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                                                                        .orElseGet(() -> ceremonyAwardsService.createNewCeremonyAwards(movie, ceremonyAwardsDTO, personMap) // Sinon on le crée et on l'ajoute à l'ensemble
                                                                                .invoke(ceremonyAwards -> movie.getCeremoniesAwards().add(ceremonyAwards))
                                                                        )
                                                                )
                                                )
                                                .call(ceremonyAwardsRepository::persist)
                                                .call(ceremonyAwardsRepository::flush)
                                                .call(() -> movieRepository.persist(movie))
                                                .map(ceremonyAwards -> CeremonyAwardsDTO.of(ceremonyAwards, ceremonyAwards.getAwards()))
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                                        .flatMap(Movie::fetchAndMapCategorySet)
                        );
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                                        .flatMap(Movie::fetchAndMapCountrySet)
                        );
    }

    /**
     * Ajoute des personnes à un film en fonction d'un ensemble de DTO et d'un service associé.
     *
     * @param id                     L'identifiant du film auquel les personnes doivent être ajoutées.
     * @param movieTechnicianDTOList L'ensemble des personnes à ajouter, sous forme de DTO.
     * @param techniciansGetter      Une fonction permettant de récupérer l'ensemble des personnes déjà associées au film.
     * @param errorMessage           Le message d'erreur à utiliser en cas d'échec de l'opération.
     * @return Une instance de {@link Uni} contenant l'ensemble des personnes ajoutées sous forme de {@link PersonDTO}.
     * En cas d'erreur, une exception est levée avec un message approprié.
     * @throws IllegalArgumentException Si le film n'est pas trouvé ou si certaines personnes sont introuvables.
     * @throws IllegalStateException    Si une erreur se produit lors de la récupération des personnes après la mise à jour.
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> addTechnicians(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory,
            String errorMessage
    ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                                        .chain(existingTechnicians -> movie.addTechnicians(movieTechnicianDTOList, techniciansGetter, asyncTechnicianFactory)) // Ajouter les nouveaux techniciens
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieActorRepository::flush) // Force la génération des IDs
                                        .flatMap(movie -> movie.fetchAndMapTechniciansList(techniciansGetter, errorMessage))
                        )
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
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                .chain(movie ->
                                        Mutiny.fetch(movie.getMovieActors())
                                                .chain(existingActors -> movie.addMovieActors(movieActorDTOList, asyncActorFactory)) // Ajouter les nouveaux acteurs
                                                .replaceWith(movie)
                                )
                                .chain(movieRepository::persist)
                                .call(movieActorRepository::flush) // Force la génération des IDs
                                .call(statsService::updateActorsStats)
                                .flatMap(Movie::fetchAndMapActorList) // Convertit les entités en DTO
                );
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CATEGORIES_NOT_INITIALIZED))
                                                        .chain(categorySet ->
                                                                categoryService.getByIds(categoryDTOSet.stream().map(CategoryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Une ou plusieurs catégories sont introuvables"))
                                                        )
                                                        .invoke(movie::addCategories)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .flatMap(Movie::fetchAndMapCategorySet)
                                        .invoke(() -> log.info("Catégories ajoutées au film {}", movieId))
                        )
                        .onFailure().invoke(e -> log.error("Erreur lors de l'ajout des catégories au film {} : {}", movieId, e.getMessage()))
                ;
    }

    /**
     * Ajoute un ou plusieurs pays à un film existant.
     *
     * @param movieId       L'identifiant du film auquel associer les pays.
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .chain(countrySet ->
                                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                        )
                                                        .invoke(movie::addCountries)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(Movie::fetchAndMapCountrySet)
                                        .invoke(() -> log.info("Pays ajoutés au film {}", movieId))
                        )
                        .onFailure().invoke(e -> log.error("Erreur lors de l'ajout des pays au film {} : {}", movieId, e.getMessage()))
                ;
    }

    /**
     * Retire une personne spécifique d'un film.
     *
     * @param movieId           L'identifiant du film.
     * @param personId          L'identifiant de la personne à retirer.
     * @param techniciansGetter Fonction permettant d'obtenir la liste des personnes à modifier depuis l'entité {@link Movie}.
     * @param errorMessage      Message d'erreur si la liste des personnes n'est pas initialisée.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} :
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws IllegalStateException    si la collection de personnes n'est pas initialisée pour ce film.
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> removeTechnician(
            Long movieId,
            Long personId,
            Function<Movie, List<T>> techniciansGetter,
            String errorMessage
    ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .call(movie -> movie.removeTechnician(techniciansGetter, personId, errorMessage))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                                        .map(personService::fromMovieTechnicianListEntity)
                                        )
                        )
                ;
    }

    /**
     * Supprime une association entre un film et un acteur spécifique.
     *
     * @param movieId      L'identifiant du film dont l'acteur doit être retiré.
     * @param movieActorId L'identifiant de l'association acteur-film à supprimer.
     * @return Une instance de {@link Uni} contenant la liste mise à jour des associations film-acteur sous forme de {@link MovieActorDTO}.
     * @throws IllegalArgumentException Si le film est introuvable.
     * @throws IllegalStateException    Si la liste des acteurs du film ne peut pas être initialisée.
     */
    public Uni<List<MovieActorDTO>> removeMovieActor(Long movieId, Long movieActorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.ACTORS_NOT_INITIALIZED))
                                                        .invoke(movieActorList -> movie.removeMovieActor(movieActorId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.ACTORS_NOT_INITIALIZED))
                                                        .map(personService::fromMovieActorListEntity)
                                        )
                        )
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCategories())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CATEGORIES_NOT_INITIALIZED))
                                                        .invoke(categorySet -> movie.removeCategory(categoryId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCategoryRepartition().replaceWith(movie))
                                        .flatMap(Movie::fetchAndMapCategorySet)
                                        .invoke(() -> log.info("Category {} removed from movie {}", categoryId, movieId))
                        )
                        .onFailure().invoke(e -> log.error("Failed to remove category from movie {}: {}", movieId, e.getMessage()))
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .invoke(countries -> movie.removeCountry(countryId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(Movie::fetchAndMapCountrySet)
                                        .invoke(() -> log.info("Country {} removed from movie {}", countryId, movieId))
                        )
                        .onFailure().invoke(e -> log.error("Failed to remove country from movie {}: {}", movieId, e.getMessage()))
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
    public Uni<Boolean> removeCeremonyAwards(Long movieId, Long ceremonyAwardsId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CEREMONY_AWARDS_NOT_INITIALIZED))
                                                        .invoke(ceremonyAwardsSet -> movie.removeCeremonyAward(ceremonyAwardsId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression de la cérémonie", throwable);
                        });
    }

    public Uni<MovieDTO> updateMovie(Long id, FileUpload file, MovieDTO movieDTO) {
        return
                Panache.withTransaction(() ->
                        movieRepository.findById(id)
                                .onItem().ifNull().failWith(() -> new NotFoundException(Messages.FILM_NOT_FOUND))
                                .invoke(movie -> movie.updateGeneralInfos(movieDTO))
                                .chain(movie -> {
                                    if (Objects.nonNull(file)) {
                                        return uploadPoster(file)
                                                .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", id, error.getMessage()))
                                                .invoke(movie::setPosterFileName)
                                                .replaceWith(movie);
                                    }
                                    return Uni.createFrom().item(movie);
                                })
                                .chain(movie -> updateCategoriesIfNeeded(movie, movieDTO))
                                .chain(movie -> updateCountriesIfNeeded(movie, movieDTO))
                                .chain(movie -> updateReleaseDateIfNeeded(movie, movieDTO))
                                .map(movie ->
                                        MovieDTO.of(
                                                movie,
                                                movie.getCategories(),
                                                movie.getCountries()
                                        )
                                )
                );
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
     * et retourne `false`.
     *
     * @param id L'identifiant du film à supprimer.
     * @return Un {@link Uni} contenant `true` si la suppression a réussi,
     * `false` si aucun film avec cet identifiant n'existe.
     */
    public Uni<Boolean> deleteMovie(Long id) {
        return
                Panache.withTransaction(() ->
                                movieRepository.deleteById(id)
                                        .flatMap(aBoolean -> statsService.decrementNumberOfMovies().replaceWith(aBoolean))
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression du film", throwable);
                        });
    }

    /**
     * Vide un ensemble de personnes (acteurs, réalisateurs, etc.) associé à un film.
     * <p>
     * Cette méthode permet de vider un ensemble spécifique de personnes associées à un film (comme les acteurs, réalisateurs, etc.).
     * Elle récupère cet ensemble en appliquant une fonction (`peopleGetter`) sur le film et, si l'ensemble est initialisé,
     * elle appelle la méthode `clearPersons` sur le film pour le vider. Si l'ensemble est nul ou si le film n'est pas trouvé,
     * une exception est levée. Enfin, le film est persisté après l'opération.
     *
     * @param id                L'identifiant du film dont les personnes associées doivent être supprimées.
     * @param techniciansGetter Une fonction permettant d'obtenir l'ensemble des personnes à partir du film (par exemple, acteurs ou réalisateurs).
     * @param errorMessage      Le message d'erreur à utiliser si l'ensemble des personnes est nul.
     * @return Un {@link Uni} contenant `true` si l'opération a été réalisée avec succès.
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws WebApplicationException  Si une erreur se produit lors de la suppression des personnes.
     */
    public <T extends MovieTechnician> Uni<Boolean> clearTechnicians(Long id, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .call(movie -> movie.clearPersons(techniciansGetter.apply(movie), errorMessage))
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des personnes", throwable);
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                            throw new WebApplicationException("Erreur lors de la suppression des catégories", throwable);
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
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
                            throw new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    public Uni<Boolean> clearCeremonyAwards(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.FILM_NOT_FOUND))
                                        .chain(movie ->
                                                Mutiny.fetch(movie.getCeremoniesAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.CEREMONY_AWARDS_NOT_INITIALIZED))
                                                        .invoke(ceremonyAwards -> movie.clearCeremonyAwards())
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des cérémonies", throwable);
                        });
    }

}
