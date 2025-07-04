package org.desha.app.domain.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalTeam {

    private List<MovieTechnician> producers;
    private List<MovieTechnician> directors;
    private List<MovieTechnician> assistantDirectors;
    private List<MovieTechnician> screenwriters;
    private List<MovieTechnician> composers;
    private List<MovieTechnician> musicians;
    private List<MovieTechnician> photographers;
    private List<MovieTechnician> costumeDesigners;
    private List<MovieTechnician> setDesigners;
    private List<MovieTechnician> editors;
    private List<MovieTechnician> casters;
    private List<MovieTechnician> artists;
    private List<MovieTechnician> soundEditors;
    private List<MovieTechnician> vfxSupervisors;
    private List<MovieTechnician> sfxSupervisors;
    private List<MovieTechnician> makeupArtists;
    private List<MovieTechnician> hairDressers;
    private List<MovieTechnician> stuntmen;

    public static TechnicalTeam build(
            final List<MovieTechnician> producers,
            final List<MovieTechnician> directors,
            final List<MovieTechnician> assistantDirectors,
            final List<MovieTechnician> screenwriters,
            final List<MovieTechnician> composers,
            final List<MovieTechnician> musicians,
            final List<MovieTechnician> photographers,
            final List<MovieTechnician> costumeDesigners,
            final List<MovieTechnician> setDesigners,
            final List<MovieTechnician> editors,
            final List<MovieTechnician> casters,
            final List<MovieTechnician> artists,
            final List<MovieTechnician> soundEditors,
            final List<MovieTechnician> vfxSupervisors,
            final List<MovieTechnician> sfxSupervisors,
            final List<MovieTechnician> makeupArtists,
            final List<MovieTechnician> hairDressers,
            final List<MovieTechnician> stuntmen
    ) {
        return TechnicalTeam.builder()
                .producers(producers)
                .directors(directors)
                .assistantDirectors(assistantDirectors)
                .screenwriters(screenwriters)
                .composers(composers)
                .musicians(musicians)
                .photographers(photographers)
                .costumeDesigners(costumeDesigners)
                .setDesigners(setDesigners)
                .editors(editors)
                .casters(casters)
                .artists(artists)
                .soundEditors(soundEditors)
                .vfxSupervisors(vfxSupervisors)
                .sfxSupervisors(sfxSupervisors)
                .makeupArtists(makeupArtists)
                .hairDressers(hairDressers)
                .stuntmen(stuntmen)
                .build();
    }
}
