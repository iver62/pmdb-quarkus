package org.desha.app.domain.dto;

import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record PersonDTO(Long id, String name, String photoPath, LocalDate dateOfBirth, LocalDate dateOfDeath,
                        LocalDateTime creationDate, LocalDateTime lastUpdate, Set<Movie> movies, Set<Country> countries,
                        Set<Award> awards) {
}
