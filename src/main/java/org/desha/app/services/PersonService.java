package org.desha.app.services;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.desha.app.domain.*;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
public class PersonService {

    @Inject
    Mutiny.SessionFactory msf;

    private final MovieService movieService;
    private final PersonRepository personRepository;

    public PersonService(MovieService movieService, PersonRepository personRepository) {
        this.movieService = movieService;
        this.personRepository = personRepository;
    }

    public Uni<Set<Person>> getProducers() {
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
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getProducersByMovie(movie)))
                                                                        .toList()
                                                        )
                                                        .usingConcurrencyOf(1)
                                                        .andFailFast()
                                                        .map(
                                                                sets ->
                                                                        sets
                                                                                .stream()
                                                                                .flatMap(Collection::stream)
                                                                                .collect(Collectors.toSet())
                                                        )
                        )
                ;
    }

    public Uni<Set<Person>> getDirectors() {
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
                                                                        .map(movie -> msf.openSession().chain(() -> movieService.getDirectorsByMovie(movie)))
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

    /**
     * Retourne tous les films d'une personne en tant que producteur.
     *
     * @param person les films de la personne à retourner
     * @return un ensemble de films
     */
    public Uni<Set<Movie>> getMoviesAsProducer(Person person) {
        return Mutiny.fetch(person.getMoviesAsProducer());
    }

    public Uni<Set<Movie>> getMoviesAsDirector(Person person) {
        return Mutiny.fetch(person.getMoviesAsDirector());
    }

    public Uni<Set<Movie>> getMoviesAsScreenwriter(Person person) {
        return Mutiny.fetch(person.getMoviesAsScreenWriter());
    }

    public Uni<Set<Movie>> getMoviesAsMusician(Person person) {
        return Mutiny.fetch(person.getMoviesAsMusician());
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

    public Uni<Set<Movie>> removeMovieAsProducer(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsProducer(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsDirector(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsDirector(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsScreenwriter(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsScreenwriter(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsMusician(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsMusician(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsPhotographer(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsPhotographer(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsCostumier(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsCostumier(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsDecorator(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovieAsDecorator(movieId))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsEditor(Long personId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
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
                                                    entity.setLastName(person.getLastName());
                                                    entity.setFirstName(person.getFirstName());
                                                    entity.setSecondName(person.getSecondName());
                                                    entity.setThirdName(person.getThirdName());
                                                    entity.setPseudo(person.getPseudo());
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
