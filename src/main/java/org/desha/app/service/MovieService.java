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
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.repository.*;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieService {

    private final AwardRepository awardRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final UserRepository userRepository;
    private final AwardService awardService;
    private final CountryService countryService;
    private final GenreService genreService;
    private final FileService fileService;

    private final ActorService actorService;
    private final ArtDirectorService artDirectorService;
    private final CasterService casterService;
    private final CostumierService costumierService;
    private final DecoratorService decoratorService;
    private final DirectorService directorService;
    private final EditorService editorService;
    private final HairDresserService hairDresserService;
    private final MakeupArtistService makeupArtistService;
    private final MusicianService musicianService;
    private final PhotographerService photographerService;
    private final ProducerService producerService;
    private final ScreenwriterService screenwriterService;
    private final SoundEditorService soundEditorService;
    private final VisualEffectsSupervisorService visualEffectsSupervisorService;
    private final StuntmanService stuntmanService;

    private static final String POSTERS_DIR = "posters/";
    private static final String DEFAULT_POSTER = "default-poster.jpg";

    @Inject
    public MovieService(
            AwardRepository awardRepository,
            AwardService awardService,
            CountryService countryService,
            CountryRepository countryRepository,
            FileService fileService,
            GenreService genreService,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            UserRepository userRepository,
            ActorService actorService,
            ArtDirectorService artDirectorService,
            CasterService casterService,
            CostumierService costumierService,
            DecoratorService decoratorService,
            DirectorService directorService,
            EditorService editorService,
            HairDresserService hairDresserService,
            MakeupArtistService makeupArtistService,
            MusicianService musicianService,
            PhotographerService photographerService,
            ProducerService producerService,
            ScreenwriterService screenwriterService,
            SoundEditorService soundEditorService,
            StuntmanService stuntmanService,
            VisualEffectsSupervisorService visualEffectsSupervisorService
    ) {
        this.awardRepository = awardRepository;
        this.awardService = awardService;
        this.countryService = countryService;
        this.countryRepository = countryRepository;
        this.fileService = fileService;
        this.genreService = genreService;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.userRepository = userRepository;
        this.actorService = actorService;
        this.artDirectorService = artDirectorService;
        this.casterService = casterService;
        this.costumierService = costumierService;
        this.decoratorService = decoratorService;
        this.directorService = directorService;
        this.editorService = editorService;
        this.hairDresserService = hairDresserService;
        this.makeupArtistService = makeupArtistService;
        this.musicianService = musicianService;
        this.photographerService = photographerService;
        this.producerService = producerService;
        this.screenwriterService = screenwriterService;
        this.soundEditorService = soundEditorService;
        this.stuntmanService = stuntmanService;
        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
    }

    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        return movieRepository.countMovies(criteriasDTO);
    }

    public Uni<Long> countCountriesInMovies(String term) {
        return countryRepository.countCountriesInMovies(term);
    }

    public Uni<Movie> getById(Long id) {
        return
                movieRepository.findByIdWithCountriesAndGenres(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMovies(page, sort, direction, criteriasDTO)
                        .map(
                                movieList ->
                                        movieList
                                                .stream()
                                                .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                                .toList()
                        )
                ;
    }

    public Uni<List<Movie>> getByTitle(String title) {
        return movieRepository.list("title", title);
    }

    public Uni<List<Movie>> getAllMovies(String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository.find("LOWER(title) LIKE LOWER(?1)", Sort.by(sort, direction), MessageFormat.format("%{0}%", criteriasDTO.getTerm()))
                        .list()
                ;
    }

    public Uni<List<MovieDTO>> searchByTitle(String query) {
        return
                movieRepository.searchByTitle(query.trim())
                        .onItem().ifNotNull()
                        .transform(movieList ->
                                movieList.stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<List<CountryDTO>> getCountriesInMovies(Page page, String sort, Sort.Direction direction, String term) {
        return
                countryRepository.findCountriesInMovies(page, sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> getActorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMovieActors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Acteurs non initialisés pour ce film"))
                                        .map(actorService::fromMovieActorListEntity)
                        )
                ;
    }

    /**
     * Récupère un ensemble de personnes associées à un film spécifique.
     *
     * @param id           L'identifiant du film.
     * @param peopleGetter Fonction permettant de récupérer le bon ensemble de personnes depuis l'entité {@link Movie}.
     * @param service      Service permettant de convertir les entités en DTO.
     * @param errorMessage Message d'erreur en cas de liste non initialisée.
     * @param <T>          Type de la personne (ex: Producteur, Réalisateur, etc.).
     * @param <S>          Type du service associé à cette personne.
     * @return Un {@link Uni} contenant un {@link Set} de {@link PersonDTO} correspondant aux personnes du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si l'ensemble des personnes n'est pas initialisé pour ce film.
     */
    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> getPeopleByMovie(Long id, Function<Movie, Set<T>> peopleGetter, S service, String errorMessage) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(movie ->
                                Mutiny.fetch(peopleGetter.apply(movie))
                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                        .map(service::fromPersonSetEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des genres associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les genres.
     * @return Un {@link Uni} contenant un {@link Set} de {@link GenreDTO} représentant les genres du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si l'ensemble des genres n'est pas initialisé pour ce film.
     */
    public Uni<Set<GenreDTO>> getGenresByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(this::fetchAndMapGenres)
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getCountries())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés pour ce film"))
                                        .map(countryService::fromCountrySetEntity)
                        )
                ;
    }

    public Uni<TechnicalTeamDTO> getTechnicalTeam(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findByIdWithTechnicalTeam(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .map(movie ->
                                                TechnicalTeamDTO.build(
                                                        movie.getProducers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getDirectors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getScreenwriters().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getMusicians().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getPhotographers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getCostumiers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getDecorators().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getEditors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getCasters().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getArtDirectors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getSoundEditors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getVisualEffectsSupervisors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getMakeupArtists().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getHairDressers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                                                        movie.getStuntmen().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet())
                                                )
                                        )
                        )
                ;
    }

    public Uni<List<RepartitionDTO>> getMoviesCreationDateEvolution() {
        return movieRepository.findMoviesCreationDateEvolution()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de l'évolution des films", failure)
                );
    }

    public Uni<List<RepartitionDTO>> getMoviesCreationDateRepartition() {
        return movieRepository.findMoviesByCreationDateRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par date de création", failure)
                );
    }

    public Uni<List<RepartitionDTO>> getMoviesReleaseDateRepartition() {
        return movieRepository.findMoviesByReleaseDateRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par date de sortie", failure)
                );
    }

    public Uni<List<RepartitionDTO>> getMoviesGenresRepartition() {
        return movieRepository.findMoviesByGenreRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par genre", failure)
                );
    }

    public Uni<List<RepartitionDTO>> getMoviesCountriesRepartition() {
        return movieRepository.findMoviesByCountryRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par pays", failure)
                );
    }

    public Uni<List<RepartitionDTO>> getMoviesUsersRepartition() {
        return movieRepository.findMoviesByUserRepartition();
    }

    public Uni<Set<Award>> getAwardsByMovie(Long id) {
        return
                getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas")) // 404 si le film n'existe pas
                        .chain(movie -> Mutiny.fetch(movie.getAwards()));
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

    public Uni<Movie> saveMovie(FileUpload file, MovieDTO movieDTO) {
        return movieRepository.movieExists(movieDTO.getTitle().trim(), movieDTO.getReleaseDate().getYear())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Uni.createFrom().failure(new WebApplicationException("Le film existe déjà.", 409));
                    }

                    Movie movie = Movie.fromDTO(movieDTO);

                    return Panache.withTransaction(() ->
                            // Récupérer les pays et les genres en parallèle
                            countryService.getByIds(movieDTO.getCountries())
                                    .invoke(movie::setCountries)
                                    .chain(() ->
                                            genreService.getByIds(movieDTO.getGenres())
                                                    .invoke(movie::setGenres)
                                    )
                                    .chain(() ->
                                            userRepository.findById(movieDTO.getUser().getId())
                                                    .invoke(user -> log.info("USER -> " + user))
                                                    .invoke(movie::setUser)
                                    )
                                    .chain(() -> {
                                        if (Objects.nonNull(file)) {
                                            return uploadPoster(file)
                                                    .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", movie.getTitle(), error.getMessage()))
                                                    .invoke(movie::setPosterFileName);
                                        }
                                        movie.setPosterFileName(DEFAULT_POSTER);
                                        return Uni.createFrom().voidItem();
                                    })
                                    .chain(movie::persist)
                                    .replaceWith(movie) // Retourne le film après la transaction
                    );
                });
    }

    public Uni<TechnicalTeamDTO> saveTechnicalTeam(Long id, TechnicalTeamDTO technicalTeam) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(
                                                movie ->
                                                        producerService.getByIds(technicalTeam.getProducers()).invoke(movie::setProducers)
                                                                .chain(() -> directorService.getByIds(technicalTeam.getDirectors()).invoke(movie::setDirectors))
                                                                .chain(() -> screenwriterService.getByIds(technicalTeam.getScreenwriters()).invoke(movie::setScreenwriters))
                                                                .chain(() -> musicianService.getByIds(technicalTeam.getMusicians()).invoke(movie::setMusicians))
                                                                .chain(() -> photographerService.getByIds(technicalTeam.getPhotographers()).invoke(movie::setPhotographers))
                                                                .chain(() -> costumierService.getByIds(technicalTeam.getCostumiers()).invoke(movie::setCostumiers))
                                                                .chain(() -> decoratorService.getByIds(technicalTeam.getDecorators()).invoke(movie::setDecorators))
                                                                .chain(() -> editorService.getByIds(technicalTeam.getEditors()).invoke(movie::setEditors))
                                                                .chain(() -> casterService.getByIds(technicalTeam.getCasters()).invoke(movie::setCasters))
                                                                .chain(() -> artDirectorService.getByIds(technicalTeam.getArtDirectors()).invoke(movie::setArtDirectors))
                                                                .chain(() -> soundEditorService.getByIds(technicalTeam.getSoundEditors()).invoke(movie::setSoundEditors))
                                                                .chain(() -> visualEffectsSupervisorService.getByIds(technicalTeam.getVisualEffectsSupervisors()).invoke(movie::setVisualEffectsSupervisors))
                                                                .chain(() -> makeupArtistService.getByIds(technicalTeam.getMakeupArtists()).invoke(movie::setMakeupArtists))
                                                                .chain(() -> hairDresserService.getByIds(technicalTeam.getHairDressers()).invoke(movie::setHairDressers))
                                                                .chain(() -> stuntmanService.getByIds(technicalTeam.getStuntmen()).invoke(movie::setStuntmen))
                                                                .invoke(() -> movie.setLastUpdate(LocalDateTime.now()))
                                        )
                                        .map(TechnicalTeamDTO::fromEntity)
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> saveCasting(Long id, List<MovieActorDTO> movieActorsList) {
        Map<Long, String> roleMap = movieActorsList.stream()
                .collect(Collectors.toMap(dto -> dto.getActor().getId(), MovieActorDTO::getRole));
        Map<Long, Integer> rankMap = movieActorsList.stream()
                .collect(Collectors.toMap(dto -> dto.getActor().getId(), MovieActorDTO::getRank));

        return
                Panache.withTransaction(() ->
                        movieRepository.findById(id)
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                .flatMap(movie ->
                                        Mutiny.fetch(movie.getMovieActors())
                                                .flatMap(existingMovieActors -> {
                                                    // Map existants
                                                    Map<Long, MovieActor> existingActorMap = existingMovieActors.stream()
                                                            .collect(Collectors.toMap(ma -> ma.getActor().getId(), ma -> ma));

                                                    // IDs des nouveaux acteurs à ajouter
                                                    List<Long> newActorIds = new ArrayList<>();

                                                    // MàJ des acteurs existants ou ajout à la liste des nouveaux
                                                    for (MovieActorDTO dto : movieActorsList) {
                                                        if (Objects.isNull(dto.getActor()) || Objects.isNull(dto.getActor().getId())) {
                                                            continue;
                                                        }
                                                        Long actorId = dto.getActor().getId();

                                                        if (existingActorMap.containsKey(actorId)) {
                                                            MovieActor existing = existingActorMap.get(actorId);
                                                            existing.setRole(dto.getRole());
                                                            existing.setRank(dto.getRank());
                                                        } else {
                                                            newActorIds.add(actorId);
                                                        }
                                                    }

                                                    // Supprimer les acteurs absents de la nouvelle liste
                                                    movie.removeMovieActors(
                                                            existingMovieActors.stream()
                                                                    .filter(ma -> movieActorsList.stream()
                                                                            .noneMatch(dto -> dto.getActor().getId().equals(ma.getActor().getId())))
                                                                    .toList()
                                                    );

                                                    return
                                                            actorService.getByIds(newActorIds)
                                                                    .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs acteurs sont introuvables"))
                                                                    .map(actorList ->
                                                                            actorList.stream()
                                                                                    .map(actor ->
                                                                                            MovieActor.build(
                                                                                                    movie,
                                                                                                    actor,
                                                                                                    roleMap.getOrDefault(actor.getId(), "Inconnu").trim(),
                                                                                                    rankMap.getOrDefault(actor.getId(), 0)
                                                                                            )
                                                                                    )
                                                                                    .toList()
                                                                    )
                                                                    .call(movie::addMovieActors)
                                                                    .replaceWith(movie)
                                                            ;
                                                })
                                )
                                .chain(movieRepository::persist)
                                .call(movieActorRepository::flush) // Force la génération des IDs
                                .flatMap(movie ->
                                        Mutiny.fetch(movie.getMovieActors())
                                                .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                                                .map(actorService::fromMovieActorListEntity)
                                )
                );
    }

    /**
     * Met à jour les genres associés à un film donné.
     * <p>
     * Cette méthode associe de nouveaux genres ou met à jour les genres existants
     * d'un film en fonction des identifiants fournis dans {@code genreDTOSet}.
     * Les genres sans identifiant sont créés avant d'être associés au film.
     *
     * @param id          L'identifiant du film dont les genres doivent être mis à jour.
     * @param genreDTOSet Un ensemble de {@link GenreDTO} représentant les genres à associer.
     * @return Un {@link Uni} contenant l'ensemble des genres mis à jour sous forme de {@link GenreDTO}.
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     */
    public Uni<Set<GenreDTO>> saveGenres(Long id, Set<GenreDTO> genreDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .chain(movie -> {
                                            // Les genres existants
                                            List<Long> existingGenreIds = genreDTOSet.stream()
                                                    .map(GenreDTO::getId)
                                                    .filter(Objects::nonNull)
                                                    .toList();

                                            // Les nouveaux genres persistés
                                            List<Uni<Genre>> newGenreUnis = genreDTOSet.stream()
                                                    .filter(genreDTO -> Objects.isNull(genreDTO.getId()))
                                                    .map(genreService::create)
                                                    .toList();

                                            Uni<Set<Genre>> newGenresUni = newGenreUnis.isEmpty()
                                                    ? Uni.createFrom().item(new HashSet<>())  // Retourne un empty set s'il n'y pas de nouveaux genres
                                                    : Uni.join().all(newGenreUnis).andCollectFailures()
                                                    .map(HashSet::new); // Convertit List<Genre> en Set<Genre>

                                            return newGenresUni
                                                    .chain(newGenres ->
                                                            genreService.getByIds(existingGenreIds)
                                                                    .map(existingGenres -> {
                                                                        newGenres.addAll(existingGenres);
                                                                        return newGenres;
                                                                    })
                                                                    .invoke(finalGenreSet -> {
                                                                        movie.setGenres(new HashSet<>(finalGenreSet));
                                                                        movie.setLastUpdate(LocalDateTime.now());
                                                                    })
                                                                    .replaceWith(movie)
                                                    );
                                        })
                                        .flatMap(movieRepository::persist)
                                        .flatMap(this::fetchAndMapGenres)
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
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
                                        .flatMap(movieRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        );
    }

    /**
     * Met à jour la liste des récompenses à un film spécifique.
     * <p>
     * Cette méthode récupère un film par son identifiant et met à jour ses récompenses :
     * - Les récompenses existantes sont mises à jour si elles correspondent à un ID dans la liste fournie.
     * - Les récompenses qui ne sont plus présentes dans la liste fournie sont supprimées.
     * - Les nouvelles récompenses (sans ID) sont ajoutées au film.
     * <p>
     * La transaction est gérée via Panache. Après modification, la liste mise à jour
     * des récompenses est récupérée et convertie en objets {@link AwardDTO}.
     *
     * @param id          L'identifiant du film.
     * @param awardDTOSet L'ensemble des récompenses à sauvegarder.
     * @return Un {@link Uni} contenant la liste mise à jour des {@link AwardDTO}.
     * @throws IllegalArgumentException si le film est introuvable.
     */
    public Uni<Set<AwardDTO>> saveAwards(Long id, Set<AwardDTO> awardDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .chain(
                                                movie ->
                                                        Mutiny.fetch(movie.getAwards())
                                                                .invoke(
                                                                        awards -> {
                                                                            // Modifier les récompenses
                                                                            awards.forEach(award ->
                                                                                    awardDTOSet.stream()
                                                                                            .filter(a -> Objects.nonNull(a.getId()) && a.getId().equals(award.getId()))
                                                                                            .findFirst()
                                                                                            .ifPresent(existingAward -> {
                                                                                                award.setCeremony(StringUtils.capitalize(existingAward.getCeremony()));
                                                                                                award.setName(StringUtils.capitalize(existingAward.getName()));
                                                                                                award.setYear(existingAward.getYear());
                                                                                            })
                                                                            );

                                                                            // Supprimer les récompenses obsolètes
                                                                            awards.removeIf(existing ->
                                                                                    awardDTOSet.stream().noneMatch(updated ->
                                                                                            Objects.nonNull(updated.getId()) && updated.getId().equals(existing.getId())
                                                                                    )
                                                                            );

                                                                            // Ajouter les nouvelles récompenses
                                                                            awardDTOSet.forEach(updated -> {
                                                                                if (Objects.isNull(updated.getId())) {
                                                                                    Award newAward = Award.fromDTO(updated);
                                                                                    newAward.setMovie(movie);
                                                                                    awards.add(newAward);
                                                                                }
                                                                            });
                                                                        }
                                                                )
                                                                .call(awardRepository::flush)
                                                                .map(awardService::fromAwardSetEntity)
                                        )
                        )
                ;
    }

    /**
     * Met à jour la liste des personnes associées à un film en supprimant les anciennes entrées,
     * en ajoutant de nouvelles personnes et en récupérant celles existantes.
     *
     * @param id           L'identifiant du film.
     * @param personDTOSet L'ensemble des personnes à enregistrer sous forme de {@link PersonDTO}.
     * @param getPeople    Fonction permettant de récupérer la liste des personnes associées au film (ex: Movie::getProducers).
     * @param createPerson Fonction permettant de créer une nouvelle personne à partir du film et d'un DTO.
     * @param service      Service permettant la récupération des personnes existantes par ID.
     * @param <T>          Type de la personne (ex: Producteur, Réalisateur, Cascadeur, etc.).
     * @param <S>          Type du service associé à cette personne.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} mis à jour des personnes associées au film.
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     */
    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> savePeople(
            Long id,
            Set<PersonDTO> personDTOSet,
            Function<Movie, Set<T>> getPeople,
            BiFunction<Movie, PersonDTO, T> createPerson,
            S service
    ) {
        return Panache.withTransaction(() ->
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .chain(movie ->
                                Mutiny.fetch(getPeople.apply(movie))
                                        .chain(existingPeople -> {
                                            Set<T> updatedPeople = new HashSet<>(existingPeople);

                                            // Supprimer les personnes obsolètes
                                            updatedPeople.removeIf(existing ->
                                                    personDTOSet.stream().noneMatch(personDTO ->
                                                            Objects.nonNull(personDTO.getId()) && personDTO.getId().equals(existing.getId())
                                                    )
                                            );

                                            // Ajouter les nouvelles personnes
                                            personDTOSet.stream()
                                                    .filter(personDTO -> Objects.isNull(personDTO.getId()))
                                                    .forEach(personDTO -> {
                                                        T newPerson = createPerson.apply(movie, personDTO);
                                                        updatedPeople.add(newPerson);
                                                    });

                                            // Récupérer et ajouter les personnes existantes
                                            return service.getByIds(
                                                            personDTOSet.stream()
                                                                    .map(PersonDTO::getId)
                                                                    .filter(Objects::nonNull)
                                                                    .filter(idToCheck -> existingPeople.stream()
                                                                            .noneMatch(person -> person.getId().equals(idToCheck)))
                                                                    .toList()
                                                    )
                                                    .invoke(updatedPeople::addAll)
                                                    .map(foundPeople -> {
                                                        updatedPeople.addAll(foundPeople);
                                                        getPeople.apply(movie).clear();
                                                        getPeople.apply(movie).addAll(updatedPeople);
                                                        return updatedPeople;
                                                    });
                                        })
                        )
                        .map(updatedPeople ->
                                updatedPeople.stream()
                                        .map(PersonDTO::fromEntity)
                                        .collect(Collectors.toSet())
                        )
        );
    }

    /**
     * Ajoute des personnes à un film en fonction d'un ensemble de DTO et d'un service associé.
     *
     * @param <T>          Le type de personne (ex. Acteur, Réalisateur) qui doit être ajouté au film.
     * @param <S>          Le type du service utilisé pour récupérer les entités des personnes.
     * @param id           L'identifiant du film auquel les personnes doivent être ajoutées.
     * @param personDTOSet L'ensemble des personnes à ajouter, sous forme de DTO.
     * @param getPeople    Une fonction permettant de récupérer l'ensemble des personnes déjà associées au film.
     * @param service      Le service permettant de récupérer les entités correspondantes aux DTO fournis.
     * @param errorMessage Le message d'erreur à utiliser en cas d'échec de l'opération.
     * @return Une instance de {@link Uni} contenant l'ensemble des personnes ajoutées sous forme de {@link PersonDTO}.
     * En cas d'erreur, une exception est levée avec un message approprié.
     * @throws IllegalArgumentException Si le film n'est pas trouvé ou si certaines personnes sont introuvables.
     * @throws IllegalStateException    Si une erreur se produit lors de la récupération des personnes après la mise à jour.
     */
    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> addPeople(Long
                                                                                                id, Set<PersonDTO> personDTOSet, Function<Movie, Set<T>> getPeople, S service, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                service.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Une ou plusieurs personnes sont introuvables"))
                                                        .call(tSet -> movie.addPeople(getPeople.apply(movie), tSet, errorMessage))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(getPeople.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                                        .map(service::fromPersonSetEntity)
                                        )
                        )
                ;
    }

    /**
     * Ajoute une liste d'acteurs à un film existant.
     *
     * @param movieId          L'identifiant du film auquel les acteurs doivent être ajoutés.
     * @param movieActorDTOSet Un ensemble d'objets {@link MovieActorDTO} représentant les acteurs à ajouter.
     * @return Un {@link Uni} contenant un {@link List} de {@link MovieActorDTO} mis à jour après l'ajout des acteurs.
     * @throws IllegalArgumentException si le film ou certains acteurs ne sont pas trouvés.
     * @throws IllegalStateException    si la liste des acteurs n'est pas initialisée pour ce film.
     */
    public Uni<List<MovieActorDTO>> addMovieActors(Long movieId, Set<MovieActorDTO> movieActorDTOSet) {
        // Création d'une map pour récupérer directement les DTO associés aux IDs des acteurs
        Map<Long, MovieActorDTO> actorDTOMap = movieActorDTOSet.stream()
                .collect(Collectors.toMap(dto -> dto.getActor().getId(), dto -> dto));

        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                actorService.getByIds(
                                                                movieActorDTOSet
                                                                        .stream()
                                                                        .map(movieActorDTO -> movieActorDTO.getActor().getId())
                                                                        .toList()
                                                        )
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs acteurs sont introuvables"))
                                                        .map(actorList ->
                                                                actorList.stream()
                                                                        .map(actor -> {
                                                                                    MovieActorDTO dto = actorDTOMap.get(actor.getId());
                                                                                    return MovieActor.build(movie, actor, dto.getRole().trim(), dto.getRank());
                                                                                }
                                                                        )
                                                                        .toList()
                                                        )
                                                        .call(movie::addMovieActors)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieActorRepository::flush) // Force la génération des IDs
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                                                        .map(actorService::fromMovieActorListEntity)
                                        )
                        )
                ;
    }

    /**
     * Ajoute un ou plusieurs genres à un film existant.
     *
     * @param movieId     L'identifiant du film auquel les genres doivent être ajoutés.
     * @param genreDTOSet Un ensemble d'objets {@link GenreDTO} représentant les genres à ajouter.
     * @return Un {@link Uni} contenant un {@link Set} de {@link GenreDTO} mis à jour après l'ajout des genres.
     * @throws IllegalArgumentException si le film ou certains genres ne sont pas trouvés.
     * @throws IllegalStateException    si l'ensemble des genres n'est pas initialisé pour ce film.
     */
    public Uni<Set<GenreDTO>> addGenres(Long movieId, Set<GenreDTO> genreDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                genreService.getByIds(genreDTOSet.stream().map(GenreDTO::getId).toList())
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs genres sont introuvables"))
                                                        .call(movie::addGenres)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(this::fetchAndMapGenres)
                        )
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                        .call(movie::addCountries)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                ;
    }

    /**
     * Ajoute un ensemble de récompenses à un film spécifique.
     *
     * @param id          L'identifiant du film auquel les récompenses doivent être ajoutées.
     * @param awardDTOSet L'ensemble des récompenses à ajouter sous forme de {@link AwardDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link AwardDTO} :
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     * @throws IllegalStateException    si l'ensemble des récompenses n'est pas initialisé pour ce film.
     */
    public Uni<Set<AwardDTO>> addAwards(Long id, Set<AwardDTO> awardDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                movie.addAwards(
                                                                awardDTOSet
                                                                        .stream()
                                                                        .map(Award::fromDTO)
                                                                        .collect(Collectors.toSet())
                                                        )
                                                        .invoke(awardSet -> awardSet.forEach(award -> award.setMovie(movie)))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(this::fetchAndMapAwards)
                        )
                ;
    }

    /**
     * Retire une personne spécifique d'un film.
     *
     * @param movieId      L'identifiant du film.
     * @param personId     L'identifiant de la personne à retirer.
     * @param peopleGetter Fonction permettant d'obtenir la liste des personnes à modifier depuis l'entité {@link Movie}.
     * @param service      Service permettant de convertir les entités en DTO.
     * @param errorMessage Message d'erreur si la liste des personnes n'est pas initialisée.
     * @param <T>          Type de la personne (ex: Producteur, Réalisateur, etc.).
     * @param <S>          Type du service associé à cette personne.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} :
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws IllegalStateException    si la collection de personnes n'est pas initialisée pour ce film.
     */
    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> removePerson(Long movieId, Long
            personId, Function<Movie, Set<T>> peopleGetter, S service, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.removePerson(peopleGetter.apply(movie), personId, errorMessage))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(peopleGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                                        .map(service::fromPersonSetEntity)
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.removeMovieActor(movieActorId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getMovieActors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                                                        .map(actorService::fromMovieActorListEntity)
                                        )
                        )
                ;
    }

    /**
     * Supprime un genre d'un film existant.
     *
     * @param movieId L'identifiant du film dont le genre doit être supprimé.
     * @param genreId L'identifiant du genre à supprimer du film.
     * @return Un {@link Uni} contenant un objet {@link MovieDTO} mis à jour après la suppression du genre.
     * - Provoque une erreur avec un message explicite si le film ou certains pays ne sont pas trouvés.
     */
    public Uni<Set<GenreDTO>> removeGenre(Long movieId, Long genreId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.removeGenre(genreId))
                                        .chain(movieRepository::persist)
                                        .flatMap(this::fetchAndMapGenres)
                        )
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.removeCountry(countryId))
                                        .chain(movieRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                ;
    }

    /**
     * Supprime une récompense associée à un film.
     * <p>
     * Cette méthode recherche un film par son identifiant et supprime une récompense
     * spécifique de sa liste de récompenses si elle est présente. Si le film n'est pas trouvé,
     * une exception est levée. Après la suppression, les changements sont persistés en base,
     * puis la liste mise à jour des récompenses est récupérée et retournée sous forme de DTOs.
     *
     * @param movieId L'identifiant du film.
     * @param awardId L'identifiant de la récompense à supprimer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des {@link AwardDTO} du film.
     * @throws IllegalArgumentException si le film est introuvable.
     */
    public Uni<Set<AwardDTO>> removeAward(Long movieId, Long awardId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.removeAward(awardId))
                                        .chain(movieRepository::persist)
                                        .flatMap(this::fetchAndMapAwards)
                        )
                ;
    }

    public Uni<Movie> updateMovie(Long id, FileUpload file, MovieDTO movieDTO) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException("Film introuvable"))
                                        .call(
                                                movie ->
                                                        countryService.getByIds(movieDTO.getCountries())
                                                                .invoke(movie::setCountries)
                                                                .chain(() ->
                                                                        genreService.getByIds(movieDTO.getGenres())
                                                                                .invoke(movie::setGenres)
                                                                )
                                        )
                        )
                        .invoke(
                                movie -> {
                                    movie.setTitle(movieDTO.getTitle());
                                    movie.setOriginalTitle(movieDTO.getOriginalTitle());
                                    movie.setSynopsis(movieDTO.getSynopsis());
                                    movie.setReleaseDate(movieDTO.getReleaseDate());
                                    movie.setRunningTime(movieDTO.getRunningTime());
                                    movie.setBudget(movieDTO.getBudget());
                                    movie.setPosterFileName(Optional.ofNullable(movie.getPosterFileName()).orElse(DEFAULT_POSTER));
                                    movie.setBoxOffice(movieDTO.getBoxOffice());
                                }
                        )
                        .call(
                                entity -> {
                                    if (Objects.nonNull(file)) {
                                        return uploadPoster(file)
                                                .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", id, error.getMessage()))
                                                .invoke(entity::setPosterFileName);
                                    }
                                    return Uni.createFrom().item(entity);
                                }
                        )
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
        return Panache.withTransaction(() -> movieRepository.deleteById(id));
    }

    /**
     * Vide un ensemble de personnes (acteurs, réalisateurs, etc.) associé à un film.
     * <p>
     * Cette méthode permet de vider un ensemble spécifique de personnes associées à un film (comme les acteurs, réalisateurs, etc.).
     * Elle récupère cet ensemble en appliquant une fonction (`peopleGetter`) sur le film et, si l'ensemble est initialisé,
     * elle appelle la méthode `clearPersons` sur le film pour le vider. Si l'ensemble est nul ou si le film n'est pas trouvé,
     * une exception est levée. Enfin, le film est persisté après l'opération.
     *
     * @param id           L'identifiant du film dont les personnes associées doivent être supprimées.
     * @param peopleGetter Une fonction permettant d'obtenir l'ensemble des personnes à partir du film (par exemple, acteurs ou réalisateurs).
     * @param errorMessage Le message d'erreur à utiliser si l'ensemble des personnes est nul.
     * @param <T>          Le type des éléments dans l'ensemble des personnes (générique).
     * @return Un {@link Uni} contenant `true` si l'opération a été réalisée avec succès.
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws WebApplicationException  Si une erreur se produit lors de la suppression des personnes.
     */
    public <T> Uni<Boolean> clearPersons(Long id, Function<Movie, Set<T>> peopleGetter, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(movie -> movie.clearPersons(peopleGetter.apply(movie), errorMessage))
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des personnes", throwable);
                        });
    }

    /**
     * Supprime tous les genres associés à un film donné.
     * <p>
     * Cette méthode permet de vider la collection des genres associés à un film en supprimant toutes les entrées
     * de cette collection. Elle effectue cette opération dans une transaction et persiste les changements
     * dans la base de données. Si le film avec l'ID spécifié n'existe pas, une exception est levée.
     *
     * @param id L'identifiant du film pour lequel les genres doivent être supprimés.
     * @return Un {@link Uni} contenant {@code true} si la suppression des genres a réussi,
     * ou une exception sera levée en cas d'erreur.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des genres (par exemple,
     *                                 en cas de film introuvable ou d'erreur de persistance).
     */
    public Uni<Boolean> clearGenres(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(Movie::clearGenres)
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des genres", throwable);
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(Movie::clearCountries)
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    /**
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de vider la collection des récompenses associées à un film en supprimant toutes les entrées
     * de cette collection. Elle effectue cette opération dans une transaction et persiste les changements
     * dans la base de données. Si le film avec l'ID spécifié n'existe pas, une exception est levée.
     *
     * @param id L'identifiant du film pour lequel les récompenses doivent être supprimées.
     * @return Un {@link Uni} contenant {@code true} si la suppression des récompenses a réussi,
     * ou une exception sera levée en cas d'erreur.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des récompenses (par exemple,
     *                                 en cas de film introuvable ou d'erreur de persistance).
     */
    public Uni<Boolean> clearAwards(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .call(Movie::clearAwards)
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    /**
     * Récupère et convertit les genres associés à un film en objets {@link GenreDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les genres d'un film
     * et les transformer en un ensemble de DTOs. Si la liste des genres est `null`,
     * une exception est levée.
     *
     * @param movie Le film dont les genres doivent être récupérés.
     * @return Un {@link Uni} contenant un ensemble de {@link GenreDTO}.
     * @throws IllegalStateException si la liste des genres n'est pas initialisée.
     */
    private Uni<Set<GenreDTO>> fetchAndMapGenres(Movie movie) {
        return
                Mutiny.fetch(movie.getGenres())
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des genres n'est pas initialisée"))
                        .map(genreService::fromGenreSetEntity)
                ;
    }

    /**
     * Récupère et convertit les pays associés à un film en objets {@link CountryDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les pays liés à un film
     * et les transformer en un ensemble de DTOs. Si la liste des pays est `null`,
     * une exception est levée.
     *
     * @param movie Le film dont les pays doivent être récupérés.
     * @return Un {@link Uni} contenant un ensemble de {@link CountryDTO}.
     * @throws IllegalStateException si la liste des pays n'est pas initialisée.
     */
    private Uni<Set<CountryDTO>> fetchAndMapCountries(Movie movie) {
        return
                Mutiny.fetch(movie.getCountries())
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des pays n'est pas initialisée"))
                        .map(countryService::fromCountrySetEntity)
                ;
    }

    /**
     * Récupère et convertit les récompenses associées à un film en objets {@link AwardDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les récompenses liées à un film
     * et les transformer en un ensemble de DTOs. Si la liste des récompenses est `null`,
     * une exception est levée.
     *
     * @param movie Le film dont les récompenses doivent être récupérées.
     * @return Un {@link Uni} contenant un ensemble de {@link AwardDTO}.
     * @throws IllegalStateException si la liste des récompenses n'est pas initialisée.
     */
    private Uni<Set<AwardDTO>> fetchAndMapAwards(Movie movie) {
        return
                Mutiny.fetch(movie.getAwards())
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des récompenses n'est pas initialisée"))
                        .map(awardService::fromAwardSetEntity)
                ;
    }
}
