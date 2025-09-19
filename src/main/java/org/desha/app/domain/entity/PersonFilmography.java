package org.desha.app.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@AllArgsConstructor
public class PersonFilmography {

    @OneToMany(mappedBy = "person")
    private final List<MovieActor> playedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieProducer> producedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieDirector> directedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieAssistantDirector> assistantDirectedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieScreenwriter> writtenMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieComposer> composedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieMusician> musicalMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MoviePhotographer> photographedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieCostumeDesigner> costumeDesignedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieSetDesigner> setDesignedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieEditor> editedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieCaster> castedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieArtist> artistMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieSoundEditor> soundEditedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieVfxSupervisor> vfxSupervisedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieSfxSupervisor> sfxSupervisedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieMakeupArtist> makeupMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieHairDresser> hairStyledMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private final List<MovieStuntman> stuntMovies = new ArrayList<>();
}
