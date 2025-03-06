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
import org.desha.app.domain.entity.Decorator;
import org.desha.app.repository.CostumierRepository;
import org.desha.app.repository.DecoratorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class CostumierService extends PersonService<Costumier> {

    @Inject
    public CostumierService(
            CountryService countryService,
            MovieRepository movieRepository,
            CostumierRepository costumierRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, costumierRepository, fileService);
    }

    public Uni<Long> countMovies(Long costumierId, String term) {
        return movieRepository.countMoviesByCostumier(costumierId, term);
    }

    public Uni<List<MovieDTO>> getMovies(Long costumierId, int page, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository
                        .findMoviesByCostumier(costumierId, page, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Costumier> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Costumier.fromDTO(personDTO).persist());
    }
}
