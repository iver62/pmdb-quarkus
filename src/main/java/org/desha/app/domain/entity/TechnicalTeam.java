package org.desha.app.domain.entity;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalTeam {

    private Set<Person> producers;
    private Set<Person> directors;
    private Set<Person> screenwriters;
    private Set<Person> dialogueWriters;
    private Set<Person> musicians;
    private Set<Person> photographers;
    private Set<Person> costumiers;
    private Set<Person> decorators;
    private Set<Person> editors;
    private Set<Person> casters;
    private Set<Person> artDirectors;
    private Set<Person> soundEditors;
    private Set<Person> visualEffectsSupervisors;
    private Set<Person> makeupArtists;
    private Set<Person> hairDressers;
    private Set<Person> stuntmen;

    public static TechnicalTeam build(
            final Set<Person> producers,
            final Set<Person> directors,
            final Set<Person> screenwriters,
            final Set<Person> dialogueWriters,
            final Set<Person> musicians,
            final Set<Person> photographers,
            final Set<Person> costumiers,
            final Set<Person> decorators,
            final Set<Person> editors,
            final Set<Person> casters,
            final Set<Person> artDirectors,
            final Set<Person> soundEditors,
            final Set<Person> visualEffectsSupervisors,
            final Set<Person> makeupArtists,
            final Set<Person> hairDressers,
            final Set<Person> stuntmen
    ) {
        return TechnicalTeam.builder()
                .producers(producers)
                .directors(directors)
                .screenwriters(screenwriters)
                .dialogueWriters(dialogueWriters)
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
}
