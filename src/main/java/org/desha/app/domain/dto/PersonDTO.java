package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private long numberOfMovies;
    private Set<MovieDTO> movies;
    private Set<CountryDTO> countries;
    private Set<Award> awards;

    public static PersonDTO fromEntity(Person person) {
        return PersonDTO.builder()
                .id(person.id)
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static PersonDTO fromEntity(Person person, long nbMovies) {
        return PersonDTO.builder()
                .id(person.id)
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .numberOfMovies(nbMovies)
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }

    public static PersonDTO fromEntityWithCountriesAndMovies(Person person) {
        return PersonDTO.builder()
                .id(person.id)
                .name(person.getName())
                .dateOfBirth(person.getDateOfBirth())
                .dateOfDeath(person.getDateOfDeath())
                .photoFileName(person.getPhotoFileName())
                .countries(person.getCountries().stream().map(CountryDTO::fromEntity).collect(Collectors.toSet()))
                .movies(person.getMovies().stream().map(MovieDTO::fromEntity).collect(Collectors.toSet()))
                .creationDate(person.getCreationDate())
                .lastUpdate(person.getLastUpdate())
                .build();
    }
}
