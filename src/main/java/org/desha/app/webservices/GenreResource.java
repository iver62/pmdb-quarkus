package org.desha.app.webservices;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Genre;
import org.desha.app.domain.Movie;
import org.desha.app.services.GenreService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

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
                        .map(panacheEntityBase -> (Genre) panacheEntityBase)
                        .chain(genreService::getMovies)
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


}
