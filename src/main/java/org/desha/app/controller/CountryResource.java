package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.service.CountryService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Path("countries")
@ApplicationScoped
public class CountryResource {

    private final CountryService countryService;

    @Inject
    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    @Path("/count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        String finalLang = queryParams.validateLang();

        return
                countryService.countCountries(queryParams.getTerm(), finalLang)
                        .map(aLong -> Response.ok(aLong).build())
                ;
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountry(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        return
                countryService.getById(id)
                        .map(country -> Response.ok(country).build())
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String finalLang = queryParams.validateLang();

        return
                countryService.getCountries(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm(), finalLang)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(countryDTOList ->
                                countryService.countCountries(queryParams.getTerm(), finalLang).map(total ->
                                        countryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("all")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAllCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);

        return
                countryService.getCountries(finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(countryList ->
                                countryService.countCountries(queryParams.getTerm(), queryParams.validateLang()).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByCountry(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getMoviesByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(movieList ->
                                countryService.countMoviesByCountry(id, queryParams.getTerm()).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/persons")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(personDTOList ->
                                countryService.countPersonsByCountry(id, criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> update(@RestPath Long id, CountryDTO countryDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        if (Objects.isNull(countryDTO)) {
            throw new BadRequestException("Aucune information sur le pays n’a été fournie dans la requête");
        }

        if (!Objects.equals(id, countryDTO.getId())) {
            throw new WebApplicationException("L'identifiant du pays ne correspond pas à celui de la requête", 422);
        }

        return
                countryService.update(id, countryDTO)
                        .map(entity -> Response.ok(entity).build())
                ;
    }

}
