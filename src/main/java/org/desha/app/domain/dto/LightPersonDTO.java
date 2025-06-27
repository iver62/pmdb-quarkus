package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Person;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LightPersonDTO {

    private Long id;
    private String name;
    private String photoFileName;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;

    public static LightPersonDTO of(Person person) {
        return
                LightPersonDTO.builder()
                        .id(person.getId())
                        .name(person.getName())
                        .dateOfBirth(person.getDateOfBirth())
                        .dateOfDeath(person.getDateOfDeath())
                        .photoFileName(person.getPhotoFileName())
                        .build();
    }

    public static Set<LightPersonDTO> fromEntitySet(Set<Person> personSet) {
        return
                Optional.ofNullable(personSet).orElse(Set.of())
                        .stream()
                        .map(LightPersonDTO::of)
                        .collect(Collectors.toSet())
                ;
    }

}
