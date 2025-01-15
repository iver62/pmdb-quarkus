package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
public abstract class PersonDTO {

    private Long id;
    private String name;
    private String photoPath;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private Set<Movie> movies;
    private Set<Country> countries;
    private Set<Award> awards;

}
