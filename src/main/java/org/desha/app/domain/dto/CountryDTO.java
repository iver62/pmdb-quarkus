package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Person;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountryDTO {

    private Long id;
    private int code;
    private String alpha2;
    private String alpha3;
    private String nomEnGb;
    private String nomFrFr;
    private LocalDateTime lastUpdate;
    private Set<MovieDTO> movies;
    private Set<PersonDTO> persons;

    public static CountryDTO fromEntity(Country country) {
        return
                CountryDTO.builder()
                        .id(country.getId())
                        .code(country.getCode())
                        .alpha2(country.getAlpha2())
                        .alpha3(country.getAlpha3())
                        .nomFrFr(country.getNomFrFr())
                        .nomEnGb(country.getNomEnGb())
                        .build();
    }

    public static CountryDTO fromFullEntity(Country country) {
        return
                CountryDTO.builder()
                        .id(country.getId())
                        .code(country.getCode())
                        .alpha2(country.getAlpha2())
                        .alpha3(country.getAlpha3())
                        .nomFrFr(country.getNomFrFr())
                        .nomEnGb(country.getNomEnGb())
                        .movies(
                                Optional.ofNullable(country.getMovies())
                                        .orElse(Set.of())
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, null, null, null))
                                        .collect(Collectors.toSet())
                        )
                        .persons(fromEntitySet(country.getPersons()))
                        .build();
    }

    private static Set<PersonDTO> fromEntitySet(Set<? extends Person> personSet) {
        return
                Optional.ofNullable(personSet).orElse(Set.of())
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }

}
