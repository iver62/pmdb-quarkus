package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Actor;
import org.desha.app.repository.ActorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class ActorService extends PersonService<Actor> {

    @Inject
    public ActorService(
            CountryService countryService,
            MovieRepository movieRepository,
            ActorRepository actorRepository,
            FileService fileService
    ) {
        super(countryService, movieRepository, actorRepository, fileService);
    }

    public Uni<Long> countMovies(long actorId, FiltersDTO filtersDTO) {
        return movieRepository.countMoviesByActor(actorId, filtersDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long actorId, int page, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        return
                movieRepository
                        .findMoviesByActor(actorId, page, size, sort, direction, filtersDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    public Uni<Actor> save(PersonDTO personDTO) {
        return Panache.withTransaction(() -> Actor.fromDTO(personDTO).persist());
    }

}
