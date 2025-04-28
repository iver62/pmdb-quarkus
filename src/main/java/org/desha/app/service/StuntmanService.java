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
import org.desha.app.domain.entity.Stuntman;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.StuntmanRepository;

import java.util.List;

@Slf4j
@Singleton
public class StuntmanService extends PersonService<Stuntman> {

    @Inject
    public StuntmanService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            StuntmanRepository stuntmanRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, stuntmanRepository, fileService);
    }

    public Uni<Long> countMovies(long stuntmanId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByStuntman(stuntmanId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long stuntmanId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByStuntman(stuntmanId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countStuntmanCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findStuntmanCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Stuntman> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Stuntman.fromDTO(personDTO).persist());
    }
}
