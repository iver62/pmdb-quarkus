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

    /**
     * Récupère la liste des acteurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les acteurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des acteurs associés au pays.
     * - 204 OK si liste des acteurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/actors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getActorsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, actorService, actorRepository, Actor.class)
                        .flatMap(personDTOList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, actorRepository, Actor.class).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
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
     *//*
    @GET
    @Path("{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getProducersByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, producerService, producerRepository, Producer.class)
                        .flatMap(personDTOList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, producerRepository, Producer.class).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des réalisateurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les réalisateurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des réalisateurs associés au pays.
     * - 204 OK si liste des réalisateurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getDirectorsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, directorService, directorRepository, Director.class)
                        .flatMap(directorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, directorRepository, Director.class).map(total ->
                                        directorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(directorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des scénaristes associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les scénaristes.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des scénaristes associés au pays.
     * - 204 OK si liste des scénaristes associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getScreenwritersByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, screenwriterService, screenwriterRepository, Screenwriter.class)
                        .flatMap(screenwriterList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, screenwriterRepository, Screenwriter.class).map(total ->
                                        screenwriterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(screenwriterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des musiciens associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les musiciens.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des musiciens associés au pays.
     * - 204 OK si liste des musiciens associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMusiciansByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, musicianService, musicianRepository, Musician.class)
                        .flatMap(musicianList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, musicianRepository, Musician.class).map(total ->
                                        musicianList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(musicianList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des décorateurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les décorateurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des décorateurs associés au pays.
     * - 204 OK si liste des décorateurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/decorators")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getDecoratorsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, decoratorService, decoratorRepository, Decorator.class)
                        .flatMap(decoratorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, decoratorRepository, Decorator.class).map(total ->
                                        decoratorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(decoratorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des costumiers associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les costumiers.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des costumiers associés au pays.
     * - 204 OK si liste des costumiers associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/costumiers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCostumiers(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, costumierService, costumierRepository, Costumier.class)
                        .flatMap(costumierList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, costumierRepository, Costumier.class).map(total ->
                                        costumierList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(costumierList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des photographes associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les photographes.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des photographes associés au pays.
     * - 204 OK si liste des photographes associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPhotographersByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, photographerService, photographerRepository, Photographer.class)
                        .flatMap(photographerList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, photographerRepository, Photographer.class).map(total ->
                                        photographerList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(photographerList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des monteurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les monteurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des monteurs associés au pays.
     * - 204 OK si liste des monteurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getEditorsByCountry(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, editorService, editorRepository, Editor.class)
                        .flatMap(editorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, editorRepository, Editor.class).map(total ->
                                        editorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(editorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des casteurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les casteurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des casteurs associés au pays.
     * - 204 OK si liste des casteurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCasters(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, casterService, casterRepository, Caster.class)
                        .flatMap(casterList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, casterRepository, Caster.class).map(total ->
                                        casterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(casterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des directeurs artistiques associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les directeurs artistiques.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des directeurs artistiques associés au pays.
     * - 204 OK si liste des directeurs artistiques associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/art-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getArtDirectors(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, artDirectorService, artDirectorRepository, ArtDirector.class)
                        .flatMap(artDirectorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, artDirectorRepository, ArtDirector.class).map(total ->
                                        artDirectorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(artDirectorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des ingénieurs du son associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les ingénieurs du son.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des ingénieurs du son associés au pays.
     * - 204 OK si liste des ingénieurs du son associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSoundEditors(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, soundEditorService, soundEditorRepository, SoundEditor.class)
                        .flatMap(soundEditorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, soundEditorRepository, SoundEditor.class).map(total ->
                                        soundEditorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(soundEditorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des spécialistes des effets spéciaux associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les spécialistes des effets spéciaux.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des spécialistes des effets spéciaux associés au pays.
     * - 204 OK si liste des spécialistes des effets spéciaux associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/visual-effects-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getVisualEffectsSupervisors(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, visualEffectsSupervisorService, visualEffectsSupervisorRepository, VisualEffectsSupervisor.class)
                        .flatMap(visualEffectsSupervisorList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, visualEffectsSupervisorRepository, VisualEffectsSupervisor.class).map(total ->
                                        visualEffectsSupervisorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(visualEffectsSupervisorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des maquilleurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les maquilleurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des maquilleurs associés au pays.
     * - 204 OK si liste des maquilleurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMakeupArtists(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, makeupArtistService, makeupArtistRepository, MakeupArtist.class)
                        .flatMap(makeupArtistList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, makeupArtistRepository, MakeupArtist.class).map(total ->
                                        makeupArtistList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(makeupArtistList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    *//**
     * Récupère la liste des coiffeurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les coiffeurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des coiffeurs associés au pays.
     * - 204 OK si liste des coiffeurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getHairDressers(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, hairDresserService, hairDresserRepository, HairDresser.class)
                        .flatMap(hairDresserList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, hairDresserRepository, HairDresser.class).map(total ->
                                        hairDresserList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(hairDresserList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    */

    /**
     * Récupère la liste des cascadeurs associés à un pays donné, avec prise en charge
     * de la pagination, du tri et des filtres définis dans les paramètres de requête.
     *
     * @param id          L'identifiant du pays pour lequel récupérer les cascadeurs.
     * @param queryParams Paramètres de requête contenant la pagination, le tri et les critères de recherche.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste des cascadeurs associés au pays.
     * - 204 OK si liste des cascadeurs associés au pays est vide.
     * @throws org.desha.app.exception.InvalidDateException Si la plage de dates spécifiée dans les paramètres de requête est incohérente, par exemple si la date de début
     *                                                      est après la date de fin. Cette exception est lancée par la méthode {@link PersonQueryParamsDTO#isInvalidDateRange()}.
     * @throws org.desha.app.exception.InvalidSortException Si le champ de tri spécifié dans les paramètres de requête est invalide. Cette exception est lancée par
     *                                                      la méthode {@link QueryParamsDTO#validateSortField(String, List)}.
     *//*
    @GET
    @Path("{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getStuntmen(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO, stuntmanService, stuntmanRepository, Stuntman.class)
                        .flatMap(stuntmanList ->
                                countryService.countPersonsByCountry(id, criteriasDTO, stuntmanRepository, Stuntman.class).map(total ->
                                        stuntmanList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(stuntmanList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/
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
