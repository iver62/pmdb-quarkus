package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class TechnicalSummaryDTO {

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
    private Set<PersonDTO> visualEffectsSupervisor;
    private Set<PersonDTO> makeupArtists;
    private Set<PersonDTO> hairDressers;

    public static TechnicalSummaryDTO build(
            final Set<PersonDTO> producers,
            final Set<PersonDTO> directors,
            final Set<PersonDTO> screenwriters,
            final Set<PersonDTO> musicians,
            final Set<PersonDTO> photographers,
            final Set<PersonDTO> costumiers,
            final Set<PersonDTO> decorators,
            final Set<PersonDTO> editors,
            final Set<PersonDTO> casting,
            final Set<PersonDTO> artDirectors,
            final Set<PersonDTO> soundEditors,
            final Set<PersonDTO> visualEffectsSupervisor,
            final Set<PersonDTO> makeupArtists,
            final Set<PersonDTO> hairDressers
    ) {
        return TechnicalSummaryDTO.builder()
                .producers(producers)
                .directors(directors)
                .screenwriters(screenwriters)
                .musicians(musicians)
                .photographers(photographers)
                .costumiers(costumiers)
                .decorators(decorators)
                .editors(editors)
                .casters(casting)
                .artDirectors(artDirectors)
                .soundEditors(soundEditors)
                .visualEffectsSupervisor(visualEffectsSupervisor)
                .makeupArtists(makeupArtists)
                .hairDressers(hairDressers)
                .build();
    }
}
