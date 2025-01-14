package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.*;
import org.desha.app.repository.DirectorRepository;
import org.desha.app.repository.PersonRepository;
import org.desha.app.repository.ProducerRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
@Slf4j
public class PersonService {

    private final DirectorService directorService;
    private final Mutiny.SessionFactory msf;
    private final MovieService movieService;
    private final PersonRepository personRepository;
    private final ProducerRepository producerRepository;

    public PersonService(
            DirectorService directorService,
            Mutiny.SessionFactory msf,
            ProducerRepository producerRepository,
            MovieService movieService,
            PersonRepository personRepository
    ) {
        this.directorService = directorService;
        this.msf = msf;
        this.movieService = movieService;
        this.producerRepository = producerRepository;
        this.personRepository = personRepository;
    }

    public Uni<List<Person>> getByIds(Set<Person> personSet) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(personSet).orElse(Collections.emptySet())
                                .stream()
                                .map(p -> p.id)
                                .toList()
                );
    }

    public Uni<Set<Person>> getProducers() {
        return producerRepository.listAll().map(HashSet::new);
//        return
//                movieService.getAll()
//                        .onItem().transformToUni(
//                                movies ->
//                                        movies.isEmpty()
//                                                ?
//                                                Uni.createFrom().nullItem()
//                                                :
//                                                Uni.join().all(
//                                                                movies
//                                                                        .stream()
//                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getProducersByMovie(movie)))
//                                                                        .toList()
//                                                        )
//                                                        .usingConcurrencyOf(1)
//                                                        .andFailFast()
//                                                        .map(
//                                                                sets ->
//                                                                        sets
//                                                                                .stream()
//                                                                                .flatMap(Collection::stream)
//                                                                                .collect(Collectors.toSet())
//                                                        )
//                        )
//                ;
    }

    public Uni<Set<Person>> getScreenwriters() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getScreenwritersByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Person>> getMusicians() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getMusiciansByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Person>> getPhotographers() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getPhotographersByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Person>> getCostumiers() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getCostumiersByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Person>> getDecorators() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getDecoratorsByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Person>> getEditors() {
        return
                movieService.getAll()
                        .onItem().transformToUni(
                                movies ->
                                        movies.isEmpty()
                                                ?
                                                Uni.createFrom().nullItem()
                                                :
                                                Uni.join().all(
                                                                movies
                                                                        .stream()
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getEditorsByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                        )
                        .map(
                                sets ->
                                        sets
                                                .stream()
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toSet())
                        )
                ;
    }

    public Uni<Set<Movie>> getMoviesAsPhotographer(Person person) {
        return Mutiny.fetch(person.getMoviesAsPhotographer());
    }

    public Uni<Set<Movie>> getMoviesAsCostumier(Person person) {
        return Mutiny.fetch(person.getMoviesAsCostumier());
    }

    public Uni<Set<Movie>> getMoviesAsDecorator(Person person) {
        return Mutiny.fetch(person.getMoviesAsDecorator());
    }

    public Uni<Set<Movie>> getMoviesAsEditor(Person person) {
        return Mutiny.fetch(person.getMoviesAsEditor());
    }

    public Uni<Set<Movie>> getMoviesAsArtDirector(Person person) {
        return Mutiny.fetch(person.getMoviesAsEditor());
    }

    public Uni<Set<Movie>> getMoviesAsSoundEditor(Person person) {
        return Mutiny.fetch(person.getMoviesAsEditor());
    }

    public Uni<Set<Role>> getRolesByActor(Person person) {
        return
                Mutiny.fetch(person.getRoles())
                        .map(
                                roles ->
                                        roles
                                                .stream()
                                                .map(role -> Role.build(role.getMovie(), null, role.getName()))
                                                .collect(Collectors.toSet())
                        );
    }

    public Uni<Set<Country>> getCountries(Person person) {
        return Mutiny.fetch(person.getCountries());
    }

    public Uni<Set<Award>> getAwards(Person person) {
        return Mutiny.fetch(person.getAwards());
    }

    public Uni<Movie> addCountries(Long id, Set<Country> countrySet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(
                                                person ->
                                                        Uni.join().all(
                                                                        countrySet
                                                                                .stream()
                                                                                .map(country -> msf.openSession().chain(() -> country.addPerson(person)))
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
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .call(entity -> entity.addAwards(awardSet))
                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
                                        .chain(entity -> entity.persist())
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsProducer(Long producerId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(producerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsProducer(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsDirector(Long directorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                directorService.getOne(directorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsScreenwriter(Long screenwriterId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(screenwriterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsScreenwriter(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsMusician(Long musicianId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(musicianId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsMusician(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsPhotographer(Long photographerId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(photographerId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsCostumier(Long costumierId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(costumierId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsCostumier(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsDecorator(Long decoratorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(decoratorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsDecorator(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsEditor(Long editorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(editorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsEditor(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsCaster(Long casterId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(casterId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsEditor(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsArtDirector(Long artDirectorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(artDirectorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsEditor(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsSoundEditor(Long soundEditorId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(soundEditorId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsEditor(movieId))
                        )
                ;
    }

    public Uni<Person> updatePerson(Long id, Person person) {
        return
                Panache
                        .withTransaction(() ->
                                        personRepository.findById(id)
                                                .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                                .invoke(
                                                        entity -> {
                                                            entity.setName(person.getName());
                                                            entity.setDateOfBirth(person.getDateOfBirth());
                                                            entity.setDateOfDeath(person.getDateOfDeath());
                                                            entity.setPhotoPath(person.getPhotoPath());
                                                            entity.setLastUpdate(LocalDateTime.now());
                                                        }
                                                )
                        )
                ;
    }
}
