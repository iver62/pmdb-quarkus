package org.desha.app.mapper;

import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.domain.entity.Ceremony;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CeremonyMapper {

    CeremonyDTO ceremonyToCeremonyDTO(Ceremony entity);

    Ceremony ceremonyDTOtoCeremony(CeremonyDTO dto);

    List<CeremonyDTO> toDTOList(List<Ceremony> ceremonyList);

    Set<CeremonyDTO> toDTOSet(List<Ceremony> ceremonySet);
}
