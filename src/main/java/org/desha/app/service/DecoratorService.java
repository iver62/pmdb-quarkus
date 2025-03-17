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
import org.desha.app.domain.entity.Decorator;
import org.desha.app.repository.DecoratorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class DecoratorService extends PersonService<Decorator> {

    @Inject
    public DecoratorService(
            CountryService countryService,
            MovieRepository movieRepository,
            DecoratorRepository decoratorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, decoratorRepository, fileService);
    }

    public Uni<Long> countMovies(long decoratorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByDecorator(decoratorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long directorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByDecorator(directorId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Decorator> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Decorator.fromDTO(personDTO).persist());
    }
}
