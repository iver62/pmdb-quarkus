package org.desha.app.webservices;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.services.CountryService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("countries")
@ApplicationScoped
@Slf4j
public class CountryResource {

    private final CountryService countryService;

    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    public Uni<List<Country>> get() {
        return Country.listAll();
    }

    @GET
    @Path("{id}")
    public Uni<Country> getSingle(Long id) {
        return Country.findById(id);
    }

    @GET
    @Path("{id}/movies")
    public Uni<Set<Movie>> getMovies(Long id) {
        return
                Country.findById(id)
                        .map(panacheEntityBase -> (Country) panacheEntityBase)
                        .chain(countryService::getMovies)
                ;
    }

    @GET
    @Path("{id}/persons")
    public Uni<Set<Person>> getPersons(Long id) {
        return
                Person.findById(id)
                        .map(Country.class::cast)
                        .chain(countryService::getPersons)
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
