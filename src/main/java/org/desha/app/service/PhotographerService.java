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
import org.desha.app.domain.entity.Photographer;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PhotographerRepository;

import java.util.List;

@Slf4j
@Singleton
public class PhotographerService extends PersonService<Photographer> {

    @Inject
    public PhotographerService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            PhotographerRepository photographerRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, photographerRepository, fileService);
    }

    public Uni<Long> countMovies(long photographerId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByPhotographer(photographerId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long photographerId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByPhotographer(photographerId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countPhotographerCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findPhotographerCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Photographer> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Photographer.fromDTO(personDTO).persist());
    }
}
