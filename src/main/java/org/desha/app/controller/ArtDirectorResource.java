package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.ArtDirector;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("art-directors")
@ApplicationScoped
@Slf4j
public class ArtDirectorResource {

    private final PersonService<ArtDirector> artDirectorService;

    @Inject
    public ArtDirectorResource(@PersonType(Role.ART_DIRECTOR) PersonService<ArtDirector> artDirectorService) {
        this.artDirectorService = artDirectorService;
    }

    @GET
    @Path("{id}")
    public Uni<ArtDirector> getArtDirector(Long id) {
        return artDirectorService.getOne(id);
    }

    @GET
    public Uni<Response> getArtDirectors() {
        return
                artDirectorService.getAll()
                        .onItem().ifNotNull().transform(artDirectors -> Response.ok(artDirectors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(Long id) {
        return
                artDirectorService.getOne(id)
                        .chain(artDirectorService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/countries")
    public Uni<Response> getCountries(Long id) {
        return
                artDirectorService.getOne(id)
                        .chain(artDirectorService::getCountries)
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    /*@GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getAwards)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }*/

    @POST
    public Uni<Response> save(PersonDTO personDTO) {
        return
                artDirectorService
                        .save(personDTO, ArtDirector.builder().build())
                        .onItem().ifNotNull()
                        .transform(artDirector -> Response.ok(artDirector).status(CREATED).build())
                ;
    }

    /*@PUT
    @Path("{id}/countries")
    public Uni<Response> addCountries(Long id, Set<Country> countrySet) {
        Set<Country> countries = new HashSet<>();
        return
                Uni.join().all(
                                countrySet.stream().filter(c -> Objects.nonNull(c.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        countrySet
                                                .stream()
                                                .filter(c -> Objects.nonNull(c.id))
                                                .map(c -> Country.findById(c.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Country) e).toList())
                        .map(countryList -> countryList.stream().collect(Collectors.toCollection(() -> countries)))
                        .map(countryList -> countrySet.stream().filter(c -> Objects.isNull(c.id)).collect(Collectors.toCollection(() -> countries)))
                        .chain(countryList -> personService.addCountries(id, countrySet))
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                ;
    }*/

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, PersonDTO personDTO) {
        if (Objects.isNull(personDTO) || Objects.isNull(personDTO.getName())) {
            throw new WebApplicationException("Person name was not set on request.", 422);
        }

        return
                artDirectorService
                        .update(id, personDTO)
                        .onItem().ifNotNull().transform(artDirector -> Response.ok(artDirector).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return
                artDirectorService.delete(id)
                        .map(deleted -> deleted
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build())
                ;
    }

}
