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
import org.desha.app.domain.entity.Director;
import org.desha.app.domain.entity.Movie;
import org.desha.app.repository.DirectorRepository;
import org.desha.app.repository.MovieRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
@ApplicationScoped
public class DirectorService extends PersonServiceImpl {

    private final CountryService countryService;
    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;

    @Inject
    public DirectorService(
            CountryService countryService,
            MovieRepository movieRepository,
            DirectorRepository directorRepository,
            FileService fileService
    ) {
        super(fileService);
        this.countryService = countryService;
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
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
        return directorRepository.count(term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate);
    }

    public Uni<Long> countMovies(Long directorId, String term) {
        return movieRepository.countMoviesByDirector(directorId, term);
    }

    public Uni<Director> getById(Long id) {
        return
                directorRepository.findById(id)
                        .onItem().ifNotNull().call(t ->
                                Mutiny.fetch(t.getCountries())
                                        .invoke(t::setCountries)
                        )
                        .onFailure().recoverWithNull()
                ;
    }

    public Uni<Set<Director>> getByIds(Set<PersonDTO> persons) {
        return
                directorRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<List<Director>> getByIds(List<Long> ids) {
        return directorRepository.findByIds(ids);
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
                directorRepository
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
                directorRepository.listAll()
                        .map(directorList ->
                                directorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(Long directorId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByDirector(directorId, page, size, sort, direction, term)
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
                                directorRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(director -> director.addMovie(movie))
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                directorRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(director -> director.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Director> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Director.fromDTO(personDTO).persist());
    }

    public Uni<Director> update(Long id, FileUpload file, PersonDTO personDTO) {
        // Validate personDTO for null or other basic validation
        if (Objects.isNull(personDTO)) {
            return Uni.createFrom().failure(new WebApplicationException("Invalid person data.", BAD_REQUEST));
        }

        return
                Panache.withTransaction(() ->
                        directorRepository.findById(id)
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
        return Panache.withTransaction(() -> directorRepository.deleteById(id));
    }
}
