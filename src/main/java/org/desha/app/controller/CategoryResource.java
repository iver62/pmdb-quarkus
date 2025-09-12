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
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.domain.dto.MovieQueryParamsDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.CategoryService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("/categories")
@ApplicationScoped
@APIResponse(responseCode = "401", description = "Utilisateur non authentifié")
@APIResponse(responseCode = "403", description = "Accès interdit")
@APIResponse(responseCode = "500", description = "Erreur interne du serveur")
@Tag(name = "Catégories", description = "Opérations liées aux catégories")
public class CategoryResource {

    private final CategoryService categoryService;

    @Inject
    public CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Compter les catégories",
            description = "Retourne le nombre de catégories correspondant au terme de recherche fourni."
    )
    @APIResponse(
            responseCode = "200",
            description = "Nombre de catégories trouvé",
            content = @Content(schema = @Schema(implementation = Long.class))
    )
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        return
                categoryService.count(queryParams.getTerm())
                        .map(aLong -> Response.ok(aLong).build())
                ;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer une catégorie par son ID",
            description = "Retourne les informations d'une catégorie si elle existe dans la base de données."
    )
    @APIResponse(
            responseCode = "200",
            description = "Catégorie trouvée",
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    @APIResponse(responseCode = "400", description = "Identifiant invalide")
    @APIResponse(responseCode = "404", description = "Catégorie non trouvée")
    @Parameter(name = "id", description = "Identifiant de la catégorie", required = true)
    public Uni<Response> getCategory(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        return
                categoryService.getById(id)
                        .map(categoryDTO -> Response.ok(categoryDTO).build())
                ;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer les catégories avec pagination, tri et recherche",
            description = "Retourne la liste des catégories avec prise en charge de la pagination, du tri et de la recherche par nom."
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des catégories trouvée",
            content = @Content(schema = @Schema(implementation = List.class)),
            headers = {
                    @Header(
                            name = "X-Total-Count",
                            description = "Nombre total de catégories correspondant aux critères",
                            schema = @Schema(type = SchemaType.NUMBER)
                    )
            }
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune catégorie trouvée avec les critères fournis",
            headers = {
                    @Header(name = "X-Total-Count",
                            description = "Nombre total de catégories correspondant aux critères",
                            schema = @Schema(type = SchemaType.NUMBER)
                    )
            }
    )
    @APIResponse(
            responseCode = "400",
            description = "Paramètres de tri invalides"
    )
    public Uni<Response> getCategories(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);

        return
                categoryService.getCategories(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(categoryDTOS -> categoryService.count(queryParams.getTerm())
                                .map(aLong ->
                                        categoryDTOS.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, aLong).build()
                                                : Response.ok(categoryDTOS).header(CustomHttpHeaders.X_TOTAL_COUNT, aLong).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer les films par catégorie avec pagination, tri et recherche",
            description = "Retourne la liste des films par catégorie avec prise en charge de la pagination, du tri et de la recherche par titre du film."
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des films par catégorie",
            content = @Content(schema = @Schema(implementation = List.class)),
            headers = @Header(
                    name = "X-Total-Count",
                    description = "Nombre total de films par catégorie correspondant aux critères",
                    schema = @Schema(type = SchemaType.NUMBER)
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun film trouvé"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (paramètres ou ID)"
    )
    @APIResponse(
            responseCode = "404",
            description = "Catégorie non trouvée"
    )
    @Parameter(name = "id", description = "Identifiant de la catégorie", required = true)
    public Uni<Response> getMoviesByCategory(@RestPath @NotNull Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                categoryService.getMoviesByCategory(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(movieList ->
                                categoryService.countMoviesByCategory(id, queryParams.getTerm()).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Créer une nouvelle catégorie",
            description = "Permet de créer une nouvelle catégorie. Le champ `id` ne doit pas être renseigné dans la requête."
    )
    @APIResponse(
            responseCode = "201",
            description = "Catégorie créée avec succès",
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (champ manquant ou ID fourni)"
    )
    @RequestBody(
            description = "Les informations de la catégorie à créer",
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    public Uni<Response> createCategory(@Valid CategoryDTO categoryDTO) {
        if (Objects.isNull(categoryDTO)) {
            throw new BadRequestException("Aucune information sur la catégorie n’a été fournie dans la requête");
        }

        if (Objects.nonNull(categoryDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        return
                categoryService.create(categoryDTO)
                        .map(category -> Response.status(CREATED).entity(category).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Operation(
            summary = "Mettre à jour une catégorie",
            description = """
                    Permet de mettre à jour une catégorie existante à partir de son identifiant.
                    Le champ `id` dans le corps de la requête doit correspondre à celui de l'URL.
                    """
    )
    @APIResponse(
            responseCode = "200",
            description = "Catégorie mise à jour avec succès",
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (nom manquant ou corps absent)"
    )
    @APIResponse(
            responseCode = "404",
            description = "Catégorie non trouvée"
    )
    @APIResponse(
            responseCode = "422",
            description = "Identifiant du corps de la requête différent de celui de l'URL"
    )
    @Parameter(name = "id", description = "Identifiant de la catégorie", required = true)
    @RequestBody(
            description = "Informations de la catégorie à mettre à jour",
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    public Uni<Response> updateCategory(@RestPath Long id, @Valid CategoryDTO categoryDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        if (Objects.isNull(categoryDTO)) {
            throw new BadRequestException("Aucune information sur la catégorie n’a été fournie dans la requête");
        }

        if (!Objects.equals(id, categoryDTO.getId())) {
            throw new WebApplicationException("L'identifiant de la catégorie ne correspond pas à celui de la requête", 422);
        }

        return
                categoryService.update(id, categoryDTO)
                        .map(category -> Response.ok(category).build())
                ;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Operation(
            summary = "Supprimer une catégorie",
            description = """
                    Supprime une catégorie à partir de son identifiant.
                    Retourne 204 si la suppression a réussi, ou 404 si la catégorie n'existe pas.
                    """
    )
    @APIResponse(
            responseCode = "204",
            description = "Catégorie supprimée avec succès"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Catégorie non trouvée"
    )
    @Parameter(name = "id", description = "Identifiant de la catégorie", required = true)
    public Uni<Response> deleteCategory(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                categoryService.deleteCategory(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build()
                        )
                ;
    }

}
