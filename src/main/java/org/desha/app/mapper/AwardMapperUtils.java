package org.desha.app.mapper;

import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.CeremonyAwards;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AwardMapperUtils {

    @Named("toAwardDTO")
    @Mapping(target = "persons", source = "personSet")
    @Mapping(target = "ceremonyAwards", ignore = true)
    AwardDTO awardToAwardDTO(Award entity);

    @Named("liteCeremonyAwards")
    @Mapping(target = "awards", ignore = true)
    CeremonyAwardsDTO toLiteCeremonyAwardsDTO(CeremonyAwards entity);

}