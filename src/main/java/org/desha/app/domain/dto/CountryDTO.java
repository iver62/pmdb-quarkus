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
    private Set<PersonDTO> actors;
    private Set<PersonDTO> producers;
    private Set<PersonDTO> directors;
    private Set<PersonDTO> screenwriters;
    private Set<PersonDTO> musicians;
    private Set<PersonDTO> photographers;
    private Set<PersonDTO> costumiers;
    private Set<PersonDTO> decorators;
    private Set<PersonDTO> editors;
    private Set<PersonDTO> casters;
    private Set<PersonDTO> artDirectors;
    private Set<PersonDTO> soundEditors;
    private Set<PersonDTO> visualEffectsSupervisors;
    private Set<PersonDTO> makeupArtists;
    private Set<PersonDTO> hairDressers;
    private Set<PersonDTO> stuntMen;

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
                        .actors(fromEntitySet(country.getActors()))
                        .producers(fromEntitySet(country.getProducers()))
                        .directors(fromEntitySet(country.getDirectors()))
                        .screenwriters(fromEntitySet(country.getScreenwriters()))
                        .musicians(fromEntitySet(country.getMusicians()))
                        .photographers(fromEntitySet(country.getPhotographers()))
                        .costumiers(fromEntitySet(country.getCostumiers()))
                        .decorators(fromEntitySet(country.getDecorators()))
                        .editors(fromEntitySet(country.getEditors()))
                        .casters(fromEntitySet(country.getCasters()))
                        .artDirectors(fromEntitySet(country.getArtDirectors()))
                        .soundEditors(fromEntitySet(country.getSoundEditors()))
                        .visualEffectsSupervisors(fromEntitySet(country.getVisualEffectsSupervisors()))
                        .makeupArtists(fromEntitySet(country.getMakeupArtists()))
                        .hairDressers(fromEntitySet(country.getHairDressers()))
                        .stuntMen(fromEntitySet(country.getStuntmen()))
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
