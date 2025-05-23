package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDTO {

    private Long id;
    private String name;
    private String photoFileName;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private Long numberOfMovies;
    private Long numberOfAwards;
    private Set<PersonType> types;
    private Set<MovieDTO> movies;
    private Set<CountryDTO> countries;
    private Set<Award> awards;

    public static PersonDTO fromEntity(Person person) {
        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .types(person.getTypes())
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static PersonDTO fromEntity(Person person, long nbMovies, long nbAwards) {
        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .numberOfMovies(nbMovies)
                .numberOfAwards(nbAwards)
                .types(person.getTypes())
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static PersonDTO fromEntity(Person person, Set<Country> countries) {
        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .types(person.getTypes())
                .countries(countries.stream().map(CountryDTO::fromEntity).collect(Collectors.toSet()))
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static PersonDTO fromEntity(Person person, List<Movie> movies, Set<Country> countries) {
        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .types(person.getTypes())
                .countries(countries.stream().map(CountryDTO::fromEntity).collect(Collectors.toSet()))
                .movies(
                        movies
                                .stream()
                                .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
                                .collect(Collectors.toSet())
                )
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static Set<PersonDTO> fromEntitySet(Set<Person> personSet) {
        return
                Optional.ofNullable(personSet).orElse(Set.of())
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
