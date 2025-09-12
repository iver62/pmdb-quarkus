package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.exception.PhotoDeletionException;
import org.desha.app.mapper.*;
import org.desha.app.repository.*;
import org.desha.app.utils.Messages;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@ApplicationScoped
@Slf4j
public class PersonService implements PersonServiceInterface {

    private static final String PHOTOS_DIR = "photos/";

    private final AwardMapper awardMapper;
    private final CategoryMapper categoryMapper;
    private final CountryMapper countryMapper;
    private final MovieMapper movieMapper;
    private final MovieActorMapper movieActorMapper;
    private final PersonMapper personMapper;

    private final CountryService countryService;
    private final FileService fileService;
    private final StatsService statsService;

    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final PersonRepository personRepository;

    @Inject
    protected PersonService(
            AwardMapper awardMapper,
            CategoryMapper categoryMapper,
            CountryMapper countryMapper,
            MovieMapper movieMapper,
            MovieActorMapper movieActorMapper,
            PersonMapper personMapper,
            CountryService countryService,
            FileService fileService,
            CategoryRepository categoryRepository,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            MovieActorRepository movieActorRepository,
            PersonRepository personRepository,
            StatsService statsService
    ) {
        this.awardMapper = awardMapper;
        this.categoryMapper = categoryMapper;
        this.countryMapper = countryMapper;
        this.movieMapper = movieMapper;
        this.movieActorMapper = movieActorMapper;
        this.personMapper = personMapper;
        this.countryService = countryService;
        this.fileService = fileService;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
        this.movieActorRepository = movieActorRepository;
        this.personRepository = personRepository;
        this.statsService = statsService;
    }

    public Uni<Long> countPersons(CriteriaDTO criteriaDTO) {
        return personRepository.countPersons(criteriaDTO);
    }

    public Uni<Long> countMovies(Long id, CriteriaDTO criteriaDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .chain(person -> movieRepository.countMoviesByPerson(person, criteriaDTO))
                ;
    }

    public Uni<Long> countRolesByPerson(Long id) {
        return movieActorRepository.countMovieActorsByActor(id);
    }

    public Uni<Long> countCountries(String term, String lang) {
        return countryRepository.countPersonCountries(term, lang);
    }

