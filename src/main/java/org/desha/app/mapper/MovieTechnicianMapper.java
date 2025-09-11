package org.desha.app.mapper;

import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.domain.entity.MovieTechnician;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MovieTechnicianMapper {

    @Mapping(target = "movie", ignore = true)
    MovieTechnicianDTO toMovieTechnicianDTO(MovieTechnician entity);

    List<MovieTechnicianDTO> toDTOList(List<? extends MovieTechnician> entityList);

}
