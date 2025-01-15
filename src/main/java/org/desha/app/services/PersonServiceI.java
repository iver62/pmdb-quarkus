/*
package org.desha.app.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class PersonService {

    private final Mutiny.SessionFactory msf;
    private final MovieService movieService;
    private final PersonRepository personRepository;

    public PersonService(
            Mutiny.SessionFactory msf,
            MovieService movieService,
            PersonRepository personRepository
    ) {
        this.msf = msf;
        this.movieService = movieService;
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

//    public Uni<Set<Person>> getProducers() {
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
//    }

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

//    public Uni<Set<Role>> getRolesByActor(Person person) {
//        return
//                Mutiny.fetch(person.getRoles())
//                        .map(
//                                roles ->
//                                        roles
//                                                .stream()
//                                                .map(role -> Role.build(role.getMovie(), null, role.getName()))
//                                                .collect(Collectors.toSet())
//                        );
//    }

    public Uni<Set<Country>> getCountries(Person person) {
        return Mutiny.fetch(person.getCountries());
    }

    public Uni<Set<Award>> getAwards(Person person) {
        return Mutiny.fetch(person.getAwards());
    }

//    public Uni<Movie> addCountries(Long id, Set<Country> countrySet) {
//        return
//                Panache
//                        .withTransaction(() ->
//                                personRepository.findById(id)
//                                        .onItem().ifNotNull()
//                                        .call(
//                                                person ->
//                                                        Uni.join().all(
//                                                                        countrySet
//                                                                                .stream()
//                                                                                .map(country -> msf.openSession().chain(() -> country.addPerson(person)))
//                                                                                .toList()
//                                                                )
//                                                                .usingConcurrencyOf(1)
//                                                                .andFailFast()
//
//                                        )
//                                        .call(entity -> entity.addCountries(countrySet))
//                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
//                                        .chain(entity -> entity.persist())
//                        )
//                ;
//    }

//    public Uni<Movie> addAwards(Long id, Set<Award> awardSet) {
//        return
//                Panache
//                        .withTransaction(() ->
//                                personRepository.findById(id)
//                                        .onItem().ifNotNull()
//                                        .call(entity -> entity.addAwards(awardSet))
//                                        .invoke(entity -> entity.setLastUpdate(LocalDateTime.now()))
//                                        .chain(entity -> entity.persist())
//                        )
//                ;
//    }

//    public Uni<Person> updatePerson(Long id, Person person) {
//        return
//                Panache
//                        .withTransaction(() ->
//                                        personRepository.findById(id)
//                                                .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
//                                                .invoke(
//                                                        entity -> {
//                                                            entity.setName(person.getName());
//                                                            entity.setDateOfBirth(person.getDateOfBirth());
//                                                            entity.setDateOfDeath(person.getDateOfDeath());
//                                                            entity.setPhotoPath(person.getPhotoPath());
//                                                            entity.setLastUpdate(LocalDateTime.now());
//                                                        }
//                                                )
//                        )
//                ;
//    }
}
*/
