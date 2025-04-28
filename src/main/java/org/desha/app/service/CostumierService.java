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
import org.desha.app.domain.entity.Costumier;
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class CostumierService extends PersonService<Costumier> {

    @Inject
    public CostumierService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            CostumierRepository costumierRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, costumierRepository, fileService);
    }

    public Uni<Long> countMovies(long costumierId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByCostumier(costumierId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long costumierId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByCostumier(costumierId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    @Override
    public Uni<Long> countCountries(String term, String lang) {
        return countryRepository.countCostumierCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findCostumierCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Costumier> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
