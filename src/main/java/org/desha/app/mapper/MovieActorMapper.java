package org.desha.app.mapper;

import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.entity.MovieActor;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MovieActorMapper {

    MovieActorDTO toDTO(MovieActor entity);

    @Named("withoutMovie")
    @Mapping(target = "movie", ignore = true)
    MovieActorDTO toDTOWithoutMovie(MovieActor entity);

    @Named("withoutPersonAndRank")
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "rank", ignore = true)
    MovieActorDTO toDTOWithoutPerson(MovieActor entity);

    @IterableMapping(qualifiedByName = "withoutMovie")
    List<MovieActorDTO> toDTOListWithoutMovie(List<MovieActor> entityList);

    @IterableMapping(qualifiedByName = "withoutPersonAndRank")
    List<MovieActorDTO> toDTOListWithoutPerson(List<MovieActor> entityList);

    Set<MovieActorDTO> toDTOSet(Set<MovieActor> entitySet);
}
