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
            AwardService awardService,
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
        this.awardService = awardService;
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
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMovieActors())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Acteurs non initialisés pour ce film"))
                                        .map(actorService::fromMovieActorSetEntity)
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getGenres())
                                        .onItem().ifNull().failWith(() -> new IllegalStateException("Genres non initialisés pour ce film"))
                                        .map(genreService::fromGenreSetEntity)
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
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
            Long id,
            Set<PersonDTO> personDTOSet,
            Function<Movie, Set<T>> addPeople,
            Function<Movie, Set<T>> getPeople,
            S service
            ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                service.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs producteurs sont introuvables"))
                                                        .call(movie::addProducers)
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(getPeople.apply(movie))
                                                        .map(tSet ->
                                                                tSet
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
                                                        .call(producers -> movie.addPeople(movie.getProducers(), producers, "La liste des producteurs n'est pas initialisée"))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getProducers())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des producteurs n'est pas initialisée"))
                                                        .map(producerService::fromPersonSetEntity)
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
                                                        .call(directors -> movie.addPeople(movie.getDirectors(), directors, "La liste des réalisateurs n'est pas initialisée"))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getDirectors())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des réalisateurs n'est pas initialisée"))
                                                        .map(directorService::fromPersonSetEntity)
                                        )
                        )
                ;
    }

    /**
     * Ajoute une liste de scénaristes à un film donné et retourne la liste mise à jour des scénaristes.
     *
     * @param movieId      L'identifiant du film auquel ajouter les scénaristes.
     * @param personDTOSet L'ensemble des scénaristes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} représentant les scénaristes du film.
     * @throws IllegalArgumentException si le film ou certains scénaristes ne sont pas trouvés.
     * @throws IllegalStateException    si les scénaristes ne sont pas initialisés pour ce film.
     */
    public Uni<Set<PersonDTO>> addScreenwriters(Long movieId, Set<PersonDTO> personDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                screenwriterService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs scénaristes sont introuvables"))
                                                        .call(screenwriters -> movie.addPeople(movie.getScreenwriters(), screenwriters, "La liste des scénaristes n'est pas initialisée"))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getScreenwriters())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des scénaristes n'est pas initialisée"))
                                                        .map(screenwriterService::fromPersonSetEntity)
                                        )
                        )
                ;
    }

    /**
     * Ajoute une liste de musiciens à un film donné et retourne la liste mise à jour des musiciens.
     *
     * @param movieId      L'identifiant du film auquel ajouter les musiciens.
     * @param personDTOSet L'ensemble des musiciens à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} représentant les musiciens du film.
     * @throws IllegalArgumentException si le film ou certains musiciens ne sont pas trouvés.
     * @throws IllegalStateException    si les musiciens ne sont pas initialisés pour ce film.
     */
    public Uni<Set<PersonDTO>> addMusicians(Long movieId, Set<PersonDTO> personDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                musicianService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs musiciens sont introuvables"))
                                                        .call(musicians -> movie.addPeople(movie.getMusicians(), musicians, "La liste des musiciens n'est pas initialisée"))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getMusicians())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des musiciens n'est pas initialisée"))
                                                        .map(musicianService::fromPersonSetEntity)
                                        )
                        )
                ;
    }

    /**
     * Ajoute une liste de photographes à un film donné et retourne la liste mise à jour des photographes.
     *
     * @param movieId      L'identifiant du film auquel ajouter les photographes.
     * @param personDTOSet L'ensemble des photographes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} représentant les photographes du film.
     * @throws IllegalArgumentException si le film ou certains photographes ne sont pas trouvés.
     * @throws IllegalStateException    si les photographes ne sont pas initialisés pour ce film.
     */
    public Uni<Set<PersonDTO>> addPhotographers(Long movieId, Set<PersonDTO> personDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .flatMap(movie ->
                                                photographerService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs photographes sont introuvables"))
                                                        .call(photographers -> movie.addPeople(movie.getPhotographers(), photographers, "La liste des photographes n'est pas initialisée"))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getPhotographers())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des photographes n'est pas initialisée"))
                                                        .map(photographerService::fromPersonSetEntity)
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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des genres n'est pas initialisée"))
                                                        .map(genreService::fromGenreSetEntity)
                                        )
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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des genres n'est pas initialisée"))
                                                        .map(countryService::fromCountrySetEntity)
                                        )
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des récompenses n'est pas initialisée"))
                                                        .map(awardService::fromAwardSetEntity)
                                        )
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
    public <T extends Person, S extends PersonService<T>> Uni<Set<PersonDTO>> removePerson(Long movieId, Long personId, Function<Movie, Set<T>> peopleGetter, S service, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeGenre(genreId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getGenres())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des genres n'est pas initialisée"))
                                                        .map(genreService::fromGenreSetEntity)
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
    public Uni<Set<CountryDTO>> removeCountry(Long movieId, Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeCountry(countryId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des pays n'est pas initialisée"))
                                                        .map(countryService::fromCountrySetEntity)
                                        )
                        )
                ;
    }

    public Uni<Set<AwardDTO>> removeAward(Long movieId, Long awardId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(movie -> movie.removeAward(awardId))
                                        .chain(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(movie.getAwards())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des récompenses n'est pas initialisée"))
                                                        .map(awardService::fromAwardSetEntity)
                                        )
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
