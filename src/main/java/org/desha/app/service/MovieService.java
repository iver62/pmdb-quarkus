package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.dto.TechnicalTeamDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.repository.MovieActorRepository;
import org.desha.app.repository.MovieRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    //    private final PersonService<ArtDirector> artDirectorService;
    private final CasterService casterService;
    private final CostumierService costumierService;
    private final DecoratorService decoratorService;
    private final DirectorService directorService;
    private final EditorService editorService;
    //    private final PersonService<HairDresser> hairDresserService;
//    private final PersonService<MakeupArtist> makeupArtistService;
    private final MusicianService musicianService;
    private final PhotographerService photographerService;
    private final ProducerService producerService;
    private final ScreenwriterService screenwriterService;
//    private final PersonService<SoundEditor> soundEditorService;
//    private final PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService;
//    private final PersonService<Stuntman> stuntmanService;

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
//            @PersonType(Role.ART_DIRECTOR) PersonService<ArtDirector> artDirectorService,
            CasterService casterService,
            CostumierService costumierService,
            DecoratorService decoratorService,
            DirectorService directorService,
            EditorService editorService,
//            @PersonType(Role.HAIR_DRESSER) PersonService<HairDresser> hairDresserService,
//            @PersonType(Role.MAKEUP_ARTIST) PersonService<MakeupArtist> makeupArtistService,
            MusicianService musicianService,
            PhotographerService photographerService,
            ProducerService producerService,
            ScreenwriterService screenwriterService