    public Uni<Long> countMovieCountriesByPerson(Long id, String term, String lang) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(person -> countryRepository.countMovieCountriesByPerson(person, term, lang))
                ;
    }

    public Uni<Long> countMovieCategoriesByPerson(Long id, String term) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(person -> categoryRepository.countMovieCategoriesByPerson(person, term))
                ;
    }

    public Uni<PersonDTO> getById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .call(person -> Mutiny.fetch(person.getCountries()).invoke(person::setCountries))
                        .map(personMapper::personToPersonDTO)
                ;
    }

    public Uni<LitePersonDTO> getLightById(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .map(personMapper::personToLitePersonDTO)
                ;
    }

    public Uni<List<Person>> getByIds(List<Long> ids) {
        return personRepository.findByIds(ids);
    }

    public Uni<List<LitePersonDTO>> getLightPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository
                        .findPersons(page, sort, direction, criteriaDTO)
                        .map(personMapper::toLiteDTOList)
                ;
    }

    public Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository
                        .findPersonsWithMoviesNumber(page, sort, direction, criteriaDTO)
                        .map(personMapper::toDTOWithNumbersList)
                ;
    }

    public Uni<List<MovieActorDTO>> getRoles(Long id, Page page, String sort, Sort.Direction direction) {
        return
                movieActorRepository.findMovieActorsByActor(id, page, sort, direction)
                        .map(movieActorMapper::toDTOListWithoutPerson)
                ;
    }

    public Uni<List<PersonDTO>> getAll() {
        return personRepository.listAll().map(personMapper::toDTOList);
    }

    public Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .chain(person ->
                                movieRepository
                                        .findMoviesByPerson(person, page, sort, direction, criteriaDTO)
                                        .map(movieMapper::toDTOWithAwardsNumberList)
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findPersonCountries(page, sort, direction, term, lang)
                        .map(countryMapper::toDTOList)
                ;
    }

    public Uni<List<CountryDTO>> getMovieCountriesByPerson(Long id, Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(person ->
                                countryRepository.findMovieCountriesByPerson(person, page, sort, direction, term, lang)
                                        .map(countryMapper::toDTOList)
                        )
                ;
    }

    public Uni<List<CategoryDTO>> getMovieCategoriesByPerson(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(person ->
                                categoryRepository.findMovieCategoriesByPerson(person, page, sort, direction, term)
                                        .map(categoryMapper::toDTOList)
                        )
                ;
    }

    public Uni<Set<CeremonyAwardsDTO>> getAwardsByPerson(Long id) {
        return
                personRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                        .flatMap(
                                person -> Mutiny.fetch(person.getAwards())
                                        .map(awardMapper::toDTOList)
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

    public Uni<File> getPhoto(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank()) {
            log.warn("Photo name is missing, returning default photo.");
            return fileService.getFile(PHOTOS_DIR, Person.DEFAULT_PHOTO);
        }

        return
                fileService.getFile(PHOTOS_DIR, fileName)
                        .onFailure(FileNotFoundException.class).recoverWithUni(() -> {
                                    log.warn("Photo {} not found, returning default photo.", fileName);
                                    return fileService.getFile(PHOTOS_DIR, Person.DEFAULT_PHOTO);
                                }
                        )
                ;
    }

    private Uni<String> uploadPhoto(FileUpload file) {
        if (Objects.isNull(file) || Objects.isNull(file.uploadedFile()) || file.fileName().isBlank()) {
            log.warn("Invalid or missing file. Using default photo.");
            return Uni.createFrom().item(Person.DEFAULT_PHOTO);
        }

        return
                fileService.uploadFile(PHOTOS_DIR, file)
                        .onFailure().recoverWithItem(error -> {
                                    log.error("Photo upload failed: {}", error.getMessage());
                                    return Person.DEFAULT_PHOTO;
                                }
                        )
                ;
    }

    public Uni<PersonDTO> save(PersonDTO personDTO) {
        return
                Panache.withTransaction(() ->
                        personRepository.persist(
                                        Person.build(
                                                personDTO.getName(),
                                                personDTO.getPhotoFileName(),
                                                personDTO.getDateOfBirth(),
                                                personDTO.getDateOfDeath(),
                                                personDTO.getTypes(),
                                                personDTO.getCreationDate(),
                                                personDTO.getLastUpdate()
                                        )
                                )
                                .map(personMapper::personToPersonDTO)
                );
    }

    public Uni<PersonDTO> update(Long id, FileUpload file, PersonDTO personDTO) {
        // Validate personDTO for null or other basic validation
        if (Objects.isNull(personDTO)) {
            return Uni.createFrom().failure(new WebApplicationException("Invalid person data.", BAD_REQUEST));
        }

        return
                Panache.withTransaction(() ->
                        personRepository.findById(id)
                                .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                .call(person -> countryService.getByIds(personDTO.getCountries())
                                        .onFailure().invoke(error -> log.error("Failed to fetch countries for person {}: {}", id, error.getMessage()))
                                        .invoke(person::setCountries)
                                )
                                .invoke(person -> person.updatePerson(personDTO))
                                .call(person -> {
                                    final String currentPhoto = person.getPhotoFileName();
                                    final String dtoPhoto = personDTO.getPhotoFileName();

                                    if (Objects.nonNull(file)) {
                                        return uploadPhoto(file)
                                                .onFailure().invoke(error -> log.error("Photo upload failed for person {}: {}", id, error.getMessage()))
                                                .chain(uploadedFileName ->
                                                        deletePhotoIfExists(currentPhoto) // On supprime l'ancien fichier si ce n'est pas le fichier par défaut
                                                                .replaceWith(uploadedFileName)
                                                )
                                                .invoke(person::setPhotoFileName);
                                    } else if (!Objects.equals(currentPhoto, dtoPhoto)) {
                                        // Pas de nouveau fichier, mais différence → on remet la photo par défaut
                                        return
                                                deletePhotoIfExists(currentPhoto)
                                                        .invoke(() -> person.setPhotoFileName(Person.DEFAULT_PHOTO))
                                                ;
                                    }
                                    // Aucun changement de photo
                                    return Uni.createFrom().item(person);
                                })
                                .map(personMapper::personToPersonDTO)
                );
    }

    public Uni<Void> deletePhotoIfExists(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank() || Objects.equals(fileName, Person.DEFAULT_PHOTO)) {
            return Uni.createFrom().voidItem();
        }

        return Uni.createFrom().item(() -> {
            try {
                fileService.deleteFile(PHOTOS_DIR, fileName);
                return null;
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de la photo {}: {}", fileName, e.getMessage());
                throw new PhotoDeletionException("Erreur lors de la suppression de la photo " + fileName);
            }
        });
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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
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
                                personRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.PERSON_NOT_FOUND))
                                        .flatMap(person -> {
                                                    final String photoFileName = person.getPhotoFileName();
                                                    return
                                                            personRepository.delete(person).replaceWith(true)
                                                                    .call(aBoolean -> statsService.updateActorsStats())
                                                                    .call(aBoolean -> deletePhotoIfExists(photoFileName))
                                                            ;
                                                }
                                        )
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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
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

    public Uni<Person> prepareAndPersistPerson(LitePersonDTO litePersonDTO, PersonType type) {
        return
                personRepository.findById(litePersonDTO.getId())
                        .onItem().ifNull().switchTo(() ->
                                personRepository.persist(
                                        Person.build(
                                                litePersonDTO.getName(),
                                                litePersonDTO.getPhotoFileName()
                                        )
                                )
                        )
                        .invoke(person -> person.addType(type))
                ;
    }

    private Uni<Set<CountryDTO>> fetchAndMapCountries(Person person) {
        return
                Mutiny.fetch(person.getCountries())
                        .onItem().ifNull().failWith(() -> new IllegalStateException(Messages.NULL_COUNTRIES))
                        .map(countryMapper::toDTOSet)
                ;
    }

}
