package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.GenreService;
import org.jboss.resteasy.reactive.RestPath;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
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
    @Path("count")
    public Uni<Response> count(@QueryParam("name") @DefaultValue("") String name) {
        return
                Genre.count(name)
                        .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("{id}")
    public Uni<Response> getGenre(Long id) {
        return
                Genre.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .onItem().ifNotNull().transform(genre -> Response.ok(genre).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    public Uni<Response> getGenres(
            @QueryParam("sort") @DefaultValue("name") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Genre.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                genreService.getGenres(sort, sortDirection, term)
                        .map(genreDTOS ->
                                genreDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOS).build()
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMoviesByGenre(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Movie.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                genreService.getMovies(id, Page.of(pageIndex, size), sort, sortDirection, term)
                        .flatMap(movieList ->
                                genreService.countMovies(id, term).map(total ->
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
    public Uni<Response> createGenre(GenreDTO genreDTO) {
        if (Objects.isNull(genreDTO) || Objects.nonNull(genreDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                Genre.create(genreDTO)
                        .map(genre -> Response.status(CREATED).entity(genre).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateGenre(Long id, GenreDTO genreDTO) {
        if (Objects.isNull(genreDTO) || Objects.isNull(genreDTO.getName())) {
            throw new WebApplicationException("Genre name was not set on request.", 422);
        }

        return
                Genre.update(id, genreDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteGenre(@RestPath Long id) {
        return
                Genre.deleteGenre(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    private Sort.Direction validateSortDirection(String direction) {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

    private Uni<Response> validateSortField(String sort, List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sort)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(MessageFormat.format("Le champ de tri \"{0}\" est invalide. Valeurs autorisées : {1}", sort, allowedSortFields))
                            .build()
            );
        }
        return null;
    }

}
