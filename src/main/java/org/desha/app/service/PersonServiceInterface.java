package org.desha.app.service;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Person;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;
import java.util.Set;

public interface PersonServiceInterface {

    Uni<Long> countPersons(CriteriaDTO criteriaDTO);

    Uni<Long> countCountries(String term, String lang);

    Uni<Long> countMoviesByPerson(@NotNull Long personId, CriteriaDTO criteriaDTO);

    Uni<PersonDTO> getById(@NotNull Long id);

    Uni<List<Person>> getByIds(List<Long> ids);

    Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriaDTO criteriaDTO);

    Uni<List<PersonDTO>> getAll();

    Uni<List<MovieDTO>> getMoviesByPerson(@NotNull Long id, Page page, String sort, Sort.Direction sortDirection, CriteriaDTO criteriaDTO);

    Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang);

    Uni<PersonDTO> save(PersonDTO personDTO);

    Uni<PersonDTO> update(@NotNull Long id, FileUpload file, PersonDTO personDTO);

    Uni<Set<CountryDTO>> updateCountries(@NotNull Long id, Set<CountryDTO> countryDTOSet);

    Uni<Set<CountryDTO>> addCountries(@NotNull Long id, Set<CountryDTO> countryDTOSet);

    Uni<Set<CountryDTO>> removeCountry(@NotNull Long personId, @NotNull Long countryId);

    Uni<Boolean> deletePerson(@NotNull Long id);

    Uni<Boolean> clearCountries(@NotNull Long id);
}
