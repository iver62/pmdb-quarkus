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
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("countries")
@ApplicationScoped
public class CountryResource {

    private final CountryService countryService;

    @Inject
    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountry(@RestPath Long id) {
        return
                countryService.getById(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String finalLang = queryParams.validateLang();

        return
                countryService.getCountries(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm(), finalLang)
                        .flatMap(countryList ->
                                countryService.countCountries(queryParams.getTerm(), finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
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
    @Path("{id}/full")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getFullCountry(Long id) {
        return
                countryService.getFull(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("search")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> searchCountriesByName(@QueryParam("query") String query) {
        if (Objects.isNull(query) || query.trim().isEmpty()) {
            throw new BadRequestException("Le paramètre 'query' est requis");
        }

        return
                countryService.searchByName(query)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    @GET
    @Path("{id}/movies/all")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAllMoviesByCountry(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        return
                countryService.getMovies(id, queryParams.getSort(), queryParams.validateSortDirection(), queryParams.getTerm())
                        .flatMap(movieList ->
                                countryService.countMovies(id, queryParams.getTerm()).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByCountry(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        String finalSort = Optional.of(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        return
                countryService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .flatMap(movieList ->
                                countryService.countMovies(id, queryParams.getTerm()).map(total ->
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

    /**
     * Récupère la liste des producteurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les producteurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des producteurs associés au pays.
     * - 204 OK si liste des producteurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     */
    @GET
    @Path("/{id}/persons")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
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
        if (Objects.isNull(countryDTO) || Objects.isNull(countryDTO.getNomFrFr())) {
            throw new WebApplicationException("Country name was not set on request.", 422);
        }

        return
                countryService.update(id, countryDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

}
