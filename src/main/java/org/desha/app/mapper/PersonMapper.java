package org.desha.app.mapper;

import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Person;
import org.desha.app.domain.record.PersonWithMoviesNumber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PersonMapper {

    Person toEntity(PersonDTO dto);

    @Mapping(target = "awards", ignore = true)
    PersonDTO toDTO(Person entity);

    @Mapping(target = "id", source = "person.id")
    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "photoFileName", source = "person.photoFileName")
    @Mapping(target = "dateOfBirth", source = "person.dateOfBirth")
    @Mapping(target = "dateOfDeath", source = "person.dateOfDeath")
    @Mapping(target = "types", source = "person.types")
    @Mapping(target = "creationDate", source = "person.creationDate")
    @Mapping(target = "lastUpdate", source = "person.lastUpdate")
    @Mapping(target = "numberOfMovies", source = "moviesNumber")
    @Mapping(target = "numberOfAwards", source = "awardsNumber")
    PersonDTO personWithMoviesNumberToPersonDTO(PersonWithMoviesNumber entity);

    LitePersonDTO toLiteDTO(Person entity);

    List<PersonDTO> toDTOList(List<Person> personList);

    List<PersonDTO> toDTOWithNumbersList(List<PersonWithMoviesNumber> entityList);

    List<LitePersonDTO> toLiteDTOList(List<Person> personList);

    Set<PersonDTO> toDTOSet(Set<Person> personSet);
}
