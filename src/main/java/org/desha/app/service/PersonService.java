package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.MovieActor;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Slf4j
@ApplicationScoped
public class PersonService implements PersonServiceInterface {

    private static final String PHOTOS_DIR = "photos/";
    public static final String DEFAULT_PHOTO = "default-photo.jpg";

    private final CountryService countryService;
    private final FileService fileService;

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;


    @Inject
    protected PersonService(
            CountryService countryService,
            FileService fileService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            PersonRepository personRepository
    ) {
        this.countryService = countryService;
        this.fileService = fileService;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
    }

    public Uni<Long> countAll() {
        return personRepository.countAll();
    }

    public Uni<Long> countPersons(CriteriasDTO criteriasDTO) {
        return personRepository.countPersons(criteriasDTO);
    }

    public Uni<Long> countMovies(long id, CriteriasDTO criteriasDTO) {
        return personRepository.findById(id)
                .chain(person -> movieRepository.countMoviesByPerson(person, criteriasDTO));
    }

    public Uni<Long> countCountries(String term, String lang) {
        return countryRepository.countPersonCountries(term, lang);
    }

    public Uni<PersonDTO> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                        .call(person -> Mutiny.fetch(person.getCountries()).invoke(person::setCountries))
                        .map(person -> PersonDTO.fromEntity(person, person.getCountries()))
                ;
    }

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

    /*public Uni<PersonDTO> getByIdWithCountriesAndMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository.findByIdWithMovies(id, page, sort, direction, criteriasDTO)
                        .call(t -> Mutiny.fetch(t.getCountries()).invoke(t::setCountries))
                        .map(t -> PersonDTO.fromEntity(t, t.getMovies(), t.getCountries()))
                ;
    }*/

    public Uni<Set<Person>> getByIds(Set<PersonDTO> persons) {
        return
                personRepository.findByIds(
                        Optional.ofNullable(persons).orElse(Collections.emptySet())
                                .stream()
                                .map(PersonDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<List<Person>> getByIds(List<Long> ids) {
        return personRepository.findByIds(ids);
    }

    public Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository
                        .findPersons(page, sort, direction, criteriasDTO)
                        .map(personList ->
                                personList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getPersonsWithMovieNumbers(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository
                        .findPersonsWithMoviesNumber(page, sort, direction, criteriasDTO)
                        .map(personList ->
                                personList
                                        .stream()
                                        .map(personWithMoviesNumber -> PersonDTO.fromEntity(personWithMoviesNumber.person(), personWithMoviesNumber.moviesNumber(), personWithMoviesNumber.awardsNumber()))
                                        .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getAll() {
        return
                personRepository.listAll()
                        .map(tList ->
                                tList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieDTO>> getMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                        .chain(person ->
                                movieRepository
                                        .findMoviesByPerson(person, page, sort, direction, criteriasDTO)
                                        .map(movieList ->
                                                movieList
                                                        .stream()
                                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards().size()))
                                                        .toList()
                                        )
                        )

                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findPersonCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Set<AwardDTO>> getAwardsByPerson(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                        .flatMap(
                                person -> Mutiny.fetch(person.getAwardSet())
                                        .map(AwardDTO::fromEntitySet)
                        )
                ;
    }

    /*@Override
    public Uni<List<Movie>> addMovie(Long id, Movie movie) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.addMovie(movie))
                        )
                ;
    }*/

   /* @Override
    public Uni<List<Movie>> removeMovie(Long id, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNotNull()
                                        .transformToUni(person -> person.removeMovie(movieId))
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

    public Uni<PersonDTO> save(PersonDTO personDTO) {
        log.info("Saving person: {}, with types: {}", personDTO.getName(), personDTO.getTypes());
        return Panache.withTransaction(() -> personRepository.persist(Person.build(personDTO)).map(PersonDTO::fromEntity));
    }

    public Uni<PersonDTO> update(Long id, FileUpload file, PersonDTO personDTO) {
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
                                .map(person -> PersonDTO.fromEntity(person, person.getCountries()))
                );
    }

    public Uni<Set<CountryDTO>> saveCountries(Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                                        .chain(t ->
                                                countryService.getByIds(
                                                                countryDTOSet.stream()
                                                                        .map(CountryDTO::getId)
                                                                        .filter(Objects::nonNull)
                                                                        .toList()
                                                        )
                                                        .invoke(finalCountrySet -> {
                                                            t.setCountries(new HashSet<>(finalCountrySet));
                                                            t.setLastUpdate(LocalDateTime.now());
                                                        })
                                                        .replaceWith(t)
                                        )
                                        .flatMap(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        );
    }

    public Uni<PersonDTO> addPersonType(Long id, PersonType personType) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable introuvable"))
                                        .flatMap(person -> person.addType(personType).replaceWith(person))
                                        .chain(personRepository::persist)
                                        .map(PersonDTO::fromEntity)
                        )
                ;
    }

    public Uni<Set<CountryDTO>> addCountries(Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable introuvable"))
                                        .flatMap(t ->
                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                        .call(t::addCountries)
                                                        .replaceWith(t)
                                        )
                                        .chain(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                ;
    }

    public Uni<Set<CountryDTO>> removeCountry(Long personId, Long countryId) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(personId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                                        .call(t -> t.removeCountry(countryId))
                                        .chain(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                ;
    }

    public Uni<Boolean> delete(Long id) {
        return
                Panache
                        .withTransaction(() -> personRepository.deleteById(id)
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                        )
                ;
    }

    public Uni<Boolean> clearCountries(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                                        .call(Person::clearCountries)
                                        .chain(personRepository::persist)
                                        .map(t -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    public List<MovieActorDTO> fromMovieActorListEntity(List<MovieActor> movieActorSet) {
        return
                movieActorSet
                        .stream()
                        .map(MovieActorDTO::fromEntity)
                        .sorted(MovieActorDTO::compareTo)
                        .toList()
                ;
    }

    public List<PersonDTO> fromPersonListEntity(List<Person> personList) {
        return
                personList
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .toList()
                ;
    }

    public Set<PersonDTO> fromPersonSetEntity(Set<Person> personSet) {
        return
                personSet
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }

    private Uni<Set<CountryDTO>> fetchAndMapCountries(Person person) {
        return
                Mutiny.fetch(person.getCountries())
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des pays n'est pas initialisée"))
                        .map(CountryDTO::fromCountryEntitySet)
                ;
    }

}
