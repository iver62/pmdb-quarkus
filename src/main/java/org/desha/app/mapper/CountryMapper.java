package org.desha.app.mapper;

import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "jakarta",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CountryMapper {

    CountryDTO toDTO(Country entity);

    List<CountryDTO> toDTOList(List<Country> countryList);

    Set<CountryDTO> toDTOSet(Set<Country> countrySet);
}
