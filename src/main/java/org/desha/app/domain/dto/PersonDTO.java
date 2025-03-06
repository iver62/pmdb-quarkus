package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDTO {

    protected Long id;
    protected String name;
    protected String photoFileName;
    protected LocalDate dateOfBirth;
    protected LocalDate dateOfDeath;
    protected LocalDateTime creationDate;
    protected LocalDateTime lastUpdate;
    protected Set<MovieDTO> movies;
    protected Set<CountryDTO> countries;
    protected Set<Award> awards;

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
}
