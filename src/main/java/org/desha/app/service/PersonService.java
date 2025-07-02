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
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
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
    private final StatsService statsService;

    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;

    @Inject
    protected PersonService(
            CountryService countryService,
            FileService fileService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            PersonRepository personRepository,
            StatsService statsService
    ) {
        this.countryService = countryService;
        this.fileService = fileService;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.statsService = statsService;
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

    public Uni<Long> countMovieCountriesByPerson(Long id, String term, String lang) {
        return countryRepository.countMovieCountriesByPerson(id, term, lang);
    }

    public Uni<PersonDTO> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
                        .call(person -> Mutiny.fetch(person.getCountries()).invoke(person::setCountries))
                        .map(person -> PersonDTO.of(person, person.getCountries()))
                ;
    }

    public Uni<List<PersonDTO>> searchByName(String name) {
        return
                personRepository.findByName(name.trim())
                        .onItem().ifNotNull()
                        .transform(tList ->
                                tList.stream()
                                        .map(t -> PersonDTO.of(t, t.getCountries()))
                                        .toList()
                        )
                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
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
                                        .map(PersonDTO::of)
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
                                        .map(personWithMoviesNumber -> PersonDTO.of(personWithMoviesNumber.person(), personWithMoviesNumber.moviesNumber(), personWithMoviesNumber.awardsNumber()))
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
                                        .map(PersonDTO::of)
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
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::of)
                                                .toList()
                        )
                ;
    }

    public Uni<List<CountryDTO>> getMovieCountriesByPerson(Long id, Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findMovieCountriesByPerson(id, page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::of)
                                                .toList()
                        )
                ;
    }

    public Uni<Set<CeremonyAwardsDTO>> getAwardsByPerson(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Personne introuvable"))
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

    public Uni<Boolean> deletePerson(Long id) {
        return
                Panache.withTransaction(() ->
                                personRepository.deleteById(id)
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

    public Uni<Person> prepareAndPersistPerson(PersonDTO personDTO, PersonType type) {
        return
                personRepository.findById(personDTO.getId())
                        .invoke(person -> person.addType(type))
                        .call(personRepository::persist);
    }

    public List<MovieActorDTO> fromMovieActorListEntity(List<MovieActor> movieActorList) {
        return
                movieActorList
                        .stream()
                        .map(MovieActorDTO::of)
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

    public List<PersonDTO> fromPersonListEntity(List<Person> personList) {
        return
                personList
                        .stream()
                        .map(PersonDTO::of)
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
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des pays n'est pas initialisée"))
                        .map(CountryDTO::fromCountryEntitySet)
                ;
    }

}
