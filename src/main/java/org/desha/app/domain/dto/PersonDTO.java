package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.enums.PersonType;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDTO extends LitePersonDTO {

    private Long numberOfMovies;
    private Long numberOfAwards;
    private Set<PersonType> types;
    private Set<MovieDTO> movies;
    private Set<CountryDTO> countries;
    private Set<Award> awards;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

}
