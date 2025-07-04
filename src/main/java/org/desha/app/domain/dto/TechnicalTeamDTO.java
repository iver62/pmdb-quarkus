package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.desha.app.domain.entity.Movie;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicalTeamDTO {

    private List<MovieTechnicianDTO> directors;
    private List<MovieTechnicianDTO> assistantDirectors;
    private List<MovieTechnicianDTO> screenwriters;
    private List<MovieTechnicianDTO> producers;
    private List<MovieTechnicianDTO> composers;
    private List<MovieTechnicianDTO> musicians;
    private List<MovieTechnicianDTO> photographers;
    private List<MovieTechnicianDTO> costumeDesigners;
    private List<MovieTechnicianDTO> setDesigners;
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
            final List<MovieTechnicianDTO> assistantDirectors,
            final List<MovieTechnicianDTO> screenwriters,
            final List<MovieTechnicianDTO> composers,
            final List<MovieTechnicianDTO> musicians,
            final List<MovieTechnicianDTO> photographers,
            final List<MovieTechnicianDTO> costumeDesigners,
            final List<MovieTechnicianDTO> setDesigners,
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

    public static TechnicalTeamDTO of(Movie movie) {
        return
                TechnicalTeamDTO.build(
                        movie.getMovieProducers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieDirectors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieAssistantDirectors().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieScreenwriters().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieComposers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieMusicians().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMoviePhotographers().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieCostumeDesigners().stream().map(MovieTechnicianDTO::of).toList(),
                        movie.getMovieSetDesigners().stream().map(MovieTechnicianDTO::of).toList(),
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
