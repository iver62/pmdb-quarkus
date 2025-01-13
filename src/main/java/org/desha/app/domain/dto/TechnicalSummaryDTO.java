package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Person;

import java.util.Set;

@Getter
@Builder
public class TechnicalSummaryDTO {

    private Set<ProducerDTO> producers;
    private Set<DirectorDTO> directors;
    private Set<Person> screenwriters;
    private Set<Person> musicians;
    private Set<Person> photographers;
    private Set<Person> costumiers;
    private Set<Person> decorators;
    private Set<Person> editors;
    private Set<Person> casters;
    private Set<Person> artDirectors;
    private Set<Person> soundEditors;
    private Set<Person> visualEffectsSupervisor;
    private Set<Person> makeupArtists;
    private Set<Person> barbers;

    public static TechnicalSummaryDTO build(
            final Set<ProducerDTO> producers,
            final Set<DirectorDTO> directors,
            final Set<Person> screenwriters,
            final Set<Person> musicians,
            final Set<Person> photographers,
            final Set<Person> costumiers,
            final Set<Person> decorators,
            final Set<Person> editors,
            final Set<Person> casting,
            final Set<Person> artDirectors,
            final Set<Person> soundEditors,
            final Set<Person> visualEffectsSupervisor,
            final Set<Person> makeupArtists,
            final Set<Person> barbers
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
                .barbers(barbers)
                .build();
    }
}
