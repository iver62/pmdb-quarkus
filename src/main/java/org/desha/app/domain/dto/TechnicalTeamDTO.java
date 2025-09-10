package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Équipe technique associée à un film, regroupée par rôle")
public class TechnicalTeamDTO {

    @Schema(description = "Liste des réalisateurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> directors;

    @Schema(description = "Liste des assistants réalisateurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> assistantDirectors;

    @Schema(description = "Liste des scénaristes", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> screenwriters;

    @Schema(description = "Liste des producteurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> producers;

    @Schema(description = "Liste des compositeurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> composers;

    @Schema(description = "Liste des musiciens", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> musicians;

    @Schema(description = "Liste des photographes", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> photographers;

    @Schema(description = "Liste des créateurs de costumes", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> costumeDesigners;

    @Schema(description = "Liste des décorateurs de plateau", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> setDesigners;

    @Schema(description = "Liste des monteurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> editors;

    @Schema(description = "Liste des directeurs de casting", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> casters;

    @Schema(description = "Liste des artistes", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> artists;

    @Schema(description = "Liste des ingénieurs du son", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> soundEditors;

    @Schema(description = "Liste des superviseurs des effets visuels", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> vfxSupervisors;

    @Schema(description = "Liste des superviseurs des effets spéciaux", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> sfxSupervisors;

    @Schema(description = "Liste des maquilleurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> makeupArtists;

    @Schema(description = "Liste des coiffeurs", type = SchemaType.ARRAY)
    private List<MovieTechnicianDTO> hairDressers;

    @Schema(description = "Liste des cascadeurs", type = SchemaType.ARRAY)
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
        return
                TechnicalTeamDTO.builder()
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
                        .build()
                ;
    }

}
