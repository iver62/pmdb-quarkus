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
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.MovieActor;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieActorRepository;
import org.desha.app.repository.MovieRepository;
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

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
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
            CountryService countryService,
            CountryRepository countryRepository,
            FileService fileService,
            GenreService genreService,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
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
        this.countryService = countryService;
        this.countryRepository = countryRepository;
        this.fileService = fileService;
        this.genreService = genreService;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
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
        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
        this.stuntmanService = stuntmanService;
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
                        .onFailure().recoverWithNull()
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

    public Uni<List<Movie>> getByTitle(String pattern) {
        return movieRepository.findByTitle(pattern);
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
        log.info("GET ACTORS BY MOVIE");
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMovieActors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Acteurs non initialisés pour ce film"))
                                        .map(
                                                movieActors ->
                                                        movieActors
                                                                .stream()
                                                                .map(MovieActorDTO::fromEntity)
                                                                .sorted(Comparator.comparing(MovieActorDTO::getRank))
                                                                .toList()
                                        )
                                        .onItem().ifNull().continueWith(Collections.emptyList())
                        )
                ;
    }

    /**
     * Récupère la liste des producteurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les producteurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les producteurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les producteurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getProducersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getProducers())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Producteurs non initialisés pour ce film"))
                                        .map(producerService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des réalisateurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les réalisateurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les réalisateurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les réalisateurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getDirectorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getDirectors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Réalisateurs non initialisés pour ce film"))
                                        .map(directorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des scénaristes associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les scénaristes.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les scénaristes du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les scénaristes ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getScreenwritersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getScreenwriters())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Scénaristes non initialisés pour ce film"))
                                        .map(screenwriterService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des musiciens associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les musiciens.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les musiciens du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les musiciens ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getMusiciansByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMusicians())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Musiciens non initialisés pour ce film"))
                                        .map(musicianService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des photographes associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les photographes.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les photographes du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les photographes ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getPhotographersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getPhotographers())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Photographes non initialisés pour ce film"))
                                        .map(photographerService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des costumiers associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les costumiers.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les costumiers du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les costumiers ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getCostumiersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getCostumiers())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Costumiers non initialisés pour ce film"))
                                        .map(costumierService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des décorateurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les décorateurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les décorateurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les décorateurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getDecoratorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getDecorators())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Décorateurs non initialisés pour ce film"))
                                        .map(decoratorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des monteurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les monteurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les monteurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les monteurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getEditorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getEditors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Monteurs non initialisés pour ce film"))
                                        .map(editorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des casteurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les casteurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les casteurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les casteurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getCastersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getCasters())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Casteurs non initialisés pour ce film"))
                                        .map(casterService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des directeurs artistiques associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les directeurs artistiques.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les directeurs artistiques du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les directeurs artistiques ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getArtDirectorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getArtDirectors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Directeurs artistiques non initialisés pour ce film"))
                                        .map(artDirectorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des ingénieurs du son associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les ingénieurs du son.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les ingénieurs du son du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les ingénieurs du son ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getSoundEditorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getSoundEditors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Ingénieurs du son non initialisés pour ce film"))
                                        .map(soundEditorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des spécialistes effets spéciaux associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les spécialistes effets spéciaux.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les spécialistes effets spéciaux du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les spécialistes effets spéciaux ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getVisualEffectsSupervisorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getVisualEffectsSupervisors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Spécialistes effets spéciaux non initialisés pour ce film"))
                                        .map(visualEffectsSupervisorService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des maquilleurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les maquilleurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les maquilleurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les maquilleurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getMakeupArtistsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMakeupArtists())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Maquilleurs non initialisés pour ce film"))
                                        .map(makeupArtistService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des coiffeurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les coiffeurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les coiffeurs du film..
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les coiffeurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getHairDressersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getHairDressers())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Coiffeurs non initialisés pour ce film"))
                                        .map(hairDresserService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des cascadeurs associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les cascadeurs.
     * @return Un {@link Uni} contenant une liste de {@link PersonDTO} représentant les cascadeurs du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les cascadeurs ne sont pas initialisés pour ce film.
     */
    public Uni<List<PersonDTO>> getStuntmenByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getStuntmen())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Cascadeurs non initialisés pour ce film"))
                                        .map(stuntmanService::fromPersonListEntity)
                        )
                ;
    }

    /**
     * Récupère la liste des genres associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les genres.
     * @return Un {@link Uni} contenant une liste de {@link GenreDTO} représentant les genres du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les genres ne sont pas initialisés pour ce film.
     */
    public Uni<List<GenreDTO>> getGenresByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getGenres())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Genres non initialisés pour ce film"))
                                        .map(genreSet ->
                                                genreSet
                                                        .stream()
                                                        .map(GenreDTO::fromEntity)
                                                        .toList()
                                        )
                        )
                ;
    }

    /**
     * Récupère la liste des pays associés à un film donné.
     *
     * @param id L'ID du film pour lequel récupérer les pays.
     * @return Un {@link Uni} contenant une liste de {@link CountryDTO} représentant les pays du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si les pays ne sont pas initialisés pour ce film.
     */
    public Uni<List<CountryDTO>> getCountriesByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getCountries())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés pour ce film"))
                                        .map(countrySet ->
                                                countrySet
                                                        .stream()
                                                        .map(CountryDTO::fromEntity)
                                                        .toList()
                                        )
                        )
                ;
    }

    public Uni<TechnicalTeamDTO> getTechnicalTeam(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findByIdWithTechnicalTeam(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
        Map<Long, String> roleMap = movieActorsList
                .stream()
                .collect(Collectors.toMap(movieActorDTO -> movieActorDTO.getActor().getId(), MovieActorDTO::getRole));

        Map<Long, Integer> rankMap = movieActorsList
                .stream()
                .collect(Collectors.toMap(movieActorDTO -> movieActorDTO.getActor().getId(), MovieActorDTO::getRank));

        return Panache.withTransaction(() ->
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMovieActors())
                                        .flatMap(existingMovieActors -> {
                                            // Créer une liste modifiable
                                            List<MovieActor> modifiableMovieActors = new ArrayList<>(existingMovieActors);

                                            // Récupérer les ID des acteurs existants
                                            Map<Long, MovieActor> existingActorMap = modifiableMovieActors.stream()
                                                    .collect(Collectors.toMap(ma -> ma.getActor().getId(), ma -> ma));

                                            // Identifier les acteurs à ajouter ou mettre à jour
                                            List<Long> newActorIds = new ArrayList<>();
                                            for (MovieActorDTO dto : movieActorsList) {
                                                if (Objects.isNull(dto.getActor()) || Objects.isNull(dto.getActor().getId())) {
                                                    continue; // Éviter les erreurs avec des données corrompues
                                                }
                                                if (existingActorMap.containsKey(dto.getActor().getId())) {
                                                    // Modifier le rôle de l'acteur existant si nécessaire
                                                    MovieActor existingMovieActor = existingActorMap.get(dto.getActor().getId());
                                                    if (!Objects.equals(existingMovieActor.getRole(), dto.getRole())) {
                                                        existingMovieActor.setRole(dto.getRole());
                                                    }
                                                    if (!Objects.equals(existingMovieActor.getRank(), dto.getRank())) {
                                                        existingMovieActor.setRank(dto.getRank());
                                                    }
                                                } else {
                                                    // Nouvel acteur à ajouter
                                                    newActorIds.add(dto.getActor().getId());
                                                }
                                            }

                                            // Identifier les acteurs à supprimer
                                            List<MovieActor> actorsToRemove = modifiableMovieActors.stream()
                                                    .filter(ma ->
                                                            movieActorsList
                                                                    .stream()
                                                                    .noneMatch(dto -> dto.getActor().getId().equals(ma.getActor().getId()))
                                                    )
                                                    .toList();

                                            // Supprimer les acteurs retirés
                                            modifiableMovieActors.removeAll(actorsToRemove);

                                            // Charger ces nouveaux acteurs et les ajouter au casting
                                            return
                                                    movieActorRepository.deleteByIds(actorsToRemove.stream().map(MovieActor::getId).toList())
                                                            .chain(() ->
                                                                    actorService.getByIds(newActorIds)
                                                                            .map(newActors -> {
                                                                                        modifiableMovieActors.addAll(
                                                                                                newActors
                                                                                                        .stream()
                                                                                                        .map(actor -> MovieActor.build(
                                                                                                                movie,
                                                                                                                actor,
                                                                                                                roleMap.getOrDefault(actor.getId(), "Inconnu"),
                                                                                                                rankMap.getOrDefault(actor.getId(), 0)
                                                                                                        )).toList()
                                                                                        );
                                                                                        return modifiableMovieActors;
                                                                                    }
                                                                            )
                                                                            .invoke(movieActors -> {
                                                                                movie.setMovieActors(movieActors);
                                                                                movie.setLastUpdate(LocalDateTime.now());
                                                                            })
                                                            )
                                                            .map(movieActors ->
                                                                    movieActors
                                                                            .stream()
                                                                            .sorted(Comparator.comparing(MovieActor::getRank))
                                                                            .map(MovieActorDTO::fromEntity)
                                                                            .toList()
                                                            )
                                                    ;
                                        })
                        )
        );
    }

    public Uni<Set<Award>> saveAwards(Long id, Set<AwardDTO> awardDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
                                                                                                award.setCeremony(existingAward.getCeremony());
                                                                                                award.setName(existingAward.getName());
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
                                        )
                        )
                ;
    }

    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> savePeople(
            Long id,
            Set<PersonDTO> personDTOSet,
            Function<Movie, Set<T>> getPeople,
            BiFunction<Movie, PersonDTO, T> createPerson,
            S service
    ) {
        return Panache.withTransaction(() ->
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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

    /*public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> addPeople(
            Long movieId,
            Set<PersonDTO> personDTOSet,
            Function<Movie, Set<T>> getPeople,
            S service
            ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                service.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs producteurs sont introuvables"))
                                                        .call(movie::addProducers)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(getPeople)
                                                        .map(producers ->
                                                                producers
                                                                        .stream()
                                                                        .map(PersonDTO::fromEntity)
                                                                        .collect(Collectors.toSet())
                                                        )
                                        )
                        )
                ;
    }*/

    /**
     * Ajoute une liste de producteurs à un film donné et retourne la liste mise à jour des producteurs.
     *
     * @param movieId      L'identifiant du film auquel ajouter les producteurs.
     * @param personDTOSet L'ensemble des producteurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} représentant les producteurs du film.
     * @throws IllegalArgumentException si le film ou certains producteurs ne sont pas trouvés.
     * @throws IllegalStateException    Si les producteurs ne sont pas initialisés pour ce film.
     */
    public Uni<Set<PersonDTO>> addProducers(Long movieId, Set<PersonDTO> personDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                producerService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs producteurs sont introuvables"))
                                                        .call(movie::addProducers)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getProducers())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des producteurs n'est pas initialisée"))
                                                        .map(producers ->
                                                                producers
                                                                        .stream()
                                                                        .map(PersonDTO::fromEntity)
                                                                        .collect(Collectors.toSet())
                                                        )
                                        )
                        )
                ;
    }

    /**
     * Ajoute une liste de réalisateurs à un film donné et retourne la liste mise à jour des réalisateurs.
     *
     * @param movieId      L'identifiant du film auquel ajouter les réalisateurs.
     * @param personDTOSet L'ensemble des réalisateurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} représentant les réalisateurs du film.
     * @throws IllegalArgumentException si le film ou certains réalisateurs ne sont pas trouvés.
     * @throws IllegalStateException    si les réalisateurs ne sont pas initialisés pour ce film.
     */
    public Uni<Set<PersonDTO>> addDirectors(Long movieId, Set<PersonDTO> personDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                directorService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs réalisateurs sont introuvables"))
                                                        .call(movie::addDirectors)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getDirectors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des réalisateurs n'est pas initialisée"))
                                                        .map(directors ->
                                                                directors
                                                                        .stream()
                                                                        .map(PersonDTO::fromEntity)
                                                                        .collect(Collectors.toSet())
                                                        )
                                        )
                        )
                ;
    }

    public Uni<Movie> addRole(Long id, MovieActor movieActor) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.addRole(MovieActor.build(entity, movieActor.getActor(), movieActor.getRole(), movieActor.getRank())))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> addAwards(Long id, Set<Award> awardSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.addAwards(awardSet))
                                        .invoke(entity -> awardSet.forEach(award -> award.setMovie(entity)))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    /**
     * Supprime un producteur d'un film spécifique.
     *
     * @param movieId    L'identifiant du film dont le producteur doit être supprimé.
     * @param producerId L'identifiant du producteur à supprimer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des producteurs du film sous forme de {@link PersonDTO}.
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     */
    public Uni<Set<PersonDTO>> removeProducer(Long movieId, Long producerId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeProducer(producerId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getProducers())
                                                        .map(producers ->
                                                                producers
                                                                        .stream()
                                                                        .map(PersonDTO::fromEntity)
                                                                        .collect(Collectors.toSet())
                                                        )
                                        )
                        )
                ;
    }

    public Uni<Movie> removeDirector(Long movieId, Long directorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeDirector(directorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeScreenwriter(Long movieId, Long screenwriterId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeScreenwriter(screenwriterId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeMusician(Long movieId, Long musicianId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeMusician(musicianId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removePhotographer(Long movieId, Long photographerId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removePhotographer(photographerId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeCostumier(Long movieId, Long costumierId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeCostumier(costumierId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeDecorator(Long movieId, Long decoratorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeDecorator(decoratorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeEditor(Long movieId, Long editorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeEditor(editorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeCaster(Long movieId, Long casterId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeCaster(casterId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeArtDirector(Long movieId, Long artDirectorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeArtDirector(artDirectorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeSoundEditor(Long movieId, Long soundEditorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeSoundEditor(soundEditorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeVisualEffectsSupervisor(Long movieId, Long visualEffectsSupervisorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeVisualEffectsSupervisor(visualEffectsSupervisorId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeMakeupArtist(Long movieId, Long makeupArtistId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeMakeupArtist(makeupArtistId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeHairDresser(Long movieId, Long hairDresserId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeHairDresser(hairDresserId))
                                        .chain(movie -> movie.persist())
                        )
                ;
    }

    public Uni<Movie> removeStuntman(Long movieId, Long stuntmanId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(entity -> entity.removeStuntman(stuntmanId))
                                        .chain(movieRepository::persist)
                        )
                ;
    }

    /**
     * Ajoute un ou plusieurs genres à un film existant.
     *
     * @param movieId     L'identifiant du film auquel les genres doivent être ajoutés.
     * @param genreDTOSet Un ensemble d'objets {@link GenreDTO} représentant les genres à ajouter.
     * @return Un {@link Uni} contenant un objet {@link MovieDTO} mis à jour après l'ajout des genres.
     * - Provoque une erreur avec un message explicite si le film ou certains pays ne sont pas trouvés.
     */
    public Uni<MovieDTO> addGenres(Long movieId, Set<GenreDTO> genreDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                genreService.getByIds(genreDTOSet.stream().map(GenreDTO::getId).toList())
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs genres sont introuvables"))
                                                        .call(movie::addGenres)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getGenres())
                                                        .map(genreSet -> MovieDTO.fromEntity(movie, genreSet, null, null))
                                        )
                        )
                ;
    }

    /**
     * Ajoute une ou plusieurs associations entre un film et des pays.
     *
     * @param movieId       L'identifiant du film auquel associer les pays.
     * @param countryDTOSet Un ensemble de DTO représentant les pays à associer.
     * @return Un {@link Uni} contenant un {@link MovieDTO} mis à jour si l'opération réussit.
     * - Provoque une erreur avec un message explicite si le film ou certains pays ne sont pas trouvés.
     */
    public Uni<MovieDTO> addCountries(Long movieId, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                        .call(movie::addCountries)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .map(countrySet -> MovieDTO.fromEntity(movie, null, countrySet, null))
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
    public Uni<MovieDTO> removeGenre(Long movieId, Long genreId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeGenre(genreId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getGenres())
                                                        .map(genreSet -> MovieDTO.fromEntity(movie, genreSet, null, null))
                                        )
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
    public Uni<MovieDTO> removeCountry(Long movieId, Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeCountry(countryId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .map(countrySet -> MovieDTO.fromEntity(movie, null, countrySet, null))
                                        )
                        )
                ;
    }

    public Uni<Movie> removeAward(Long movieId, Long awardId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeAward(awardId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> updateMovie(Long id, FileUpload file, MovieDTO movieDTO) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException("Film non trouvé"))
                                        .onItem().ifNotNull()
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

    public Uni<Boolean> deleteMovie(Long id) {
        return Panache.withTransaction(() -> movieRepository.deleteById(id));
    }
}
