package org.desha.app.mapper;

import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.entity.MovieActor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MovieActorMapper {

    MovieActorDTO toMovieActorDTO(MovieActor entity);

    List<MovieActorDTO> toDTOList(List<MovieActor> entityList);

    Set<MovieActorDTO> toDTOSet(Set<MovieActor> entitySet);
}
