package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.dto.MovieStatsDTO;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.domain.record.Repartition;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@ApplicationScoped
public class StatsService {

    private final AtomicLong movieCount = new AtomicLong(0);
    private final AtomicLong actorCount = new AtomicLong(0);
    private List<Repartition> moviesByReleaseDateRepartition = new ArrayList<>();
    private List<Repartition> moviesByCategoryRepartition = new ArrayList<>();
    private List<Repartition> moviesByCountryRepartition = new ArrayList<>();
    private List<Repartition> moviesByUserRepartition = new ArrayList<>();
    private List<Repartition> moviesByCreationDateRepartition = new ArrayList<>();
    private List<Repartition> moviesNumberEvolution = new ArrayList<>();
    private List<Repartition> actorsNumberEvolution = new ArrayList<>();

    private final BroadcastProcessor<MovieStatsDTO> statsProcessor = BroadcastProcessor.create();

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;

    @Inject
    public StatsService(MovieRepository movieRepository, PersonRepository personRepository) {
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
    }

    void onStart(@Observes StartupEvent ev) throws Throwable {
        VertxContextSupport.subscribeAndAwait(() ->
                Panache.withSession(() ->
                        movieRepository.count()
                                .invoke(movieCount::set)
                                .chain(() -> personRepository.countPersons(CriteriaDTO.builder().personTypes(Set.of(PersonType.ACTOR)).build())
                                        .invoke(actorCount::set))
                                .chain(() -> movieRepository.findMoviesByReleaseDateRepartition()
                                        .invoke(repartition -> moviesByReleaseDateRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByCategoryRepartition()
                                        .invoke(repartition -> moviesByCategoryRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByCountryRepartition()
                                        .invoke(repartition -> moviesByCountryRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByUserRepartition()
                                        .invoke(repartition -> moviesByUserRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByCreationDateRepartition()
                                        .invoke(repartition -> moviesByCreationDateRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesCreationDateEvolution()
                                        .invoke(repartition -> moviesNumberEvolution = repartition))
                                .chain(() -> personRepository.findActorsCreationDateEvolution()
                                        .invoke(repartition -> actorsNumberEvolution = repartition))
                                .invoke(() -> {
                                    MovieStatsDTO stats = getCurrentStats();
                                    statsProcessor.onNext(stats);
                                    log.info("MovieStats emitted: {}", stats);
                                })
                )
        );
    }

    public void updateAndEmitStats() {
        statsProcessor.onNext(getCurrentStats());
    }

    public MovieStatsDTO getCurrentStats() {
        return
                MovieStatsDTO.build(
                        movieCount.get(),
                        actorCount.get(),
                        moviesByReleaseDateRepartition,
                        moviesByCategoryRepartition,
                        moviesByCountryRepartition,
                        moviesByUserRepartition,
                        moviesByCreationDateRepartition,
                        moviesNumberEvolution,
                        actorsNumberEvolution
                );
    }

    public Flow.Publisher<MovieStatsDTO> getStatsPublisher() {
        return Multi.createBy().concatenating().streams(
                Multi.createFrom().item(getCurrentStats()),
                statsProcessor
        );
    }

    public Uni<Void> incrementNumberOfMovies() {
        return
                updateMoviesByReleaseDateRepartition()
                        .invoke(movieCount::incrementAndGet)
                        .chain(this::updateMoviesByCreationDateRepartition)
                        .chain(this::updateMoviesByUserRepartition)
                        .chain(this::updateMoviesNumberEvolution)
                        .invoke(this::updateAndEmitStats)
                        .onFailure().invoke(t -> log.error("Error while incrementing number of movies", t))
                ;
    }

    public Uni<Void> decrementNumberOfMovies() {
        return
                updateMoviesByReleaseDateRepartition()
                        .invoke(movieCount::decrementAndGet)
                        .chain(this::updateMoviesByCreationDateRepartition)
                        .chain(this::updateMoviesByUserRepartition)
                        .chain(this::updateMoviesNumberEvolution)
                        .invoke(this::updateAndEmitStats)
                        .onFailure().invoke(t -> log.error("Error while decrementing number of movies", t))
                ;
    }

    public Uni<Void> updateActorsStats() {
        return
                updateNumberOfActors()
                        .chain(this::updateActorsNumberEvolution)
                        .onFailure().invoke(t -> log.error("Error while updating number of actors", t));
    }

    private Uni<Void> updateNumberOfActors() {
        return
                Panache.withTransaction(() ->
                        personRepository.countPersons(CriteriaDTO.builder().personTypes(Set.of(PersonType.ACTOR)).build())
                                .invoke(aLong -> {
                                            actorCount.set(aLong);
                                            updateAndEmitStats();
                                            log.info("Actors number updated: {}", aLong);
                                        }
                                ).replaceWithVoid()
                );
    }

    public Uni<Void> updateMoviesByReleaseDateRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByReleaseDateRepartition()
                                .invoke(repartition -> {
                                            moviesByReleaseDateRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by release date repartition updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    public Uni<Void> updateMoviesByCategoryRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByCategoryRepartition()
                                .invoke(repartition -> {
                                            moviesByCategoryRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by category repartition updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    public Uni<Void> updateMoviesByCountryRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByCountryRepartition()
                                .invoke(repartition -> {
                                            moviesByCountryRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by country repartition updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    private Uni<Void> updateMoviesByUserRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByUserRepartition()
                                .invoke(repartition -> {
                                            moviesByUserRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by user repartition updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    private Uni<Void> updateMoviesByCreationDateRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByCreationDateRepartition()
                                .invoke(repartition -> {
                                            moviesByCreationDateRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by creation date repartition updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    private Uni<Void> updateMoviesNumberEvolution() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesCreationDateEvolution()
                                .invoke(repartition -> {
                                            moviesNumberEvolution = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies number evolution updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }

    private Uni<Void> updateActorsNumberEvolution() {
        return
                Panache.withTransaction(() ->
                        personRepository.findActorsCreationDateEvolution()
                                .invoke(repartition -> {
                                            actorsNumberEvolution = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Actors number evolution updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }
}