//            @PersonType(Role.SOUND_EDITOR) PersonService<SoundEditor> soundEditorService,
//            @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService,
//            @PersonType(Role.STUNT_MAN) PersonService<Stuntman> stuntmanService
    ) {
        this.msf = msf;
        this.countryService = countryService;
        this.fileService = fileService;
        this.genreService = genreService;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.actorService = actorService;
//        this.artDirectorService = artDirectorService;
        this.casterService = casterService;
        this.costumierService = costumierService;
        this.decoratorService = decoratorService;
        this.directorService = directorService;
        this.editorService = editorService;
//        this.hairDresserService = hairDresserService;
//        this.makeupArtistService = makeupArtistService;
        this.musicianService = musicianService;
        this.photographerService = photographerService;
        this.producerService = producerService;
        this.screenwriterService = screenwriterService;
//        this.soundEditorService = soundEditorService;
//        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
//        this.stuntmanService = stuntmanService;
    }

    public Uni<Long> count(
            String term,
            List<Integer> countryIds,
            List<Integer> genreIds,
            LocalDate fromReleaseDate,
            LocalDate toReleaseDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        return movieRepository.countMovies(term, countryIds, genreIds, fromReleaseDate, toReleaseDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate);
    }

    public Uni<Movie> getById(Long id) {
        return
                movieRepository.findByIdWithCountriesAndGenres(id)
                        .onFailure().recoverWithNull()
                ;
    }

    public Uni<List<MovieDTO>> getMovies(
            int pageIndex,
            int size,
            String sort,
            Sort.Direction direction,
            String term,
            List<Integer> countryIds,
            List<Integer> genreIds,
            LocalDate fromReleaseDate,
            LocalDate toReleaseDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        return
                movieRepository
                        .findMovies(pageIndex, size, sort, direction, term, countryIds, genreIds, fromReleaseDate, toReleaseDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate)
                        .map(
                                movieList ->
                                        movieList
                                                .stream()
                                                .map(MovieDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<Movie>> getByTitle(String pattern) {
        return movieRepository.findByTitle(pattern);
    }

    public Uni<List<MovieActorDTO>> getActorsByMovie(Movie movie) {
        return
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
                ;
    }

    public Uni<TechnicalTeamDTO> getTechnicalTeam(Long id) {
        return
                movieRepository.findByIdWithTechnicalTeam(id)
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
                ;
    }

    public Uni<Set<Producer>> getProducersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getProducers())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Director>> getDirectorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDirectors())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Screenwriter>> getScreenwritersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getScreenwriters())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Musician>> getMusiciansByMovie(Movie movie) {
        return Mutiny.fetch(movie.getMusicians())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Photographer>> getPhotographersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getPhotographers())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Costumier>> getCostumiersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCostumiers())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Decorator>> getDecoratorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDecorators())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Editor>> getEditorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getEditors())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Caster>> getCastersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCasters())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<ArtDirector>> getArtDirectorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getArtDirectors())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<SoundEditor>> getSoundEditorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getSoundEditors())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getVisualEffectsSupervisors())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<MakeupArtist>> getMakeupArtists(Movie movie) {
        return Mutiny.fetch(movie.getMakeupArtists())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<HairDresser>> getHairDressers(Movie movie) {
        return Mutiny.fetch(movie.getHairDressers())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Stuntman>> getStuntmen(Movie movie) {
        return Mutiny.fetch(movie.getStuntmen())
                .onItem().ifNull().continueWith(Collections.emptySet());
    }

    public Uni<Set<Genre>> getGenresByMovie(Movie movie) {
        return Mutiny.fetch(movie.getGenres());
    }

    public Uni<Set<Country>> getCountriesByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCountries());
    }

    public Uni<Set<Award>> getAwardsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getAwards());
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
        return
                Uni.createFrom()
                        .item(Movie.fromDTO(movieDTO))
                        .call(
                                movie ->
                                        countryService.getByIds(movieDTO.getCountries())
                                                .invoke(movie::setCountries)
                                                .chain(() ->
                                                        genreService.getByIds(movieDTO.getGenres())
                                                                .invoke(movie::setGenres)
                                                )
                        )
                        .call(movie -> {
                                    if (Objects.nonNull(file)) {
                                        return uploadPoster(file)
                                                .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", movie.getTitle(), error.getMessage()))
                                                .invoke(movie::setPosterFileName);
                                    }
                                    movie.setPosterFileName(DEFAULT_POSTER);
                                    return Uni.createFrom().item(movie);
                                }
                        )
                        .chain(movie -> Panache.withTransaction(movie::persist))
                ;
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
//                                                                .chain(() -> artDirectorService.getByIds(technicalTeam.getArtDirectors()).invoke(movie::setArtDirectors))
//                                                                .chain(() -> soundEditorService.getByIds(technicalTeam.getSoundEditors()).invoke(movie::setSoundEditors))
//                                                                .chain(() -> visualEffectsSupervisorService.getByIds(technicalTeam.getVisualEffectsSupervisors()).invoke(movie::setVisualEffectsSupervisors))
//                                                                .chain(() -> makeupArtistService.getByIds(technicalTeam.getMakeupArtists()).invoke(movie::setMakeupArtists))
//                                                                .chain(() -> hairDresserService.getByIds(technicalTeam.getHairDressers()).invoke(movie::setHairDressers))
//                                                                .chain(() -> stuntmanService.getByIds(technicalTeam.getStuntmen()).invoke(movie::setStuntmen))
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
                                                    .collect(Collectors.toMap(ma -> ma.getActor().id, ma -> ma));

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
                                                                    .noneMatch(dto -> dto.getActor().getId().equals(ma.getActor().id))
                                                    )
                                                    .toList();

                                            // Supprimer les acteurs retirés
                                            modifiableMovieActors.removeAll(actorsToRemove);

                                            // Charger ces nouveaux acteurs et les ajouter au casting
                                            return
                                                    movieActorRepository.deleteByIds(actorsToRemove.stream().map(movieActor -> movieActor.id).toList())
                                                            .chain(() ->
                                                                    actorService.getByIds(newActorIds)
                                                                            .map(newActors -> {
                                                                                        modifiableMovieActors.addAll(
                                                                                                newActors
                                                                                                        .stream()
                                                                                                        .map(actor -> MovieActor.build(
                                                                                                                movie,
                                                                                                                actor,
                                                                                                                roleMap.getOrDefault(actor.id, "Inconnu"),
                                                                                                                rankMap.getOrDefault(actor.id, 0)
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
                                                            );
                                        })
                        )
        );
    }

    /**
     * Ajoute une liste de {@link Person} à la liste des producteurs d'un {@link Movie} et, pour chaque {@link Person},
     * ajoute le {@link Movie} dans la liste des {@link Movie} en tant que producteur.
     *
     * @param id        l'identifiant du {@link Movie}
     * @param personSet la liste des {@link Person}
     * @return le {@link Movie}
     */
 /*   public Uni<Movie> addProducers(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                        movieRepository.findById(id)
                                                .onItem().ifNotNull()
                                                .call(
                                                        entity ->
                                                                Uni.join().all(
                                                                                personSet
                                                                                        .stream()
                                                                                        .map(person -> msf.openSession().chain(() -> person.addMovieAsProducer(entity)))
                                                                                        .toList()
                                                                        )
                                                                        .usingConcurrencyOf(1)
                                                                        .andFailFast()
                                                )
                                                .call(entity -> entity.addProducers(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addDirectors(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsDirector(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addDirectors(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addScreenwriters(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsScreenwriter(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addScreenwriters(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addMusicians(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsMusician(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addMusicians(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addPhotographers(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsPhotographer(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addPhotographers(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addCostumiers(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsCostumier(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addCostumiers(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> addDecorators(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsDecorator(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addDecorators(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

   /* public Uni<Movie> addEditors(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.addMovieAsEditor(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.addEditors(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/

    /*public Uni<Movie> saveCasting(Long id, Set<Person> personSet) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        Uni.join().all(
                                                                        personSet
                                                                                .stream()
                                                                                .map(person -> msf.openSession().chain(() -> person.saveMovieAsCaster(movie)))
                                                                                .toList()
                                                                )
                                                                .usingConcurrencyOf(1)
                                                                .andFailFast()
                                        )
                                        .call(entity -> entity.saveCasting(personSet))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }*/
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
                                        .onItem().ifNotNull()
                                        .call(
                                                movie ->
                                                        countryService.getByIds(movieDTO.getCountries())
                                                                .invoke(movie::setCountries)
                                                                .chain(() ->
                                                                        genreService.getByIds(movieDTO.getGenres())
                                                                                .invoke(movie::setGenres))
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
                        )
                ;
    }

    public Uni<Boolean> deleteMovie(Long id) {
        return Panache.withTransaction(() -> movieRepository.deleteById(id));
    }
}
