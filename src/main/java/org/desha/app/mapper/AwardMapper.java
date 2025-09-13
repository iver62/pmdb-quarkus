package org.desha.app.mapper;

import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.entity.Award;
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
public interface AwardMapper {

    @Mapping(target = "persons", source = "personSet")
    @Mapping(target = "ceremonyAwards", source = "ceremonyAwards", qualifiedByName = "liteCeremonyAwards")
    AwardDTO todDTO(Award entity);

    Award toEntity(AwardDTO awardDTO);

    List<AwardDTO> toDTOList(List<Award> entityList);

    Set<AwardDTO> toDTOSet(Set<Award> awardSet);
}
