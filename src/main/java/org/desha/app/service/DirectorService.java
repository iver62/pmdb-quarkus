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
import org.desha.app.domain.entity.Director;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.DirectorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class DirectorService extends PersonService<Director> {

    @Inject
    public DirectorService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            DirectorRepository directorRepository,
            FileService fileService
    ) {
        super(countryService, countryRepository, movieRepository, directorRepository, fileService);
    }

    public Uni<Long> countMovies(long directorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByDirector(directorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long directorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByDirector(directorId, page, sort, direction, criteriasDTO)
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
        return countryRepository.countDirectorCountries(term, lang);
    }

    @Override
    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findDirectorCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Director> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Director.fromDTO(personDTO).persist());
    }

}
