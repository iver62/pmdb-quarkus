package org.desha.app.domain.dto;

import lombok.Getter;

import java.util.Set;

@Getter
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

}
