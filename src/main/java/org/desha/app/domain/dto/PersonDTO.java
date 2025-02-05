package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
    private Set<MovieDTO> movies;
    private Set<CountryDTO> countries;
    private Set<Award> awards;

    public static PersonDTO fromEntity(Person person) {
        return PersonDTO.builder()
                .id(person.id)
                .name(person.getName())
                .photoFileName(person.getPhotoFileName())
                .build();
    }
}
