package org.desha.app.controller;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import org.desha.app.domain.record.CountryRepartition;
import org.desha.app.domain.record.Repartition;
import org.desha.app.service.StatsService;

@Path("/stats")
@ApplicationScoped
public class StatsResource {

    private final Sse sse;
    private final StatsService statsService;

    @Inject
    public StatsResource(StatsService statsService, Sse sse) {
        this.statsService = statsService;
        this.sse = sse;
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> stream() {
        return
                Multi.createFrom().publisher(statsService.getStatsPublisher())
                        .flatMap(stats ->
                                Multi.createFrom().items(
                                        sse.newEventBuilder().name("movie-count").mediaType(MediaType.APPLICATION_JSON_TYPE).data(stats.getMovieCount()).build(),
                                        sse.newEventBuilder().name("actor-count").mediaType(MediaType.APPLICATION_JSON_TYPE).data(stats.getActorCount()).build(),
                                        sse.newEventBuilder().name("movies-by-release-date").mediaType(MediaType.APPLICATION_JSON_TYPE).data(Repartition.class, stats.getByReleaseDate()).build(),
                                        sse.newEventBuilder().name("movies-by-country").mediaType(MediaType.APPLICATION_JSON_TYPE).data(CountryRepartition.class, stats.getByCountry()).build(),
                                        sse.newEventBuilder().name("movies-by-category").mediaType(MediaType.APPLICATION_JSON_TYPE).data(Repartition.class, stats.getByGenre()).build(),
                                        sse.newEventBuilder().name("movies-by-user").mediaType(MediaType.APPLICATION_JSON_TYPE).data(Repartition.class, stats.getByUser()).build(),
                                        sse.newEventBuilder().name("movies-number-by-creation-date").mediaType(MediaType.APPLICATION_JSON_TYPE).data(Repartition.class, stats.getByCreationDate()).build(),
                                        sse.newEventBuilder().name("movies-number-evolution").mediaType(MediaType.APPLICATION_JSON_TYPE).data(Repartition.class, stats.getNumberEvolution()).build()
                                )
                        )
                ;
    }
}
