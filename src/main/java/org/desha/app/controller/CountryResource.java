package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.service.CountryService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Path("countries")
@ApplicationScoped
@APIResponses(value = {
        @APIResponse(
                responseCode = "401",
                description = "Utilisateur non authentifié"
        ),
        @APIResponse(
                responseCode = "403",
                description = "Accès interdit"
        ),
        @APIResponse(
                responseCode = "500",
                description = "Erreur interne du serveur"
        )
})
@Tag(name = "Pays", description = "Opérations liées aux pays")
public class CountryResource {

    private final CountryService countryService;

    @Inject
    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    @Path("/count")
    @Operation(
            summary = "Compter les pays",
            description = "Retourne le nombre total de pays correspondant aux critères de recherche"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Nombre de pays correspondant aux critères",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides"
            )
    })
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        String finalLang = queryParams.validateLang();

        return
                countryService.countCountries(queryParams.getTerm(), finalLang)
                        .map(aLong -> Response.ok(aLong).build())
                ;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer un pays par son ID",
            description = "Retourne les détails du pays correspondant à l'identifiant fourni."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Le pays a été trouvé",
                    content = @Content(schema = @Schema(implementation = CountryDTO.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "ID invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Aucun pays trouvé pour cet ID"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique du pays", in = ParameterIn.PATH, required = true, example = "42")
    public Uni<Response> getCountry(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        return
                countryService.getById(id)
                        .map(country -> Response.ok(country).build())
                ;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lister les pays",
            description = "Retourne une liste paginée de pays avec possibilité de filtrer, trier et rechercher par nom."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays trouvée",
                    content = @Content(schema = @Schema(implementation = CountryDTO.class))
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays trouvé"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides"
            )
    })
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère les films associés à un pays",
            description = "Retourne une liste paginée de films associés à un pays donné, avec filtres facultatifs (terme, tri, pagination)."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des films trouvés",
                    content = @Content(schema = @Schema(implementation = MovieDTO.class))
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun film trouvé"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Pays non trouvé"
            )
    })
    @Parameter(name = "id", description = "Identifiant du pays", required = true, in = ParameterIn.PATH)
    public Uni<Response> getMoviesByCountry(@RestPath @NotNull Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                countryService.getMoviesByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère les personnes associées à un pays",
            description = "Retourne une liste paginée de personnes associées à un pays donné, avec filtres facultatifs (terme, tri, pagination)."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des personnes trouvés",
                    content = @Content(schema = @Schema(implementation = PersonDTO.class))
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune personne trouvée"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Pays non trouvé"
            )
    })
    @Parameter(name = "id", description = "Identifiant du pays", required = true, in = ParameterIn.PATH)
    public Uni<Response> getPersonsByCountry(@RestPath @NotNull Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_COUNTRY_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                countryService.getPersonsByCountry(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(personDTOList ->
                                countryService.countPersonsByCountry(id, criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour un pays",
            description = "Met à jour les informations d'un pays existant en fonction de son identifiant."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Pays mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = CountryDTO.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Pays non trouvé"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "L'identifiant du pays ne correspond pas à celui de la requête"
            )
    })
    @Parameter(name = "id", description = "Identifiant du pays à mettre à jour", required = true, in = ParameterIn.PATH)
    public Uni<Response> update(@RestPath @NotNull Long id, @Valid CountryDTO countryDTO) {
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
