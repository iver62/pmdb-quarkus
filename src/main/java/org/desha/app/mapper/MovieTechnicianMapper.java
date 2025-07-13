package org.desha.app.mapper;

import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.domain.entity.MovieTechnician;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MovieTechnicianMapper {

    MovieTechnicianDTO toMovieTechnicianDTO(MovieTechnician entity);

    List<MovieTechnicianDTO> toDTOList(List<? extends MovieTechnician> entityList);

    Set<MovieTechnicianDTO> toDTOSet(Set<MovieTechnician> entitySet);
}
