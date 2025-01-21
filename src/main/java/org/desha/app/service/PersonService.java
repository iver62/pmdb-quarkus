package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
@Dependent
public class PersonService<T extends Person> implements PersonServiceInterface<T> {

    private final PersonRepository<T> personRepository;

    @Inject
    public PersonService(PersonRepository<T> personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Uni<T> getOne(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public Uni<Set<T>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::id)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<Set<T>> getAll() {
        return personRepository.listAll().map(HashSet::new);
    }

    public Uni<Set<Movie>> getMovies(T t) {
        return Mutiny.fetch(t.getMovies());
    }

    public Uni<Set<Movie>> addMovie(Long id, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<T> save(PersonDTO personDTO, T instance) {
        return
                Panache
                        .withTransaction(() -> {
                                    instance.setName(StringUtils.trim(personDTO.name()));
                                    instance.setCreationDate(LocalDateTime.now());
                                    return instance.persist();
                                }
                        )
                ;
    }

    public Uni<T> update(Long id, PersonDTO personDTO) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(new WebApplicationException("Person missing from database.", NOT_FOUND))
                                        .invoke(
                                                entity -> {
                                                    entity.setName(personDTO.name());
                                                    entity.setDateOfBirth(personDTO.dateOfBirth());
                                                    entity.setDateOfDeath(personDTO.dateOfDeath());
                                                    entity.setPhotoPath(personDTO.photoPath());
                                                    entity.setLastUpdate(LocalDateTime.now());
                                                }
                                        )
                        )
                ;
    }

    public Uni<Boolean> delete(Long id) {
        return Panache.withTransaction(() -> personRepository.deleteById(id));
    }
}
