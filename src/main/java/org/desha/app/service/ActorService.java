package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Actor;
import org.desha.app.domain.entity.MovieActor;
import org.desha.app.repository.ActorRepository;
import org.desha.app.repository.CountryRepository;
import org.desha.app.repository.MovieRepository;

import java.util.List;

@Slf4j
@Singleton
public class ActorService extends PersonService<Actor> {

    private final ActorRepository actorRepository;
    private final StatsService statsService;

    @Inject
    public ActorService(
            CountryService countryService,
            CountryRepository countryRepository,
            MovieRepository movieRepository,
            ActorRepository actorRepository,
            FileService fileService,
            StatsService statsService
    ) {
        super(countryService, countryRepository, movieRepository, actorRepository, fileService);
        this.actorRepository = actorRepository;
        this.statsService = statsService;
    }

    @Override
    public Uni<List<PersonDTO>> get(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                actorRepository
                        .find(page, sort, direction, criteriasDTO)
                        .map(
                                actorList ->
                                        actorList
                                                .stream()
                                                .map(actor -> PersonDTO.fromEntity(actor, actor.getMovieActors().size()))
                                                .toList()
                        )
                ;
    }

    public Uni<Long> countMovies(long actorId, CriteriasDTO criteriasDTO) {
        return movieRepository.countMoviesByActor(actorId, criteriasDTO);
    }

    public Uni<Long> countCountries(String term, String lang) {
        return countryRepository.countActorCountries(term, lang);
    }

    public Uni<List<MovieDTO>> getMovies(long actorId, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository
                        .findMoviesByActor(actorId, page, sort, direction, criteriasDTO)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(movie -> MovieDTO.fromEntity(movie, movie.getAwards()))
                                        .toList()
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang) {
        return
                countryRepository.findActorCountries(page, sort, direction, term, lang)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<Actor> save(PersonDTO personDTO) {
        return
                Panache.withTransaction(() ->
                        actorRepository.persist(Actor.fromDTO(personDTO))
                                .invoke(statsService::incrementNumberOfActors)
                );
    }

    public List<MovieActorDTO> fromMovieActorListEntity(List<MovieActor> movieActorSet) {
        return
                movieActorSet
                        .stream()
                        .map(MovieActorDTO::fromEntity)
                        .sorted(MovieActorDTO::compareTo)
                        .toList()
                ;
    }

}