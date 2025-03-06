package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class TechnicalTeamDTO {

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
    private Set<PersonDTO> stuntmen;

    public static TechnicalTeamDTO build(
            final Set<PersonDTO> producers,
            final Set<PersonDTO> directors,
            final Set<PersonDTO> screenwriters,
            final Set<PersonDTO> musicians,
            final Set<PersonDTO> photographers,
            final Set<PersonDTO> costumiers,
            final Set<PersonDTO> decorators,
            final Set<PersonDTO> editors,
            final Set<PersonDTO> casters,
            final Set<PersonDTO> artDirectors,
            final Set<PersonDTO> soundEditors,
            final Set<PersonDTO> visualEffectsSupervisors,
            final Set<PersonDTO> makeupArtists,
            final Set<PersonDTO> hairDressers,
            final Set<PersonDTO> stuntmen
    ) {
        return TechnicalTeamDTO.builder()
                .producers(producers)
                .directors(directors)
                .screenwriters(screenwriters)
                .musicians(musicians)
                .photographers(photographers)
                .costumiers(costumiers)
                .decorators(decorators)
                .editors(editors)
                .casters(casters)
                .artDirectors(artDirectors)
                .soundEditors(soundEditors)
                .visualEffectsSupervisors(visualEffectsSupervisors)
                .makeupArtists(makeupArtists)
                .hairDressers(hairDressers)
                .stuntmen(stuntmen)
                .build();
    }

    public static TechnicalTeamDTO fromEntity(Movie movie) {
        return
                TechnicalTeamDTO.build(
                        movie.getProducers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getDirectors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getScreenwriters().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getMusicians().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getPhotographers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getCostumiers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getDecorators().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getEditors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        movie.getCasters().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet()

//                        movie.getArtDirectors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
//                        movie.getSoundEditors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
//                        movie.getVisualEffectsSupervisors().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
//                        movie.getMakeupArtists().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
//                        movie.getHairDressers().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet()),
//                        movie.getStuntmen().stream().map(PersonDTO::fromEntity).collect(Collectors.toSet())
                );
    }

    // Convertit une collection d'entités en DTOs
    private static Set<PersonDTO> toDTOSet(Set<Person> persons) {
        return
                persons
                        .stream()
                        .map(PersonDTO::fromEntity)
                        .collect(Collectors.toSet());
    }

}
