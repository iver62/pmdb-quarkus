package org.desha.app.domain.dto;

import lombok.Getter;
import org.desha.app.domain.entity.Award;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
public abstract class PersonDTO {

    private String name;
    private String photoPath;
    private LocalDate dateOfBirth;
    private LocalDate dateOfDeath;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private Set<Award> awards;

}
