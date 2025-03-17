package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Stuntman;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.StuntmanRepository;

import java.util.List;

@Slf4j
@Singleton
public class StuntmanService extends PersonService<Stuntman> {

    @Inject
    public StuntmanService(
            CountryService countryService,
            MovieRepository movieRepository,
            StuntmanRepository stuntmanRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, stuntmanRepository, fileService);
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
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Stuntman> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Stuntman.fromDTO(personDTO).persist());
    }
}
