package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.service.GenreService;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("genres")
@ApplicationScoped
@Slf4j
public class GenreResource {

    private final GenreService genreService;

    @Inject
    public GenreResource(GenreService genreService) {
        this.genreService = genreService;
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return genreService.getOne(id)
                .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    public Uni<Response> get() {
        return genreService.getAll()
                .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(Long id) {
        return
                genreService.getMovies(id)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @POST
    public Uni<Response> createGenre(GenreDTO genreDTO) {
        if (Objects.isNull(genreDTO) || Objects.nonNull(genreDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                genreService.createGenre(genreDTO)
                        .map(genre -> Response.status(CREATED).entity(genre).build())
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
