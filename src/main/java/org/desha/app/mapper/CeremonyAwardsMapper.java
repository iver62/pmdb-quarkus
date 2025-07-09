package org.desha.app.mapper;

import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.domain.entity.CeremonyAwards;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        uses = AwardMapperUtils.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CeremonyAwardsMapper {

    @Mapping(target = "awards", source = "awards", qualifiedByName = "toAwardDTO")
    CeremonyAwardsDTO ceremonyAwardsToCeremonyAwardsDTO(CeremonyAwards entity);

    List<CeremonyAwardsDTO> toDTOList(List<CeremonyAwards> ceremonyAwardsList);

    @Mapping(target = "movie", source = "movie", ignore = true)
    Set<CeremonyAwardsDTO> toDTOSet(List<CeremonyAwards> ceremonyAwardsSet);
}
