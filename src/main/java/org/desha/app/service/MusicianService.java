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
import org.desha.app.domain.entity.Musician;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.MusicianRepository;

import java.util.List;

@Slf4j
@Singleton
public class MusicianService extends PersonService<Musician> {

    @Inject
    public MusicianService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            MusicianRepository musicianRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, musicianRepository, fileService);
    }

    public Uni<Long> countMovies(long musicianId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByMusician(musicianId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long musicianId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByMusician(musicianId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countMusicianCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findMusicianCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Musician> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Musician.fromDTO(personDTO).persist());
    }
}
