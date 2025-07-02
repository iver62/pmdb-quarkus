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
import org.desha.app.domain.entity.MovieTechnician;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieActorRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.desha.app.utils.Messages;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@ApplicationScoped
@Slf4j
public class PersonService implements PersonServiceInterface {

    private static final String PHOTOS_DIR = "photos/";
    public static final String DEFAULT_PHOTO = "default-photo.jpg";

    private final CountryService countryService;
    private final FileService fileService;
    private final StatsService statsService;

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;

    @Inject
    protected PersonService(
            CountryService countryService,
            FileService fileService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            StatsService statsService
    ) {
        this.countryService = countryService;
        this.fileService = fileService;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.personRepository = personRepository;
        this.statsService = statsService;
    }

    public Uni<Long> countPersons(CriteriasDTO criteriasDTO) {
        return personRepository.countPersons(criteriasDTO);
    }

    public Uni<Long> countMovies(Long id, CriteriasDTO criteriasDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .chain(person -> movieRepository.countMoviesByPerson(person, criteriasDTO))
                ;
    }

    public Uni<Long> countRolesByPerson(Long id) {
        return movieActorRepository.countMovieActorsByActor(id);
    }

    public Uni<Long> countCountries(String term, String lang) {
        return countryRepository.countPersonCountries(term, lang);
    }

    public Uni<Long> countMovieCountriesByPerson(Long id, String term, String lang) {
        return countryRepository.countMovieCountriesByPerson(id, term, lang);
    }

