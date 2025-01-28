package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.TechnicalSummaryDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.repository.MovieRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MovieService {

    private final Mutiny.SessionFactory msf;
    private final MovieRepository movieRepository;
    private final CountryService countryService;
    private final GenreService genreService;

    private final PersonService<ArtDirector> artDirectorService;
    private final PersonService<Caster> casterService;
    private final PersonService<Costumier> costumierService;
    private final PersonService<Decorator> decoratorService;
    private final PersonService<Director> directorService;
    private final PersonService<Editor> editorService;
    private final PersonService<HairDresser> hairDresserService;
    private final PersonService<MakeupArtist> makeupArtistService;
    private final PersonService<Musician> musicianService;
    private final PersonService<Photographer> photographerService;
    private final PersonService<Producer> producerService;
    private final PersonService<Screenwriter> screenwriterService;
    private final PersonService<SoundEditor> soundEditorService;
    private final PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService;

    @Inject
    public MovieService(
            Mutiny.SessionFactory msf,
            CountryService countryService,
            GenreService genreService,
            MovieRepository movieRepository,
            @PersonType(Role.ART_DIRECTOR) PersonService<ArtDirector> artDirectorService,
            @PersonType(Role.CASTER) PersonService<Caster> casterService,
            @PersonType(Role.COSTUMIER) PersonService<Costumier> costumierService,
            @PersonType(Role.DECORATOR) PersonService<Decorator> decoratorService,
            @PersonType(Role.DIRECTOR) PersonService<Director> directorService,
            @PersonType(Role.EDITOR) PersonService<Editor> editorService,
            @PersonType(Role.HAIR_DRESSER) PersonService<HairDresser> hairDresserService,
            @PersonType(Role.MAKEUP_ARTIST) PersonService<MakeupArtist> makeupArtistService,
            @PersonType(Role.MUSICIAN) PersonService<Musician> musicianService,
            @PersonType(Role.PHOTOGRAPHER) PersonService<Photographer> photographerService,
            @PersonType(Role.PRODUCER) PersonService<Producer> producerService,
            @PersonType(Role.SCREENWRITER) PersonService<Screenwriter> screenwriterService,
            @PersonType(Role.SOUND_EDITOR) PersonService<SoundEditor> soundEditorService,
            @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService
    ) {
        this.msf = msf;
        this.countryService = countryService;
        this.genreService = genreService;
        this.movieRepository = movieRepository;
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
    }

    public Uni<Movie> getSingle(Long id) {
        return movieRepository.findById(id);
    }

    public Uni<Set<Movie>> getAll() {
        return movieRepository.listAll().map(HashSet::new);
    }

    public Uni<Set<Movie>> getByTitle(String pattern) {
        return movieRepository.findByTitle(pattern).map(HashSet::new);
    }

    public Uni<Set<Producer>> getProducersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getProducers());
    }

    public Uni<Set<Director>> getDirectorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDirectors());
    }

    public Uni<Set<Screenwriter>> getScreenwritersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getScreenwriters());
    }

    public Uni<Set<Musician>> getMusiciansByMovie(Movie movie) {
        return Mutiny.fetch(movie.getMusicians());
    }

    public Uni<Set<Photographer>> getPhotographersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getPhotographers());
    }

    public Uni<Set<Costumier>> getCostumiersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCostumiers());
    }

    public Uni<Set<Decorator>> getDecoratorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDecorators());
    }

    public Uni<Set<Editor>> getEditorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getEditors());
    }

    public Uni<Set<Caster>> getCastersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCasters());
    }

    public Uni<Set<ArtDirector>> getArtDirectorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getArtDirectors());
    }

    public Uni<Set<SoundEditor>> getSoundEditorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getSoundEditors());
    }

    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getVisualEffectsSupervisors());
    }

    public Uni<Set<MakeupArtist>> getMakeupArtists(Movie movie) {
        return Mutiny.fetch(movie.getMakeupArtists());
    }

    public Uni<Set<HairDresser>> getHairDressers(Movie movie) {
        return Mutiny.fetch(movie.getHairDressers());
    }

    public Uni<Set<MovieActor>> getActorsByMovie(Movie movie) {
        return
                Mutiny.fetch(movie.getMovieActors())
                        .map(
                                roles ->
                                        roles
                                                .stream()
                                                .map(role -> MovieActor.build(null, role.getActor(), role.getName()))
                                                .collect(Collectors.toSet())
                        )
                ;
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

    public Uni<Movie> createMovie(MovieDTO movieDTO) {
/*        return
                Uni.combine().all()
                        .unis(
                                countryService.getByIds(movieDTO.getCountries()),
                                genreService.getByIds(movieDTO.getGenres())
                        )
                        .asTuple()
                        .onItem().transformToUni(
                                listTuple -> {
                                    final Movie movie = Movie.build(movieDTO);
                                    movie.setCountries(new HashSet<>(listTuple.getItem1()));
                                    movie.setGenres(new HashSet<>(listTuple.getItem2()));
                                    return Panache.withTransaction(movie::persist);
                                }
                        );*/

        return
                Uni.createFrom()
                        .item(Movie.build(movieDTO))
                        .call(
                                movie ->
                                        countryService.getByIds(movieDTO.getCountries())
                                                .invoke(movie::setCountries)
                                                .chain(() ->
                                                        genreService.getByIds(movieDTO.getGenres())
                                                                .invoke(movie::setGenres)
                                                )
                        )
                        .chain(movie -> Panache.withTransaction(movie::persist));

        /*List<Uni<Country>> countryUnis = Optional.ofNullable(movieDTO.getCountries())
                .stream()
                .flatMap(Collection::stream)
                .map(
                        c ->
                                countryRepository
                                        .findById(c.id)
                                        .onFailure().recoverWithNull()
                )
                .toList();

        List<Uni<Genre>> genreUnis = Optional.ofNullable(movieDTO.getGenres())
                .stream()
                .flatMap(Collection::stream)
                .map(
                        g ->
                                genreRepository
                                        .findById(g.id)
                                        .onFailure().recoverWithNull()
                )
                .toList();

        return
                Uni.join()
                        .all(countryUnis.isEmpty() ? List.of(Uni.createFrom().nullItem()) : countryUnis)
                        .usingConcurrencyOf(1)
                        .andCollectFailures()
                        .onItem().ifNull().continueWith(Collections.emptyList())
                        .onItem().transform(countries -> {
                            movie.setCountries(new HashSet<>(countries));
                            return movie;
                        })
                        .chain(() ->
                                Uni.join()
                                        .all(genreUnis.isEmpty() ? List.of(Uni.createFrom().nullItem()) : genreUnis)
                                        .usingConcurrencyOf(1)
                                        .andCollectFailures()
                                        .onItem().ifNull().continueWith(Collections.emptyList())
                                        .onItem().transform(genres -> {
                                            movie.setGenres(new HashSet<>(genres));
                                            return movie;
                                        })
                        )
                        .onItem()
                        .transformToUni(movie1 -> Panache.withTransaction(movie::persist));*/
    }

    public Uni<TechnicalSummary> saveTechnicalSummary(Long id, TechnicalSummaryDTO technicalSummary) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(
                                                movie ->
                                                        producerService.getByIds(technicalSummary.getProducers())
                                                                .invoke(movie::setProducers)
                                                                .chain(() ->
                                                                        directorService.getByIds(technicalSummary.getDirectors())
                                                                                .invoke(movie::setDirectors)
                                                                )
                                                                .chain(() ->
                                                                        screenwriterService.getByIds(technicalSummary.getScreenwriters())
                                                                                .invoke(movie::setScreenwriters)
                                                                )
                                                                .chain(() ->
                                                                        musicianService.getByIds(technicalSummary.getMusicians())
                                                                                .invoke(movie::setMusicians)
                                                                )
                                                                .chain(() ->
                                                                        photographerService.getByIds(technicalSummary.getPhotographers())
                                                                                .invoke(movie::setPhotographers)
                                                                )
                                                                .chain(() ->
                                                                        costumierService.getByIds(technicalSummary.getCostumiers())
                                                                                .invoke(movie::setCostumiers)
                                                                )
                                                                .chain(() ->
                                                                        decoratorService.getByIds(technicalSummary.getDecorators())
                                                                                .invoke(movie::setDecorators)
                                                                )
                                                                .chain(() ->
                                                                        editorService.getByIds(technicalSummary.getEditors())
                                                                                .invoke(movie::setEditors)
                                                                )
                                                                .chain(() ->
                                                                        casterService.getByIds(technicalSummary.getCasters())
                                                                                .invoke(movie::setCasters)
                                                                )
                                                                .chain(() ->
                                                                        artDirectorService.getByIds(technicalSummary.getArtDirectors())
                                                                                .invoke(movie::setArtDirectors)
                                                                )
                                                                .chain(() ->
                                                                        soundEditorService.getByIds(technicalSummary.getSoundEditors())
                                                                                .invoke(movie::setSoundEditors)
                                                                )
                                                                .chain(() ->
                                                                        visualEffectsSupervisorService.getByIds(technicalSummary.getVisualEffectsSupervisors())
                                                                                .invoke(movie::setVisualEffectsSupervisors)
                                                                )
                                                                .chain(() ->
                                                                        makeupArtistService.getByIds(technicalSummary.getMakeupArtists())
                                                                                .invoke(movie::setMakeupArtists)
                                                                )
                                                                .chain(() ->
                                                                        hairDresserService.getByIds(technicalSummary.getHairDressers())
                                                                                .invoke(movie::setHairDressers)
                                                                )
                                        )
                                        /*.call(
                                                movie ->
                                                        personService.getDirectorByIds(technicalSummary.getDirectors())
                                                                .invoke(directors -> movie.setDirectors(new HashSet<>(directors)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getScreenwriters())
                                                                .invoke(people -> movie.setScreenwriters(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getMusicians())
                                                                .invoke(people -> movie.setMusicians(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getPhotographers())
                                                                .invoke(people -> movie.setPhotographers(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getCostumiers())
                                                                .invoke(people -> movie.setCostumiers(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getDecorators())
                                                                .invoke(people -> movie.setDecorators(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getEditors())
                                                                .invoke(people -> movie.setEditors(new HashSet<>(people)))
                                        )
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getCasters())
                                                                .invoke(people -> movie.setCasters(new HashSet<>(people)))
                                        )*/
                                        /*.chain(entity ->
                                                Uni.combine().all().unis(
                                                                personService.getByIds(technicalSummary.getProducers()),
                                                                personService.getDirectorByIds(technicalSummary.getDirectors()),
                                                                personService.getByIds(technicalSummary.getScreenwriters()),
                                                                personService.getByIds(technicalSummary.getMusicians()),
                                                                personService.getByIds(technicalSummary.getPhotographers()),
                                                                personService.getByIds(technicalSummary.getCostumiers()),
                                                                personService.getByIds(technicalSummary.getDecorators()),
                                                                personService.getByIds(technicalSummary.getEditors()),
                                                                personService.getByIds(technicalSummary.getCasters())
                                                        )
                                                        .asTuple()
                                                        .chain(
                                                                lists -> {
                                                                    entity.setProducers(new HashSet<>(lists.getItem1()));
                                                                    entity.setDirectors(new HashSet<>(lists.getItem2()));
                                                                    entity.setScreenwriters(new HashSet<>(lists.getItem3()));
                                                                    entity.setMusicians(new HashSet<>(lists.getItem4()));
                                                                    entity.setPhotographers(new HashSet<>(lists.getItem5()));
                                                                    entity.setCostumiers(new HashSet<>(lists.getItem6()));
                                                                    entity.setDecorators(new HashSet<>(lists.getItem7()));
                                                                    entity.setEditors(new HashSet<>(lists.getItem8()));
                                                                    entity.setCasters(new HashSet<>(lists.getItem9()));
                                                                    return movieRepository.persist(entity);
                                                                }
                                                        )
                                        )*/
                                        .map(
                                                movie ->
                                                        TechnicalSummary.build(
                                                                movie.getProducers(),
                                                                movie.getDirectors(),
                                                                movie.getScreenwriters(),
                                                                movie.getMusicians(),
                                                                movie.getPhotographers(),
                                                                movie.getCostumiers(),
                                                                movie.getDecorators(),
                                                                movie.getEditors(),
                                                                movie.getCasters(),
                                                                movie.getArtDirectors(),
                                                                movie.getSoundEditors(),
                                                                movie.getVisualEffectsSupervisors(),
                                                                movie.getMakeupArtists(),
                                                                movie.getHairDressers()
                                                        )
                                        )
                        )
                ;
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .call(entity -> entity.addRole(MovieActor.build(entity, movieActor.getActor(), movieActor.getName())))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(
                                                entity -> {
                                                    awardSet.forEach(award -> award.setMovie(entity));
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
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
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> updateMovie(Long id, MovieDTO movieDTO) {
        return
                Panache
                        .withTransaction(() ->
                                        movieRepository.findById(id)
                                                .onItem().ifNotNull()
                                                .call(
                                                        movie ->
                                                                countryService.getByIds(movieDTO.getCountries())
                                                                        .invoke(movie::setCountries)
                                                )
                                                .call(
                                                        movie ->
                                                                genreService.getByIds(movieDTO.getGenres())
                                                                        .invoke(movie::setGenres)
                                                )
                                                .invoke(
                                                        movie -> {
                                                            movie.setTitle(movieDTO.getTitle());
                                                            movie.setOriginalTitle(movieDTO.getOriginalTitle());
                                                            movie.setSynopsis(movieDTO.getSynopsis());
                                                            movie.setReleaseDate(movieDTO.getReleaseDate());
                                                            movie.setRunningTime(movieDTO.getRunningTime());
                                                            movie.setBudget(movieDTO.getBudget());
                                                            movie.setPosterPath(movieDTO.getPosterPath());
                                                            movie.setBoxOffice(movieDTO.getBoxOffice());
                                                            movie.setLastUpdate(LocalDateTime.now());
                                                        }
                                                )
//                                        .chain(
//                                                movie ->
//                                                        Uni.combine().all().unis(getCountriesByMovie(movie), getGenresByMovie(movie)).asTuple()
//                                                                .onItem().transform(
//                                                                        tuple ->
//                                                                                Movie.builder()
//                                                                                        .title(movie.getTitle())
//                                                                                        .originalTitle(movie.getOriginalTitle())
//                                                                                        .synopsis(movie.getSynopsis())
//                                                                                        .releaseDate(movie.getReleaseDate())
//                                                                                        .runningTime(movie.getRunningTime())
//                                                                                        .budget(movie.getBudget())
//                                                                                        .posterPath(movie.getPosterPath())
//                                                                                        .boxOffice(movie.getBoxOffice())
//                                                                                        .creationDate(movie.getCreationDate())
//                                                                                        .lastUpdate(LocalDateTime.now())
//                                                                                        .countries(tuple.getItem1())
//                                                                                        .genres(tuple.getItem2())
//                                                                                        .build()
//                                                                )
//                                        )
                        )
                ;

/*        Uni.combine().all().unis(countryService.getByIds(movieDTO.getCountries()), genreService.getByIds(movieDTO.getGenres())).asTuple()
                .chain(listTuple ->
                        Panache
                                .withTransaction(() ->
                                        movieRepository.findById(id)
                                                .onItem().ifNotNull()
                                                .invoke(
                                                        entity -> {
                                                            entity.setTitle(movieDTO.getTitle());
                                                            entity.setOriginalTitle(movieDTO.getOriginalTitle());
                                                            entity.setSynopsis(movieDTO.getSynopsis());
                                                            entity.setReleaseDate(movieDTO.getReleaseDate());
                                                            entity.setRunningTime(movieDTO.getRunningTime());
                                                            entity.setBudget(movieDTO.getBudget());
                                                            entity.setPosterPath(movieDTO.getPosterPath());
                                                            entity.setBoxOffice(movieDTO.getBoxOffice());
                                                            entity.setCountries(new HashSet<>(listTuple.getItem1()));
                                                            entity.setGenres(new HashSet<>(listTuple.getItem2()));
                                                            entity.setLastUpdate(LocalDateTime.now());
                                                        }
                                                )
                                )
                )
        ;*/
    }

    public Uni<Boolean> deleteMovie(Long id) {
        return Panache.withTransaction(() -> movieRepository.deleteById(id));
    }
}
