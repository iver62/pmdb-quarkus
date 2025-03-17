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

    public Uni<Long> countMovies(long actorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByActor(actorId, criteriasDTO);
    }

    public Uni<List<MovieDTO>> getMovies(long actorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByActor(actorId, page, sort, direction, criteriasDTO)
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
