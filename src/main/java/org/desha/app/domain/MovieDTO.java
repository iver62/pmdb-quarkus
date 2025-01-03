package org.desha.app.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class MovieDTO {

    private String title;
    private String originalTitle;
    private String synopsis;
    private LocalDate releaseDate;
    private Long runningTime;
    private Long budget;
    private Long boxOffice;
    private String posterPath;
    private TechnicalSummaryDTO technicalSummary;
    private Set<Role> roles;
    private Set<Country> countries;
    private Set<Genre> genres;
    private Set<Award> awards;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

}
