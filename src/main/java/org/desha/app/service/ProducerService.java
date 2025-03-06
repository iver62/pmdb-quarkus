package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Producer;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.ProducerRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
@ApplicationScoped
public class ProducerService extends PersonServiceImpl {

    private final CountryService countryService;
    private final MovieRepository movieRepository;
    private final ProducerRepository producerRepository;

    @Inject
    public ProducerService(
            CountryService countryService,
            MovieRepository movieRepository,
            ProducerRepository producerRepository,
            FileService fileService
    ) {
        super(fileService);
        this.countryService = countryService;
        this.movieRepository = movieRepository;
        this.producerRepository = producerRepository;
    }

    public Uni<Long> count(
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        return producerRepository.count(term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate);
    }

    public Uni<Long> countMovies(Long producerId, String term) {
        return movieRepository.countMoviesByProducer(producerId, term);
    }

    public Uni<Producer> getById(Long id) {
        return
                producerRepository.findById(id)
                        .onItem().ifNotNull().call(t ->
                                Mutiny.fetch(t.getCountries())
                                        .invoke(t::setCountries)
                        )
                        .onFailure().recoverWithNull()
                ;
    }

    public Uni<Set<Producer>> getByIds(Set<PersonDTO> persons) {
        return
                producerRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<List<Producer>> getByIds(List<Long> ids) {
        return producerRepository.findByIds(ids);
    }

    public Uni<List<PersonDTO>> get(
            int pageIndex,
            int size,
            String sort,
            Sort.Direction direction,
            String term,
            List<Integer> countryIds,
            LocalDate fromBirthDate,
            LocalDate toBirthDate,
            LocalDate fromDeathDate,
            LocalDate toDeathDate,
            LocalDateTime fromCreationDate,
            LocalDateTime toCreationDate,
            LocalDateTime fromLastUpdate,
            LocalDateTime toLastUpdate
    ) {
        return
                producerRepository
                        .find(pageIndex, size, sort, direction, term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate)
                        .map(
                                actorList ->
                                        actorList
                                                .stream()
                                                .map(PersonDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getAll() {
        return
                producerRepository.listAll()
                        .map(directorList ->
                                directorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Long actorId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByProducer(actorId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Set<Movie>> addMovie(Long id, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                producerRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(producer -> producer.addMovie(movie))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                producerRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(producer -> producer.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Producer> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Producer.fromDTO(personDTO).persist());
    }

    public Uni<Producer> update(Long id, FileUpload file, PersonDTO personDTO) {
        // Validate personDTO for null or other basic validation
        if (Objects.isNull(personDTO)) {
            return Uni.createFrom().failure(new WebApplicationException("Invalid person data.", BAD_REQUEST));
        }

        return
                Panache.withTransaction(() ->
                        producerRepository.findById(id)
                                .onItem().ifNull().failWith(() -> {
                                    log.error("Person with ID {} not found in the database.", id);
                                    return new WebApplicationException("Person missing from database.", NOT_FOUND);
                                })
                                .call(person -> countryService.getByIds(personDTO.getCountries())
                                        .onFailure().invoke(error -> log.error("Failed to fetch countries for person {}: {}", id, error.getMessage()))
                                        .invoke(person::setCountries)
                                )
                                .invoke(entity -> {
                                    entity.setName(personDTO.getName());
                                    entity.setDateOfBirth(personDTO.getDateOfBirth());
                                    entity.setDateOfDeath(personDTO.getDateOfDeath());
                                    entity.setPhotoFileName(Optional.ofNullable(personDTO.getPhotoFileName()).orElse(DEFAULT_PHOTO));
                                }).call(entity -> {
                                    if (Objects.nonNull(file)) {
                                        return uploadPhoto(file)
                                                .onFailure().invoke(error -> log.error("Photo upload failed for person {}: {}", id, error.getMessage()))
                                                .invoke(entity::setPhotoFileName);
                                    }
                                    return Uni.createFrom().item(entity);
                                })
                );
    }

    public Uni<Boolean> delete(Long id) {
        return Panache.withTransaction(() -> producerRepository.deleteById(id));
    }
}