    public Uni<PersonDTO> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .call(person -> Mutiny.fetch(person.getCountries()).invoke(person::setCountries))
                        .map(person -> PersonDTO.of(person, person.getCountries()))
                ;
    }

    public Uni<LightPersonDTO> getLightById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .map(LightPersonDTO::of)
                ;
    }

    public Uni<List<PersonDTO>> searchByName(String name) {
        return
                personRepository.findByName(name.trim())
                        .onItem().ifNotNull().transform(personList ->
                                personList.stream()
                                        .map(person -> PersonDTO.of(person, person.getCountries()))
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    public Uni<List<Person>> getByIds(List<Long> ids) {
        return personRepository.findByIds(ids);
    }

    public Uni<List<LightPersonDTO>> getLightPersons(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository
                        .findPersons(page, sort, direction, criteriasDTO)
                        .map(personList -> fromPersonListEntity(personList, LightPersonDTO::of))
                ;
    }

    public Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository
                        .findPersonsWithMoviesNumber(page, sort, direction, criteriasDTO)
                        .map(personList ->
                                personList
                                        .stream()
                                        .map(personWithMoviesNumber -> PersonDTO.of(personWithMoviesNumber.person(), personWithMoviesNumber.moviesNumber(), personWithMoviesNumber.awardsNumber()))
                                        .toList()
                        )
                ;
    }

    public Uni<List<MovieActorDTO>> getRoles(Long id, Page page, String sort, Sort.Direction direction) {
        return
                movieActorRepository
                        .findMovieActorsByActor(id, page, sort, direction)
                        .map(movieActorList ->
                                movieActorList.stream()
                                        .map(MovieActorDTO::fromMovie)
                                        .toList()
                        )
                ;
    }

    public Uni<List<PersonDTO>> getAll() {
        return
                personRepository
                        .listAll()
                        .map(personList -> fromPersonListEntity(personList, PersonDTO::of))
                ;
    }

    public Uni<List<MovieDTO>> getMovies(long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .chain(person ->
                                movieRepository
                                        .findMoviesByPerson(person, page, sort, direction, criteriasDTO)
                                        .map(movieWithAwardsNumberList ->
                                                movieWithAwardsNumberList
                                                        .stream()
                                                        .map(movieWithAwardsNumber ->
                                                                MovieDTO.of(
                                                                        movieWithAwardsNumber.movie(),
                                                                        movieWithAwardsNumber.awardsNumber()
                                                                )
                                                        )
                                                        .toList()
                                        )
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findPersonCountries(page, sort, direction, term, lang)
                        .map(countryService::fromCountryListEntity)
                ;
    }

    public Uni<List<CountryDTO>> getMovieCountriesByPerson(Long id, Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findMovieCountriesByPerson(id, page, sort, direction, term, lang)
                        .map(countryService::fromCountryListEntity)
                ;
    }

    public Uni<Set<CeremonyAwardsDTO>> getAwardsByPerson(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(
                                person -> Mutiny.fetch(person.getAwards())
                                        .map(AwardDTO::fromEntityList)
                                        .map(awardDTOList -> {
                                            Map<CeremonyAwardsDTO, List<AwardDTO>> grouped = awardDTOList.stream()
                                                    .collect(Collectors.groupingBy(AwardDTO::getCeremonyAwards));

                                            // Pour chaque groupe, créer un CeremonyAwardsDTO avec la liste des awards
                                            return grouped.entrySet().stream()
                                                    .map(entry -> {
                                                        CeremonyAwardsDTO ca = entry.getKey();
                                                        ca.setAwards(entry.getValue());
                                                        return ca;
                                                    })
                                                    .collect(Collectors.toSet());
                                        })
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
        return Panache.withTransaction(() -> personRepository.persist(Person.build(personDTO)).map(PersonDTO::of));
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
                                .map(person -> PersonDTO.of(person, person.getCountries()))
                );
    }

    public Uni<Set<CountryDTO>> updateCountries(Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .chain(person ->
                                                countryService.getByIds(
                                                                countryDTOSet.stream()
                                                                        .map(CountryDTO::getId)
                                                                        .filter(Objects::nonNull)
                                                                        .toList()
                                                        )
                                                        .invoke(finalCountrySet -> {
                                                            person.setCountries(new HashSet<>(finalCountrySet));
                                                            person.setLastUpdate(LocalDateTime.now());
                                                        })
                                                        .replaceWith(person)
                                        )
                                        .flatMap(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        );
    }

    public Uni<Set<CountryDTO>> addCountries(Long id, Set<CountryDTO> countryDTOSet) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .chain(countries ->
                                                                countryService.getByIds(countryDTOSet.stream().map(CountryDTO::getId).toList())
                                                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Un ou plusieurs pays sont introuvables"))
                                                                        .invoke(person::addCountries)
                                                        )
                                                        .replaceWith(person)
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .invoke(countries -> person.removeCountry(countryId))
                                                        .replaceWith(person)
                                        )
                                        .chain(personRepository::persist)
                                        .flatMap(this::fetchAndMapCountries)
                        )
                ;
    }

    public Uni<Boolean> deletePerson(Long id) {
        return
                Panache.withTransaction(() ->
                                personRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .flatMap(aBoolean -> statsService.updateActorsStats().replaceWith(aBoolean))
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression de la personne", throwable);
                        });
    }

    public Uni<Boolean> clearCountries(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .flatMap(person ->
                                                Mutiny.fetch(person.getCountries())
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                                                        .invoke(countries -> person.clearCountries())
                                                        .replaceWith(person)
                                        )
                                        .chain(personRepository::persist)
                                        .map(t -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des pays", throwable);
                        });
    }

    public Uni<Person> prepareAndPersistPerson(PersonDTO personDTO, PersonType type) {
        return
                personRepository.findById(personDTO.getId())
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .invoke(person -> person.addType(type))
                        .call(personRepository::persist);
    }

    public List<MovieActorDTO> fromMovieActorListEntity(List<MovieActor> movieActorList) {
        return
                movieActorList
                        .stream()
                        .map(MovieActorDTO::fromActor)
                        .sorted(MovieActorDTO::compareTo)
                        .toList()
                ;
    }

    public <T extends MovieTechnician> List<MovieTechnicianDTO> fromMovieTechnicianListEntity(Supplier<List<T>> getTechnicians) {
        return
                getTechnicians.get()
                        .stream()
                        .map(MovieTechnicianDTO::of)
                        .toList()
                ;
    }

    public <T> List<T> fromPersonListEntity(List<Person> personList, Function<Person, T> mapper) {
        return
                personList
                        .stream()
                        .map(mapper)
                        .toList()
                ;
    }

    public <T extends MovieTechnician> List<MovieTechnicianDTO> fromMovieTechnicianListEntity(List<T> movieTechnicians) {
        return
                movieTechnicians
                        .stream()
                        .map(MovieTechnicianDTO::of)
                        .toList()
                ;
    }

    private Uni<Set<CountryDTO>> fetchAndMapCountries(Person person) {
        return
                Mutiny.fetch(person.getCountries())
                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.COUNTRIES_NOT_INITIALIZED))
                        .map(CountryDTO::fromCountryEntitySet)
                ;
    }

}
