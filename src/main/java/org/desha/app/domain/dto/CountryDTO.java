package org.desha.app.domain.dto;

import lombok.Getter;
import org.desha.app.domain.entity.Movie;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class CountryDTO {

    private Long id;
    private int code;
    private String alpha2;
    private String alpha3;
    private String nomEnGb;
    private String nomFrFr;
    private LocalDateTime lastUpdate;
    private Set<PersonDTO> persons;
    private Set<Movie> movies;

}
