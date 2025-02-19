package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
@Dependent
public class PersonService<T extends Person> implements PersonServiceInterface<T> {

    private final CountryService countryService;
    private final MovieRepository movieRepository;
    private final PersonRepository<T> personRepository;
    private final FileService fileService;

    private static final String PHOTOS_DIR = "photos/";
    private static final String DEFAULT_PHOTO = "default-photo.jpg";

    @Inject
    public PersonService(
            CountryService countryService,
            MovieRepository movieRepository,
            PersonRepository<T> personRepository,
            FileService fileService
    ) {
        this.countryService = countryService;
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.fileService = fileService;
    }

    @Override
    public Uni<Long> count(String name) {
        return personRepository.count(name);
    }

    public Uni<Long> countMovies(Long personId, Class<T> personType, String term) {
        return switch (personType.getSimpleName()) {
            case "Actor" -> movieRepository.countMoviesByActor(personId, term);
            case "Producer" -> movieRepository.countMoviesByProducer(personId, term);
            case "Director" -> movieRepository.countMoviesByDirector(personId, term);
            case "Screenwriter" -> movieRepository.countMoviesByScreenwriter(personId, term);
            case "Musician" -> movieRepository.countMoviesByMusician(personId, term);
            case "Decorator" -> movieRepository.countMoviesByDecorator(personId, term);
            case "Costumier" -> movieRepository.countMoviesByCostumier(personId, term);
            case "Photographer" -> movieRepository.countMoviesByPhotographer(personId, term);
            case "Editor" -> movieRepository.countMoviesByEditor(personId, term);
            case "Caster" -> movieRepository.countMoviesByCaster(personId, term);
            case "ArtDirector" -> movieRepository.countMoviesByArtDirector(personId, term);
            case "SoundEditor" -> movieRepository.countMoviesBySoundEditor(personId, term);
            case "VisualEffectsSupervisor" -> movieRepository.countMoviesByVisualEffectsSupervisor(personId, term);
            case "MakeupArtist" -> movieRepository.countMoviesByMakeupArtist(personId, term);
            case "HairDresser" -> movieRepository.countMoviesByHairDresser(personId, term);
            case "Stuntman" -> movieRepository.countMoviesByStuntman(personId, term);
            default -> Uni.createFrom().failure(new IllegalArgumentException("Type inconnu"));
        };
    }

    @Override
    public Uni<T> getOne(Long id) {
        return personRepository.findById(id);
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
    public Uni<List<T>> get(int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return personRepository.find(pageIndex, size, sort, direction, name);
    }

    @Override
    public Uni<List<T>> getAll() {
        return personRepository.listAll();
    }

    @Override
    public Uni<List<Movie>> getMovies(Long personId, Class<T> personType, int page, int size, String sort, Sort.Direction direction, String term) {
        return switch (personType.getSimpleName()) {
            case "Actor" -> movieRepository.findMoviesByActor(personId, page, size, sort, direction, term);
            case "Producer" -> movieRepository.findMoviesByProducer(personId, page, size, sort, direction, term);
            case "Director" -> movieRepository.findMoviesByDirector(personId, page, size, sort, direction, term);
            case "Screenwriter" ->
                    movieRepository.findMoviesByScreenwriter(personId, page, size, sort, direction, term);
            case "Musician" -> movieRepository.findMoviesByMusician(personId, page, size, sort, direction, term);
            case "Decorator" -> movieRepository.findMoviesByDecorator(personId, page, size, sort, direction, term);
            case "Costumier" -> movieRepository.findMoviesByCostumier(personId, page, size, sort, direction, term);
            case "Photographer" ->
                    movieRepository.findMoviesByPhotographer(personId, page, size, sort, direction, term);
            case "Editor" -> movieRepository.findMoviesByEditor(personId, page, size, sort, direction, term);
            case "Caster" -> movieRepository.findMoviesByCaster(personId, page, size, sort, direction, term);
            case "ArtDirector" -> movieRepository.findMoviesByArtDirector(personId, page, size, sort, direction, term);
            case "SoundEditor" -> movieRepository.findMoviesBySoundEditor(personId, page, size, sort, direction, term);
            case "VisualEffectsSupervisor" ->
                    movieRepository.findMoviesByVisualEffectsSupervisor(personId, page, size, sort, direction, term);
            case "MakeupArtist" ->
                    movieRepository.findMoviesByMakeupArtist(personId, page, size, sort, direction, term);
            case "HairDresser" -> movieRepository.findMoviesByHairDresser(personId, page, size, sort, direction, term);
            case "Stuntman" -> movieRepository.findMoviesByStuntman(personId, page, size, sort, direction, term);
            default -> Uni.createFrom().failure(new IllegalArgumentException("Type inconnu"));
        };
    }

    @Override
    public Uni<Set<Country>> getCountries(Long id) {
        return personRepository.findById(id).flatMap(t -> Mutiny.fetch(t.getCountries()));
    }

    @Override
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

    @Override
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
                                    instance.setName(StringUtils.trim(personDTO.getName()));
                                    instance.setPhotoFileName(DEFAULT_PHOTO);
                                    return instance.persist();
                                }
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
}
