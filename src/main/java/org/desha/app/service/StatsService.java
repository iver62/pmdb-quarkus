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
import org.desha.app.domain.dto.MovieStats;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;
import org.desha.app.repository.ActorRepository;
import org.desha.app.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@ApplicationScoped
public class StatsService {

    private final AtomicLong movieCount = new AtomicLong(0);
    private final AtomicLong actorCount = new AtomicLong(0);
    private List<Repartition> moviesByReleaseDateRepartition = new ArrayList<>();
    private List<Repartition> moviesByGenreRepartition = new ArrayList<>();
    private List<CountryRepartition> moviesByCountryRepartition = new ArrayList<>();
    private List<Repartition> moviesByUserRepartition = new ArrayList<>();
    private List<Repartition> moviesByCreationDateRepartition = new ArrayList<>();
    private List<Repartition> movieNumberEvolution = new ArrayList<>();

    private final BroadcastProcessor<MovieStats> statsProcessor = BroadcastProcessor.create();

    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;

    @Inject
    public StatsService(ActorRepository actorRepository, MovieRepository movieRepository) {
        this.actorRepository = actorRepository;
        this.movieRepository = movieRepository;
    }

    void onStart(@Observes StartupEvent ev) throws Throwable {
        log.info("The application is starting...");
        VertxContextSupport.subscribeAndAwait(() ->
                Panache.withSession(() ->
                        movieRepository.count()
                                .invoke(movieCount::set)
                                .chain(() -> actorRepository.count()
                                        .invoke(actorCount::set))
                                .chain(() -> movieRepository.findMoviesByReleaseDateRepartition()
                                        .invoke(repartition -> moviesByReleaseDateRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByGenreRepartition()
                                        .invoke(repartition -> moviesByGenreRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByCountryRepartition()
                                        .invoke(repartition -> moviesByCountryRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByUserRepartition()
                                        .invoke(repartition -> moviesByUserRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesByCreationDateRepartition()
                                        .invoke(repartition -> moviesByCreationDateRepartition = repartition))
                                .chain(() -> movieRepository.findMoviesCreationDateEvolution()
                                        .invoke(repartition -> movieNumberEvolution = repartition))
                                .invoke(() -> {
                                    MovieStats stats = getCurrentStats();
                                    statsProcessor.onNext(stats);
                                    log.info("MovieStats emitted: {}", stats);
                                })
                )
        );
    }

    public void updateAndEmitStats() {
        statsProcessor.onNext(getCurrentStats());
    }

    public MovieStats getCurrentStats() {
        return
                MovieStats.build(
                        movieCount.get(),
                        actorCount.get(),
                        moviesByReleaseDateRepartition,
                        moviesByGenreRepartition,
                        moviesByCountryRepartition,
                        moviesByUserRepartition,
                        moviesByCreationDateRepartition,
                        movieNumberEvolution
                );
    }

    public Flow.Publisher<MovieStats> getStatsPublisher() {
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

    public void incrementNumberOfActors() {
        actorCount.incrementAndGet();
        updateAndEmitStats();
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

    public Uni<Void> updateMoviesByGenreRepartition() {
        return
                Panache.withTransaction(() ->
                        movieRepository.findMoviesByGenreRepartition()
                                .invoke(repartition -> {
                                            moviesByGenreRepartition = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies by genre repartition updated: {}", repartition);
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
                                            movieNumberEvolution = repartition; // ← mise à jour locale
                                            updateAndEmitStats();
                                            log.info("Movies number evolution updated: {}", repartition);
                                        }
                                ).replaceWithVoid()
                );
    }
}
