package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Caster;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.repository.CasterRepository;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class CasterService extends PersonService<Caster> {

    @Inject
    public CasterService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            CasterRepository casterRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, casterRepository, fileService);
    }

    public Uni<Long> countMovies(long casterId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByCaster(casterId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long casterId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByCaster(casterId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    @Override
    public Uni<Long> countCountries(String term) {
        return countryRepository.countCasterCountries(term);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term) {
        return
                countryRepository.findCasterCountries(page, sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Caster> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
