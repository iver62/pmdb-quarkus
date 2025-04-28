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
import org.desha.app.domain.entity.HairDresser;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.HairDresserRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class HairDresserService extends PersonService<HairDresser> {

    @Inject
    public HairDresserService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            HairDresserRepository hairDresserRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, hairDresserRepository, fileService);
    }

    public Uni<Long> countMovies(long hairDresserId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByHairDresser(hairDresserId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long hairDresserId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByHairDresser(hairDresserId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countHairDresserCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findHairDresserCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<HairDresser> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
