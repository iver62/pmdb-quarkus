package org.desha.app.mapper;

import org.desha.app.domain.dto.TechnicalTeamDTO;
import org.desha.app.domain.entity.TechnicalTeam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "jakarta",
        uses = MovieTechnicianMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TechnicalTeamMapper {

    @Mapping(target = "directors", source = "movieDirectors")
    @Mapping(target = "assistantDirectors", source = "movieAssistantDirectors")
    @Mapping(target = "screenwriters", source = "movieScreenwriters")
    @Mapping(target = "producers", source = "movieProducers")
    @Mapping(target = "composers", source = "movieComposers")
    @Mapping(target = "musicians", source = "movieMusicians")
    @Mapping(target = "photographers", source = "moviePhotographers")
    @Mapping(target = "costumeDesigners", source = "movieCostumeDesigners")
    @Mapping(target = "stageDesigners", source = "movieSetDesigners")
    @Mapping(target = "editors", source = "movieEditors")
    @Mapping(target = "casters", source = "movieCasters")
    @Mapping(target = "artists", source = "movieArtists")
    @Mapping(target = "soundEditors", source = "movieSoundEditors")
    @Mapping(target = "vfxSupervisors", source = "movieVfxSupervisors")
    @Mapping(target = "sfxSupervisors", source = "movieSfxSupervisors")
    @Mapping(target = "makeupArtists", source = "movieMakeupArtists")
    @Mapping(target = "hairDressers", source = "movieHairDressers")
    @Mapping(target = "stuntmen", source = "movieStuntmen")
    TechnicalTeamDTO toDTO(TechnicalTeam entity);

}
