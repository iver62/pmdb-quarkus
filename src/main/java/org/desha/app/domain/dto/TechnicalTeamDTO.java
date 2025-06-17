package org.desha.app.domain.dto;

import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Movie;

import java.util.List;

@Getter
@Builder
public class TechnicalTeamDTO {

    private List<MovieTechnicianDTO> producers;
    private List<MovieTechnicianDTO> directors;
    private List<MovieTechnicianDTO> screenwriters;
    private List<MovieTechnicianDTO> musicians;
    private List<MovieTechnicianDTO> photographers;
    private List<MovieTechnicianDTO> costumiers;
    private List<MovieTechnicianDTO> decorators;
    private List<MovieTechnicianDTO> editors;
    private List<MovieTechnicianDTO> casters;
    private List<MovieTechnicianDTO> artists;
    private List<MovieTechnicianDTO> soundEditors;
    private List<MovieTechnicianDTO> vfxSupervisors;
    private List<MovieTechnicianDTO> sfxSupervisors;
    private List<MovieTechnicianDTO> makeupArtists;
    private List<MovieTechnicianDTO> hairDressers;
    private List<MovieTechnicianDTO> stuntmen;

    public static TechnicalTeamDTO build(
            final List<MovieTechnicianDTO> producers,
            final List<MovieTechnicianDTO> directors,
            final List<MovieTechnicianDTO> screenwriters,
            final List<MovieTechnicianDTO> musicians,
            final List<MovieTechnicianDTO> photographers,
            final List<MovieTechnicianDTO> costumiers,
            final List<MovieTechnicianDTO> decorators,
            final List<MovieTechnicianDTO> editors,
            final List<MovieTechnicianDTO> casters,
            final List<MovieTechnicianDTO> artists,
            final List<MovieTechnicianDTO> soundEditors,
            final List<MovieTechnicianDTO> vfxSupervisors,
            final List<MovieTechnicianDTO> sfxSupervisors,
            final List<MovieTechnicianDTO> makeupArtists,
            final List<MovieTechnicianDTO> hairDressers,
            final List<MovieTechnicianDTO> stuntmen
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
                .artists(artists)
                .soundEditors(soundEditors)
                .vfxSupervisors(vfxSupervisors)
                .sfxSupervisors(sfxSupervisors)
                .makeupArtists(makeupArtists)
                .hairDressers(hairDressers)
                .stuntmen(stuntmen)
                .build();
    }

    public static TechnicalTeamDTO of(Movie movie) {
        return
                TechnicalTeamDTO.build(
                        movie.getMovieProducers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieDirectors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieScreenwriters().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieMusicians().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMoviePhotographers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieCostumiers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieDecorators().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieEditors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieCasters().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieArtists().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieSoundEditors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieVfxSupervisors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieSfxSupervisors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieMakeupArtists().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieHairDressers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieStuntmen().stream().map(MovieTechnicianDTO::of).toList()
                );
    }

}
