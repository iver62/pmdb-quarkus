package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
//@Dependent
//@ApplicationScoped
public abstract class PersonService<T extends Person> implements PersonServiceInterface<T> {

    private final CountryService countryService;
    protected final MovieRepository movieRepository;
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

    public Uni<Long> count(String term) {
        return personRepository.count(term);
    }

    @Override
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
        return personRepository.count(term);
    }

    public Uni<Long> countMovies(Long personId, Class<T> personType, String term) {
        return switch (personType.getSimpleName()) {
//            case "Actor" -> movieRepository.countMoviesByActor(personId, term);
//            case "Producer" -> movieRepository.countMoviesByProducer(personId, term);
//            case "Director" -> movieRepository.countMoviesByDirector(personId, term);
//            case "Screenwriter" -> movieRepository.countMoviesByScreenwriter(personId, term);
//            case "Musician" -> movieRepository.countMoviesByMusician(personId, term);
//            case "Decorator" -> movieRepository.countMoviesByDecorator(personId, term);
//            case "Costumier" -> movieRepository.countMoviesByCostumier(personId, term);
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
    public Uni<T> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNotNull().call(t ->
                                Mutiny.fetch(t.getCountries())
                                        .invoke(t::setCountries)
                        )
                        .onFailure().recoverWithNull()
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
                personRepository
                        .find(pageIndex, size, sort, direction, term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate)
                        .map(
                                tList ->
                                        tList
                                                .stream()
                                                .map(PersonDTO::fromEntity)
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
    public Uni<List<MovieDTO>> getMovies(Long personId, Class<T> personType, int page, int size, String sort, Sort.Direction direction, String term) {
        return switch (personType.getSimpleName()) {
            /*case "Actor" -> movieRepository
                    .findMoviesByActor(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Producer" -> movieRepository
                    .findMoviesByProducer(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Director" -> movieRepository
                    .findMoviesByDirector(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Screenwriter" -> movieRepository
                    .findMoviesByScreenwriter(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;*/
            /*case "Musician" -> movieRepository
                    .findMoviesByMusician(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;*/
            /*case "Decorator" -> movieRepository
                    .findMoviesByDecorator(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;*/
            /*case "Costumier" -> movieRepository
                    .findMoviesByCostumier(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;*/
            case "Photographer" -> movieRepository
                    .findMoviesByPhotographer(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Editor" -> movieRepository
                    .findMoviesByEditor(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Caster" -> movieRepository
                    .findMoviesByCaster(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "ArtDirector" -> movieRepository
                    .findMoviesByArtDirector(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "SoundEditor" -> movieRepository
                    .findMoviesBySoundEditor(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "VisualEffectsSupervisor" -> movieRepository
                    .findMoviesByVisualEffectsSupervisor(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "MakeupArtist" -> movieRepository
                    .findMoviesByMakeupArtist(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "HairDresser" -> movieRepository
                    .findMoviesByHairDresser(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            case "Stuntman" -> movieRepository
                    .findMoviesByStuntman(personId, page, size, sort, direction, term)
                    .map(movieList ->
                            movieList
                                    .stream()
                                    .map(MovieDTO::fromEntity)
                                    .toList()
                    )
            ;
            default -> Uni.createFrom().failure(new IllegalArgumentException("Type inconnu"));
        };
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

    public abstract Uni<T> save(PersonDTO personDTO);

    /*public Uni<T> save(PersonDTO personDTO) {
        return
                Panache
                        .withTransaction(() -> T.fromDTO(personDTO).persist()
        *//*{
                                    instance.setName(StringUtils.trim(personDTO.getName()));
                                    instance.setPhotoFileName(DEFAULT_PHOTO);
                                    return instance.persist();
                                }*//*
                        )
                ;
    }*/

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
