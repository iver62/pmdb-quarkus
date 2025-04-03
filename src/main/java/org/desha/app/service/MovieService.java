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

    private final Mutiny.SessionFactory msf;
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
            Mutiny.SessionFactory msf,
            CountryService countryService,
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
        this.msf = msf;
        this.countryService = countryService;
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

    public Uni<List<MovieActorDTO>> getActorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getMovieActors())
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

    /*public Uni<List<Producer>> getProducersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie ->
                                Mutiny.fetch(movie.getProducers())
                                        .flatMap(producers ->
                                                Uni.join().all(
                                                        producers.stream()
                                                                .map(producer ->
                                                                        Mutiny.fetch(producer.getCountries())
                                                                                .invoke(producer::setCountries) // Assigner les pays au producteur
                                                                                .replaceWith(producer) // Retourner le producteur mis à jour
                                                                )
                                                                .toList()
                                                ).andCollectFailures() // Gérer les erreurs de récupération
                                        )
                        )
                ;
    }*/

    /*public Uni<Set<Producer>> getProducersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getProducers()))
                ;
    }*/

    public Uni<Set<Director>> getDirectorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getDirectors()))
                ;
    }

    public Uni<Set<Screenwriter>> getScreenwritersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getScreenwriters()))
                ;
    }

    public Uni<Set<Musician>> getMusiciansByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getMusicians()))
                ;
    }

    public Uni<Set<Photographer>> getPhotographersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getPhotographers()))
                ;
    }

    public Uni<Set<Costumier>> getCostumiersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getCostumiers()))
                ;
    }

    public Uni<Set<Decorator>> getDecoratorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getDecorators()))
                ;
    }

    public Uni<Set<Editor>> getEditorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getEditors()))
                ;
    }

    public Uni<Set<Caster>> getCastersByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getCasters()))
                ;
    }

    public Uni<Set<ArtDirector>> getArtDirectorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getArtDirectors()))
                ;
    }

    public Uni<Set<SoundEditor>> getSoundEditorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getSoundEditors()))
                ;
    }

    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisorsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getVisualEffectsSupervisors()))
                ;
    }

    public Uni<Set<MakeupArtist>> getMakeupArtists(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getMakeupArtists()))
                ;
    }

    public Uni<Set<HairDresser>> getHairDressers(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getHairDressers()))
                ;
    }

    public Uni<Set<Stuntman>> getStuntmen(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                        .flatMap(movie -> Mutiny.fetch(movie.getStuntmen()))
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

    public Uni<Set<Genre>> getGenresByMovie(Movie movie) {
        return Mutiny.fetch(movie.getGenres());
    }

    public Uni<Set<Country>> getCountriesByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCountries());
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

    public Uni<Movie> addGenres(Long id, Set<Genre> genreSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        genreSet
                                                                                .stream()
                                                                                .map(genre -> msf.openSession().chain(() -> genre.addMovie(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addGenres(genreSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> addCountries(Long id, Set<Country> countrySet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        countrySet
                                                                                .stream()
                                                                                .map(country -> msf.openSession().chain(() -> country.addMovie(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addCountries(countrySet))
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

    public Uni<Movie> removeProducer(Long movieId, Long producerId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeProducer(producerId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeDirector(Long movieId, Long directorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeDirector(directorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeScreenwriter(Long movieId, Long screenwriterId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeScreenwriter(screenwriterId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeMusician(Long movieId, Long musicianId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeMusician(musicianId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removePhotographer(Long movieId, Long photographerId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removePhotographer(photographerId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeCostumier(Long movieId, Long costumierId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeCostumier(costumierId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeDecorator(Long movieId, Long decoratorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeDecorator(decoratorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeEditor(Long movieId, Long editorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeEditor(editorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeCaster(Long movieId, Long casterId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeCaster(casterId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeArtDirector(Long movieId, Long artDirectorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeArtDirector(artDirectorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeSoundEditor(Long movieId, Long soundEditorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeSoundEditor(soundEditorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeVisualEffectsSupervisor(Long movieId, Long visualEffectsSupervisorId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeVisualEffectsSupervisor(visualEffectsSupervisorId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeMakeupArtist(Long movieId, Long makeupArtistId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeMakeupArtist(makeupArtistId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeHairDresser(Long movieId, Long hairDresserId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeHairDresser(hairDresserId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeStuntman(Long movieId, Long stuntmanId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeStuntman(stuntmanId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeGenre(Long movieId, Long genreId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeGenre(genreId))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeCountry(Long movieId, Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeCountry(countryId))
                                        .chain(entity -> entity.persist())
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
                                        /*.call(movie ->
                                                // Gestion des récompenses
                                                Mutiny.fetch(movie.getAwards())
                                                        .invoke(
                                                                awards -> {
                                                                    Set<AwardDTO> updatedAwards = movieDTO.getAwards();

                                                                    // Modifier les récompenses
                                                                    awards.forEach(award ->
                                                                            updatedAwards.stream()
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
                                                                            updatedAwards.stream().noneMatch(updated ->
                                                                                    Objects.nonNull(updated.getId()) && updated.getId().equals(existing.getId())
                                                                            )
                                                                    );

                                                                    // Ajouter les nouvelles récompenses
                                                                    updatedAwards.forEach(updated -> {
                                                                        if (Objects.isNull(updated.getId())) {
                                                                            Award newAward = Award.fromDTO(updated);
                                                                            newAward.setMovie(movie);
                                                                            awards.add(newAward);
                                                                        }
                                                                    });
                                                                }
                                                        )
                                        )*/
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
