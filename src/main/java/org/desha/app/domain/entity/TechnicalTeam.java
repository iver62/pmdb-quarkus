package org.desha.app.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class TechnicalTeam {

    private Set<Producer> producers;
    private Set<Director> directors;
    private Set<Screenwriter> screenwriters;
    private Set<Musician> musicians;
    private Set<Photographer> photographers;
    private Set<Costumier> costumiers;
    private Set<Decorator> decorators;
    private Set<Editor> editors;
    private Set<Caster> casters;
    private Set<ArtDirector> artDirectors;
    private Set<SoundEditor> soundEditors;
    private Set<VisualEffectsSupervisor> visualEffectsSupervisors;
    private Set<MakeupArtist> makeupArtists;
    private Set<HairDresser> hairDressers;
    private Set<Stuntman> stuntmen;

    public static TechnicalTeam build(
            final Set<Producer> producers,
            final Set<Director> directors,
            final Set<Screenwriter> screenwriters,
            final Set<Musician> musicians,
            final Set<Photographer> photographers,
            final Set<Costumier> costumiers,
            final Set<Decorator> decorators,
            final Set<Editor> editors,
            final Set<Caster> casters,
            final Set<ArtDirector> artDirectors,
            final Set<SoundEditor> soundEditors,
            final Set<VisualEffectsSupervisor> visualEffectsSupervisors,
            final Set<MakeupArtist> makeupArtists,
            final Set<HairDresser> hairDressers,
            final Set<Stuntman> stuntmen
    ) {
        return TechnicalTeam.builder()
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
}
