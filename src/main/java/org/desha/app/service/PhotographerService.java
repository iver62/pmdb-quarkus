package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Costumier;
import org.desha.app.domain.entity.Photographer;
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PhotographerRepository;

import java.util.List;

@Slf4j
@Singleton
public class PhotographerService extends PersonService<Photographer> {

    @Inject
    public PhotographerService(
            CountryService countryService,
            MovieRepository movieRepository,
            PhotographerRepository photographerRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, photographerRepository, fileService);
    }

    public Uni<Long> countMovies(Long photographerId, String term) {
        return movieRepository.countMoviesByCostumier(photographerId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long photographerId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByPhotographer(photographerId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Photographer> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Photographer.fromDTO(personDTO).persist());
    }
}
