package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
public abstract class PersonService<T extends Person> implements PersonServiceInterface<T> {

    private final CountryService countryService;
    protected final CountryRepository countryRepository;
    protected final MovieRepository movieRepository;
    private final PersonRepository<T> personRepository;
    private final FileService fileService;

    private static final String PHOTOS_DIR = "photos/";
    public static final String DEFAULT_PHOTO = "default-photo.jpg";

    @Inject
    protected PersonService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            PersonRepository<T> personRepository,
            FileService fileService
    ) {
        this.countryService = countryService;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.fileService = fileService;
    }

    public Uni<Long> count(String term) {
        return personRepository.count(term);
    }

    @Override
    public Uni<Long> count(CriteriasDTO criteriasDTO) {
        return personRepository.count(criteriasDTO);
    }

    @Override
    public Uni<T> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNotNull()
                        .call(t -> Mutiny.fetch(t.getCountries()).invoke(t::setCountries))
                        .onFailure().recoverWithNull()
                ;
    }

    @Override
    public Uni<List<PersonDTO>> searchByName(String name) {
        return
                personRepository.findByName(name.trim())
                        .onItem().ifNotNull()
                        .transform(tList ->
                                tList.stream()
                                        .map(t -> PersonDTO.fromEntity(t, t.getCountries()))
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<PersonDTO> getByIdWithCountriesAndMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository.findByIdWithMovies(id, page, sort, direction, criteriasDTO)
                        .call(t -> Mutiny.fetch(t.getCountries()).invoke(t::setCountries))
                        .map(t -> PersonDTO.fromEntity(t, t.getMovies(), t.getCountries()))
                ;
    }

    public Uni<Set<T>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    @Override
    public Uni<List<T>> getByIds(List<Long> ids) {
        return personRepository.findByIds(ids);
    }

    @Override
    public Uni<List<PersonDTO>> get(
            Page page,
            String sort,
            Sort.Direction direction,
            CriteriasDTO criteriasDTO
    ) {
        return
                personRepository
                        .find(page, sort, direction, criteriasDTO)
                        .map(
                                tList ->
                                        tList
                                                .stream()
                                                .map(t -> PersonDTO.fromEntity(t, t.getMovies().size()))
                                                .toList()
                        )
                ;
    }

    @Override
    public Uni<List<PersonDTO>> getAll() {
        return personRepository.listAll()
                .map(tList ->
                        tList
                                .stream()
                                .map(PersonDTO::fromEntity)
                                .toList()
                );
    }

    @Override
    public Uni<List<Movie>> addMovie(Long id, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }

    @Override
    public Uni<List<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
                        )
                ;
    }

    public Uni<File> getPhoto(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank()) {
            log.warn("Photo name is missing, returning default photo.");
            return fileService.getFile(PHOTOS_DIR, DEFAULT_PHOTO);
        }

        return fileService.getFile(PHOTOS_DIR, fileName)
                .onFailure(FileNotFoundException.class).recoverWithUni(() -> {
                    log.warn("Photo {} not found, returning default photo.", fileName);
                    return fileService.getFile(PHOTOS_DIR, DEFAULT_PHOTO);
                });
    }

    private Uni<String> uploadPhoto(FileUpload file) {
        if (Objects.isNull(file) || Objects.isNull(file.uploadedFile()) || file.fileName().isBlank()) {
            log.warn("Invalid or missing file. Using default photo.");
            return Uni.createFrom().item(DEFAULT_PHOTO);
        }

        return fileService.uploadFile(PHOTOS_DIR, file)
                .onFailure().recoverWithItem(error -> {
                    log.error("Photo upload failed: {}", error.getMessage());
                    return DEFAULT_PHOTO;
                });
    }

    public Uni<T> update(Long id, FileUpload file, PersonDTO personDTO) {
        // Validate personDTO for null or other basic validation
        if (Objects.isNull(personDTO)) {
            return Uni.createFrom().failure(new WebApplicationException("Invalid person data.", BAD_REQUEST));
        }

        return
                Panache.withTransaction(() ->
                        personRepository.findById(id)
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
        return Panache.withTransaction(() -> personRepository.deleteById(id));
    }

    protected Set<PersonDTO> fromPersonSetEntity(Set<T> personSet) {
        return
                personSet
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }

}
