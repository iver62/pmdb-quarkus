package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.dto.MovieQueryParamsDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.GenreService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("genres")
@ApplicationScoped
public class GenreResource {

    private final GenreService genreService;

    @Inject
    public GenreResource(GenreService genreService) {
        this.genreService = genreService;
    }

    @GET
    @Path("count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        return
                genreService.count(queryParams.getTerm())
                        .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getGenre(@RestPath Long id) {
        return
                genreService.getById(id)
                        .onItem().ifNotNull().transform(genre -> Response.ok(genre).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getGenres(@BeanParam MovieQueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Genre.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Genre.ALLOWED_SORT_FIELDS);

        return
                genreService.getGenres(finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .map(genreDTOS ->
                                genreDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOS).build()
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByGenre(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        return
                genreService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .flatMap(movieList ->
                                genreService.countMovies(id, queryParams.getTerm()).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> createGenre(GenreDTO genreDTO) {
        if (Objects.isNull(genreDTO) || Objects.nonNull(genreDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                genreService.create(genreDTO)
                        .map(genre -> Response.status(CREATED).entity(genre).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> updateGenre(@RestPath Long id, GenreDTO genreDTO) {
        if (Objects.isNull(genreDTO) || Objects.isNull(genreDTO.getName())) {
            throw new WebApplicationException("Genre name was not set on request.", 422);
        }

        return
                genreService.update(id, genreDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> deleteGenre(@RestPath Long id) {
        return
                genreService.deleteGenre(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

}
