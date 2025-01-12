package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.TechnicalSummaryDTO;
import org.desha.app.domain.entity.*;
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
    private final PersonService personService;

    @Inject
    public MovieService(
            Mutiny.SessionFactory msf,
            CountryService countryService,
            GenreService genreService,
            MovieRepository movieRepository,
            PersonService personService
    ) {
        this.msf = msf;
        this.countryService = countryService;
        this.genreService = genreService;
        this.movieRepository = movieRepository;
        this.personService = personService;
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

    public Uni<Set<Person>> getProducersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getProducers());
    }

    public Uni<Set<Director>> getDirectorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDirectors());
    }

    public Uni<Set<Person>> getScreenwritersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getScreenwriters());
    }

    public Uni<Set<Person>> getMusiciansByMovie(Movie movie) {
        return Mutiny.fetch(movie.getMusicians());
    }

    public Uni<Set<Person>> getPhotographersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getPhotographers());
    }

    public Uni<Set<Person>> getCostumiersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCostumiers());
    }

    public Uni<Set<Person>> getDecoratorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getDecorators());
    }

    public Uni<Set<Person>> getEditorsByMovie(Movie movie) {
        return Mutiny.fetch(movie.getEditors());
    }

    public Uni<Set<Person>> getCastersByMovie(Movie movie) {
        return Mutiny.fetch(movie.getCasters());
    }

    public Uni<Set<Role>> getActorsByMovie(Movie movie) {
        return
                Mutiny.fetch(movie.getRoles())
                        .map(
                                roles ->
                                        roles
                                                .stream()
                                                .map(role -> Role.build(null, role.getActor(), role.getName()))
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
                                                .invoke(countryList -> movie.setCountries(new HashSet<>(countryList))))
                        .call(
                                movie ->
                                        genreService.getByIds(movieDTO.getGenres())
                                                .invoke(genreList -> movie.setGenres(new HashSet<>(genreList)))
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

    public Uni<TechnicalSummaryDTO> saveTechnicalSummary(Long id, TechnicalSummaryDTO technicalSummary) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Film non trouvé"))
                                        .call(
                                                movie ->
                                                        personService.getByIds(technicalSummary.getProducers())
                                                                .invoke(people -> movie.setProducers(new HashSet<>(people)))
                                                                .chain(() ->
                                                                        personService.getDirectorByIds(technicalSummary.getDirectors())
                                                                                .invoke(directors -> movie.setDirectors(new HashSet<>(directors)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getScreenwriters())
                                                                                .invoke(people -> movie.setScreenwriters(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getMusicians())
                                                                                .invoke(people -> movie.setMusicians(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getPhotographers())
                                                                                .invoke(people -> movie.setPhotographers(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getCostumiers())
                                                                                .invoke(people -> movie.setCostumiers(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getDecorators())
                                                                                .invoke(people -> movie.setDecorators(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getEditors())
                                                                                .invoke(people -> movie.setEditors(new HashSet<>(people)))
                                                                )
                                                                .chain(() ->
                                                                        personService.getByIds(technicalSummary.getCasters())
                                                                                .invoke(people -> movie.setCasters(new HashSet<>(people)))
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
                                                        TechnicalSummaryDTO.build(
                                                                movie.getProducers(),
                                                                movie.getDirectors(),
                                                                movie.getScreenwriters(),
                                                                movie.getMusicians(),
                                                                movie.getPhotographers(),
                                                                movie.getCostumiers(),
                                                                movie.getDecorators(),
                                                                movie.getEditors(),
                                                                movie.getCasters()
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
    public Uni<Movie> addRole(Long id, Role role) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.addRole(Role.build(entity, role.getActor(), role.getName())))
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

    public Uni<Movie> removeProducer(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeProducer(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeDirector(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeDirector(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeScreenwriter(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeScreenwriter(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeMusician(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeMusician(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removePhotographer(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removePhotographer(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeCostumier(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeCostumier(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeDecorator(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeDecorator(personId))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Movie> removeEditor(Long movieId, Long personId) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.removeEditor(personId))
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
                                                                        .invoke(countryList -> movie.setCountries(new HashSet<>(countryList)))
                                                )
                                                .call(
                                                        movie ->
                                                                genreService.getByIds(movieDTO.getGenres())
                                                                        .invoke(genreList -> movie.setGenres(new HashSet<>(genreList)))
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
