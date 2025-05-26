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
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.record.Repartition;
import org.desha.app.repository.*;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieService {

    private static final String POSTERS_DIR = "posters/";
    private static final String DEFAULT_POSTER = "default-poster.jpg";

    private final AwardService awardService;
    private final CountryService countryService;
    private final FileService fileService;
    private final GenreService genreService;
    private final PersonService personService;
    private final StatsService statsService;

    private final AwardRepository awardRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    @Inject
    public MovieService(
            AwardRepository awardRepository,
            AwardService awardService,
            CountryService countryService,
            CountryRepository countryRepository,
            FileService fileService,
            GenreService genreService,
            PersonService personService,
            StatsService statsService,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            UserRepository userRepository
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
                movieRepository.findByIdWithCountriesAndGenres(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .map(movie -> MovieDTO.fromEntity(movie, movie.getGenres(), movie.getCountries()))
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
                                                .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards().size()))
                                                .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMovies(sort, direction, criteriasDTO)
                        .map(
                                movieList ->
                                        movieList
                                                .stream()
                                                .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards().size()))
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
                                        .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
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
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getPersonsByMovie(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(movie ->
                                personRepository.findPersonsByMovie(id, page, sort, direction, criteriasDTO)
                                        .map(personService::fromPersonListEntity)
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
                                        .map(personService::fromMovieActorListEntity)
                        )
                ;
    }

    /**
     * Récupère un ensemble de personnes associées à un film spécifique.
     *
     * @param id           L'identifiant du film.
     * @param peopleGetter Fonction permettant de récupérer le bon ensemble de personnes depuis l'entité {@link Movie}.
     * @param errorMessage Message d'erreur en cas de liste non initialisée.
     * @return Un {@link Uni} contenant un {@link Set} de {@link PersonDTO} correspondant aux personnes du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si l'ensemble des personnes n'est pas initialisé pour ce film.
     */
    public Uni<Set<PersonDTO>> getPeopleByMovie(Long id, Function<Movie, Set<Person>> peopleGetter, String errorMessage) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .flatMap(movie ->
                                Mutiny.fetch(peopleGetter.apply(movie))
                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                        .map(personService::fromPersonSetEntity)
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
                                        .map(CountryDTO::fromCountryEntitySet)
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
                                                        personService.fromPersonSetEntity(movie.getProducers()),
                                                        personService.fromPersonSetEntity(movie.getDirectors()),
                                                        personService.fromPersonSetEntity(movie.getScreenwriters()),
                                                        personService.fromPersonSetEntity(movie.getDialogueWriters()),
                                                        personService.fromPersonSetEntity(movie.getMusicians()),
                                                        personService.fromPersonSetEntity(movie.getPhotographers()),
                                                        personService.fromPersonSetEntity(movie.getCostumiers()),
                                                        personService.fromPersonSetEntity(movie.getDecorators()),
                                                        personService.fromPersonSetEntity(movie.getEditors()),
                                                        personService.fromPersonSetEntity(movie.getCasters()),
                                                        personService.fromPersonSetEntity(movie.getArtDirectors()),
                                                        personService.fromPersonSetEntity(movie.getSoundEditors()),
                                                        personService.fromPersonSetEntity(movie.getVisualEffectsSupervisors()),
                                                        personService.fromPersonSetEntity(movie.getMakeupArtists()),
                                                        personService.fromPersonSetEntity(movie.getHairDressers()),
                                                        personService.fromPersonSetEntity(movie.getStuntmen())
                                                )
                                        )
                        )
                ;
    }

    public Uni<Set<AwardDTO>> getAwardsByMovie(Long id) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas")) // 404 si le film n'existe pas
                        .flatMap(movie -> Mutiny.fetch(movie.getAwards()))
                        .map(AwardDTO::fromEntitySet);
    }

    public Uni<List<Repartition>> getMoviesCreationDateEvolution() {
        return movieRepository.findMoviesCreationDateEvolution()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de l'évolution des films", failure)
                );
    }

    public Uni<List<Repartition>> getMoviesCreationDateRepartition() {
        return movieRepository.findMoviesByCreationDateRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par date de création", failure)
                );
    }

    public Uni<List<Repartition>> getMoviesReleaseDateRepartition() {
        return movieRepository.findMoviesByReleaseDateRepartition()
                .onFailure().invoke(failure ->
                        log.error("Erreur lors de la récupération de la répartition des films par date de sortie", failure)
                );
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
                movieRepository.movieExists(movieDTO.getTitle(), movieDTO.getOriginalTitle())
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return Uni.createFrom().failure(new WebApplicationException("Le film existe déjà.", 409));
                            }

                            Movie movie = Movie.fromDTO(movieDTO);

                            return Panache.withTransaction(() ->
                                    // Récupérer les pays et les genres en parallèle
                                    countryService.getByIds(movieDTO.getCountries())
                                            .invoke(movie::setCountries)
                                            .chain(() -> genreService.getByIds(movieDTO.getGenres()).invoke(movie::setGenres))
                                            .chain(() ->
                                                    userRepository.findById(movieDTO.getUser().getId())
                                                            .invoke(user -> log.info("Movie created by {}", user.getUsername()))
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
                                            .flatMap(entity ->
                                                    statsService.incrementNumberOfMovies()
                                                            .chain(() -> {
                                                                if (Objects.nonNull(movie.getCountries()) && !movie.getCountries().isEmpty()) {
                                                                    return statsService.updateMoviesByCountryRepartition();
                                                                }
                                                                return Uni.createFrom().voidItem();
                                                            })
                                                            .chain(() -> {
                                                                if (Objects.nonNull(movie.getGenres()) && !movie.getGenres().isEmpty()) {
                                                                    return statsService.updateMoviesByGenreRepartition();
                                                                }
                                                                return Uni.createFrom().voidItem();
                                                            })
                                                            .replaceWith(entity)
                                            )
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
                                                        personService.getByIds(technicalTeam.getProducers())
                                                                .invoke(personSet -> {
                                                                    personSet.forEach(person -> person.getTypes().add(PersonType.PRODUCER));
                                                                    movie.setProducers(personSet);
                                                                })
                                                                .chain(() -> personService.getByIds(technicalTeam.getDirectors())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.DIRECTOR));
                                                                            movie.setDirectors(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getScreenwriters())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.SCREENWRITER));
                                                                            movie.setScreenwriters(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getDialogueWriters())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.DIALOGUE_WRITER));
                                                                            movie.setDialogueWriters(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getMusicians())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.MUSICIAN));
                                                                            movie.setMusicians(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getPhotographers())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.PHOTOGRAPHER));
                                                                            movie.setPhotographers(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getCostumiers())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.COSTUMIER));
                                                                            movie.setCostumiers(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getDecorators())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.DECORATOR));
                                                                            movie.setDecorators(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getEditors())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.EDITOR));
                                                                            movie.setEditors(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getCasters())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.CASTER));
                                                                            movie.setCasters(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getArtDirectors())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.ART_DIRECTOR));
                                                                            movie.setArtDirectors(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getSoundEditors())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.SOUND_EDITOR));
                                                                            movie.setSoundEditors(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getVisualEffectsSupervisors())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.VISUAL_EFFECTS_SUPERVISOR));
                                                                            movie.setVisualEffectsSupervisors(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getMakeupArtists())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.MAKEUP_ARTIST));
                                                                            movie.setMakeupArtists(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getHairDressers())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.HAIR_DRESSER));
                                                                            movie.setHairDressers(personSet);
                                                                        })
                                                                )
                                                                .chain(() -> personService.getByIds(technicalTeam.getStuntmen())
                                                                        .invoke(personSet -> {
                                                                            personSet.forEach(person -> person.getTypes().add(PersonType.STUNT_MAN));
                                                                            movie.setStuntmen(personSet);
                                                                        })
                                                                )
                                                                .invoke(() -> movie.setLastUpdate(LocalDateTime.now()))
                                        )
                                        .map(TechnicalTeamDTO::fromEntity)
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> saveCast(Long id, List<MovieActorDTO> movieActorsList) {
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
                                                            existing.setRole(dto.getRole().trim());
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
                                                            personService.getByIds(newActorIds)
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
                                                                    .call(statsService::updateNumberOfActors)
                                                                    .replaceWith(movie)
                                                            ;
                                                })
                                )
                                .chain(movieRepository::persist)
                                .call(movieActorRepository::flush) // Force la génération des IDs
                                .flatMap(movie ->
                                        Mutiny.fetch(movie.getMovieActors())
                                                .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                                                .map(personService::fromMovieActorListEntity)
                                )
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
                                                                .chain(existingAwards -> {
                                                                            // Récupérer toutes les personnes liées aux DTO (même si dupliquées, on fera le set plus tard)
                                                                            List<Long> allPersonIds = awardDTOSet.stream()
                                                                                    .filter(dto -> Objects.nonNull(dto.getPersons()))
                                                                                    .flatMap(dto -> dto.getPersons().stream())
                                                                                    .map(PersonDTO::getId)
                                                                                    .filter(Objects::nonNull)
                                                                                    .distinct()
                                                                                    .toList();

                                                                            return personService.getByIds(allPersonIds)
                                                                                    .map(personList -> {
                                                                                                Map<Long, Person> personMap = personList.stream()
                                                                                                        .collect(Collectors.toMap(Person::getId, p -> p));

                                                                                                // Mise à jour des récompenses existantes
                                                                                                existingAwards.forEach(award ->
                                                                                                        awardDTOSet.stream()
                                                                                                                .filter(a -> Objects.nonNull(a.getId()) && a.getId().equals(award.getId()))
                                                                                                                .findFirst()
                                                                                                                .ifPresent(dto -> {
                                                                                                                    award.setCeremony(StringUtils.capitalize(StringUtils.defaultString(dto.getCeremony()).trim()));
                                                                                                                    award.setName(StringUtils.capitalize(StringUtils.defaultString(dto.getName()).trim()));
                                                                                                                    award.setYear(dto.getYear());

                                                                                                                    Set<Person> linkedPersons = dto.getPersons().stream()
                                                                                                                            .map(PersonDTO::getId)
                                                                                                                            .map(personMap::get)
                                                                                                                            .filter(Objects::nonNull)
                                                                                                                            .collect(Collectors.toSet());

                                                                                                                    award.setPersonSet(linkedPersons);
                                                                                                                })
                                                                                                );

                                                                                                // Suppression des récompenses obsolètes
                                                                                                existingAwards.removeIf(existing ->
                                                                                                        awardDTOSet.stream().noneMatch(updated ->
                                                                                                                Objects.nonNull(updated.getId()) && updated.getId().equals(existing.getId())
                                                                                                        )
                                                                                                );

                                                                                                // Ajout des nouvelles récompenses
                                                                                                awardDTOSet.stream()
                                                                                                        .filter(dto -> Objects.isNull(dto.getId()))
                                                                                                        .forEach(dto -> {
                                                                                                            Award newAward = Award.fromDTO(dto);
                                                                                                            newAward.setMovie(movie);

                                                                                                            if (Objects.nonNull(dto.getPersons())) {
                                                                                                                Set<Person> linkedPersons = dto.getPersons().stream()
                                                                                                                        .map(PersonDTO::getId)
                                                                                                                        .map(personMap::get)
                                                                                                                        .filter(Objects::nonNull)
                                                                                                                        .collect(Collectors.toSet());

                                                                                                                newAward.setPersonSet(linkedPersons);
                                                                                                            }
                                                                                                            existingAwards.add(newAward);
                                                                                                        });

                                                                                                return movie.getAwards();
                                                                                            }

                                                                                    )
                                                                                    .call(awardRepository::persist)
                                                                                    .call(awardRepository::flush)
                                                                                    .map(AwardDTO::fromEntitySet);
                                                                        }
                                                                )
                                        )
                        )
                ;
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
                                        .flatMap(movie -> statsService.updateMoviesByGenreRepartition().replaceWith(movie))
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
                                        .flatMap(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCountries)
                        );
    }

    /**
     * Met à jour la liste des personnes associées à un film en supprimant les anciennes entrées,
     * en ajoutant de nouvelles personnes et en récupérant celles existantes.
     *
     * @param id           L'identifiant du film.
     * @param personDTOSet L'ensemble des personnes à enregistrer sous forme de {@link PersonDTO}.
     * @param getPeople    Fonction permettant de récupérer la liste des personnes associées au film (ex: Movie::getProducers).
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} mis à jour des personnes associées au film.
     * @throws IllegalArgumentException si le film n'est pas trouvé.
     */
    public Uni<Set<PersonDTO>> savePeople(Long
                                                  id, Set<PersonDTO> personDTOSet, Function<Movie, Set<Person>> getPeople) {
        return Panache.withTransaction(() ->
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                        .chain(movie ->
                                Mutiny.fetch(getPeople.apply(movie))
                                        .chain(existingPeople -> {
                                            Set<Person> updatedPeople = new HashSet<>(existingPeople);

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
                                                        Person newPerson = Person.build(personDTO);
                                                        updatedPeople.add(newPerson);
                                                    });

                                            // Récupérer et ajouter les personnes existantes
                                            return personService.getByIds(
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
     * @param id           L'identifiant du film auquel les personnes doivent être ajoutées.
     * @param personDTOSet L'ensemble des personnes à ajouter, sous forme de DTO.
     * @param getPeople    Une fonction permettant de récupérer l'ensemble des personnes déjà associées au film.
     * @param errorMessage Le message d'erreur à utiliser en cas d'échec de l'opération.
     * @return Une instance de {@link Uni} contenant l'ensemble des personnes ajoutées sous forme de {@link PersonDTO}.
     * En cas d'erreur, une exception est levée avec un message approprié.
     * @throws IllegalArgumentException Si le film n'est pas trouvé ou si certaines personnes sont introuvables.
     * @throws IllegalStateException    Si une erreur se produit lors de la récupération des personnes après la mise à jour.
     */
    public Uni<Set<PersonDTO>> addPeople(Long
                                                 id, Set<PersonDTO> personDTOSet, Function<Movie, Set<Person>> getPeople, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film introuvable"))
                                        .flatMap(movie ->
                                                personService.getByIds(personDTOSet)
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Une ou plusieurs personnes sont introuvables"))
                                                        .call(personSet -> movie.addPeople(getPeople.apply(movie), personSet, errorMessage))
                                                        .replaceWith(movie)
                                        )
                                        .flatMap(movieRepository::persist)
                                        .flatMap(movie ->
                                                Mutiny.fetch(getPeople.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                                                        .map(personService::fromPersonSetEntity)
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
                                                personService.getByIds(
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
                                                        .map(personService::fromMovieActorListEntity)
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
                                                        .flatMap(movie::addGenres)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByGenreRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapGenres)
                                        .invoke(() -> log.info("Genres ajoutés au film {}", movieId))
                        )
                        .onFailure().invoke(e -> log.error("Erreur lors de l'ajout des genres au film {} : {}", movieId, e.getMessage()))
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
                                                        .flatMap(movie::addCountries)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCountries)
                                        .invoke(() -> log.info("Pays ajoutés au film {}", movieId))
                        )
                        .onFailure().invoke(e -> log.error("Erreur lors de l'ajout des pays au film {} : {}", movieId, e.getMessage()))
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
     * @param errorMessage Message d'erreur si la liste des personnes n'est pas initialisée.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} :
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws IllegalStateException    si la collection de personnes n'est pas initialisée pour ce film.
     */
    public Uni<Set<PersonDTO>> removePerson(Long movieId, Long
            personId, Function<Movie, Set<Person>> peopleGetter, String errorMessage) {
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
                                                        .map(personService::fromPersonSetEntity)
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
                                                        .map(personService::fromMovieActorListEntity)
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
                                        .chain(movie -> statsService.updateMoviesByGenreRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapGenres)
                                        .invoke(() -> log.info("Category {} removed from movie {}", genreId, movieId))
                        )
                        .onFailure().invoke(e -> log.error("Failed to remove genre from movie {}: {}", movieId, e.getMessage()))
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
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
                                        .flatMap(this::fetchAndMapCountries)
                                        .invoke(() -> log.info("Country {} removed from movie {}", countryId, movieId))
                        )
                        .onFailure().invoke(e -> log.error("Failed to remove country from movie {}: {}", movieId, e.getMessage()))
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
                                        .flatMap(
                                                movie -> {
                                                    List<Long> newCountryIds = movieDTO.getCountries().stream().map(CountryDTO::getId).toList();
                                                    List<Long> newGenreIds = movieDTO.getGenres().stream().map(GenreDTO::getId).toList();

                                                    boolean shouldUpdateReleaseDate = !movieDTO.getReleaseDate().equals(movie.getReleaseDate());
                                                    AtomicBoolean shouldUpdateCountries = new AtomicBoolean(false);
                                                    AtomicBoolean shouldUpdateGenres = new AtomicBoolean(false);

                                                    return
                                                            Mutiny.fetch(movie.getCountries())
                                                                    .invoke(countries -> shouldUpdateCountries.set(!new HashSet<>(newCountryIds)
                                                                                    .equals(
                                                                                            new HashSet<>(countries.stream()
                                                                                                    .map(Country::getId)
                                                                                                    .toList())
                                                                                    )
                                                                            )
                                                                    )
                                                                    .chain(() ->
                                                                            Mutiny.fetch(movie.getGenres())
                                                                                    .invoke(genres -> shouldUpdateGenres.set(!new HashSet<>(newGenreIds)
                                                                                                    .equals(
                                                                                                            new HashSet<>(genres.stream()
                                                                                                                    .map(Genre::getId)
                                                                                                                    .toList())
                                                                                                    )
                                                                                            )
                                                                                    )
                                                                    )
                                                                    .chain(ignore ->
                                                                            countryService.getByIds(newCountryIds).invoke(movie::setCountries)
                                                                                    .chain(() -> genreService.getByIds(newGenreIds).invoke(movie::setGenres))
                                                                                    .invoke(() -> {
                                                                                                movie.setTitle(movieDTO.getTitle());
                                                                                                movie.setOriginalTitle(movieDTO.getOriginalTitle());
                                                                                                movie.setSynopsis(movieDTO.getSynopsis());
                                                                                                movie.setReleaseDate(movieDTO.getReleaseDate());
                                                                                                movie.setRunningTime(movieDTO.getRunningTime());
                                                                                                movie.setBudget(movieDTO.getBudget());
                                                                                                movie.setBudgetCurrency(movieDTO.getBudgetCurrency());
                                                                                                movie.setPosterFileName(Optional.ofNullable(movie.getPosterFileName()).orElse(DEFAULT_POSTER));
                                                                                                movie.setBoxOffice(movieDTO.getBoxOffice());
                                                                                                movie.setBoxOfficeCurrency(movieDTO.getBoxOfficeCurrency());
                                                                                            }
                                                                                    )
                                                                                    .flatMap(genres -> {
                                                                                                if (Objects.nonNull(file)) {
                                                                                                    return uploadPoster(file)
                                                                                                            .onFailure().invoke(error -> log.error("Poster upload failed for movie {}: {}", id, error.getMessage()))
                                                                                                            .invoke(movie::setPosterFileName)
                                                                                                            .replaceWith(movie);
                                                                                                }
                                                                                                return Uni.createFrom().item(movie);
                                                                                            }
                                                                                    )
                                                                                    .flatMap(entity -> {
                                                                                        Uni<Void> releaseDateUpdate = shouldUpdateReleaseDate ? statsService.updateMoviesByReleaseDateRepartition() : Uni.createFrom().voidItem();
                                                                                        Uni<Void> genreUpdate = shouldUpdateGenres.get() ? statsService.updateMoviesByGenreRepartition() : Uni.createFrom().voidItem();
                                                                                        Uni<Void> countryUpdate = shouldUpdateCountries.get() ? statsService.updateMoviesByCountryRepartition() : Uni.createFrom().voidItem();
                                                                                        return releaseDateUpdate.chain(() -> genreUpdate.chain(() -> countryUpdate)).replaceWith(entity);
                                                                                    })
                                                                    );
                                                })
                        );
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
     * @param id           L'identifiant du film dont les personnes associées doivent être supprimées.
     * @param peopleGetter Une fonction permettant d'obtenir l'ensemble des personnes à partir du film (par exemple, acteurs ou réalisateurs).
     * @param errorMessage Le message d'erreur à utiliser si l'ensemble des personnes est nul.
     * @return Un {@link Uni} contenant `true` si l'opération a été réalisée avec succès.
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws WebApplicationException  Si une erreur se produit lors de la suppression des personnes.
     */
    public Uni<Boolean> clearPersons(Long id, Function<Movie, Set<Person>> peopleGetter, String errorMessage) {
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
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByGenreRepartition().replaceWith(movie))
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
                                        .chain(movieRepository::persist)
                                        .chain(movie -> statsService.updateMoviesByCountryRepartition().replaceWith(movie))
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
                        .map(GenreDTO::fromGenreSetEntity)
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
                        .map(CountryDTO::fromCountryEntitySet)
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
                        .map(AwardDTO::fromEntitySet)
                ;
    }
}
