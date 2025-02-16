package org.desha.app.controller;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.entity.Genre;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("genres")
@ApplicationScoped
@Slf4j
public class GenreResource {

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
    public Uni<Response> getGenres() {
        return
                Genre.getAll()
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMoviesByGenre(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("title") @DefaultValue("") String title
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Genre.getMovies(id, page, size, sort, sortDirection, title)
                        .flatMap(movieList ->
                                Genre.countMovies(id, title).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(movieList).header("X-Total-Count", total).build()
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

}
