package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Country;
import org.desha.app.service.CountryService;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("countries")
@ApplicationScoped
@Slf4j
public class CountryResource {

    private final CountryService countryService;

    @Inject
    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return
                countryService.getOne(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    public Uni<Response> get() {
        return
                countryService.getAll()
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @Path("{id}/full")
    public Uni<Response> getFullCountry(Long id) {
        return
                countryService.getFull(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/producers")
    public Uni<Response> getProducers(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getProducers)
                        .onItem().ifNotNull().transform(producers -> Response.ok(producers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/directors")
    public Uni<Response> getDirectors(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getDirectors)
                        .onItem().ifNotNull().transform(directors -> Response.ok(directors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/screenwriters")
    public Uni<Response> getScreenwriters(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getScreenwriters)
                        .onItem().ifNotNull().transform(screenwriters -> Response.ok(screenwriters).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/musicians")
    public Uni<Response> getMusicians(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getMusicians)
                        .onItem().ifNotNull().transform(musicians -> Response.ok(musicians).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/photographers")
    public Uni<Response> getPhotographers(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getPhotographers)
                        .onItem().ifNotNull().transform(photographers -> Response.ok(photographers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/costumiers")
    public Uni<Response> getCostumiers(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getCostumiers)
                        .onItem().ifNotNull().transform(costumiers -> Response.ok(costumiers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/decorators")
    public Uni<Response> getDecorators(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getDecorators)
                        .onItem().ifNotNull().transform(decorators -> Response.ok(decorators).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/editors")
    public Uni<Response> getEditors(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getEditors)
                        .onItem().ifNotNull().transform(editors -> Response.ok(editors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/casters")
    public Uni<Response> getCasters(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getCasters)
                        .onItem().ifNotNull().transform(casters -> Response.ok(casters).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/art-directors")
    public Uni<Response> getArtDirectors(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getArtDirectors)
                        .onItem().ifNotNull().transform(artDirectors -> Response.ok(artDirectors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/sound-editors")
    public Uni<Response> getSoundEditors(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getSoundEditors)
                        .onItem().ifNotNull().transform(soundEditors -> Response.ok(soundEditors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> getVisualEffectsSupervisors(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getVisualEffectsSupervisors)
                        .onItem().ifNotNull().transform(visualEffectsSupervisors -> Response.ok(visualEffectsSupervisors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/makeup-artists")
    public Uni<Response> getMakeupArtists(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getMakeupArtists)
                        .onItem().ifNotNull().transform(makeupArtists -> Response.ok(makeupArtists).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/hair-dressers")
    public Uni<Response> getHairDressers(Long id) {
        return
                countryService.getOne(id)
                        .chain(countryService::getHairDressers)
                        .onItem().ifNotNull().transform(hairDressers -> Response.ok(hairDressers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Country country) {
        if (Objects.isNull(country) || Objects.isNull(country.getAlpha2())) {
            throw new WebApplicationException("Country name was not set on request.", 422);
        }

        return
                countryService.updateCountry(id, country)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

}
