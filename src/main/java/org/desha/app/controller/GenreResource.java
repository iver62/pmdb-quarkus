package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.Genre;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.GenreService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("genres")
@ApplicationScoped
@Slf4j
public class GenreResource {

    private final GenreService genreService;

    public GenreResource(GenreService genreService) {
        this.genreService = genreService;
    }

    @GET
    public Uni<List<Genre>> get() {
        return Genre.listAll();
    }

    @GET
    @Path("{id}")
    public Uni<Genre> getSingle(Long id) {
        return Genre.findById(id);
    }

    @GET
    @Path("{id}/movies")
    public Uni<Set<Movie>> getMovies(Long id) {
        return
                Genre.findById(id)
                        .map(Genre.class::cast)
                        .chain(genreService::getMovies)
                ;
    }

    @POST
    public Uni<Response> createGenre(Genre genre) {
        return
                Panache
                        .withTransaction(() -> {
                                    genre.setCreationDate(LocalDateTime.now());
                                    genre.setName(StringUtils.capitalize(genre.getName()));
                                    return genre.persist();
                                }
                        )
                        .replaceWith(Response.ok(genre).status(CREATED)::build)
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Genre genre) {
        if (Objects.isNull(genre) || Objects.isNull(genre.getName())) {
            throw new WebApplicationException("Genre name was not set on request.", 422);
        }

        return
                genreService.updateGenre(id, genre)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Panache.withTransaction(() -> Genre.deleteById(id))
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

}
