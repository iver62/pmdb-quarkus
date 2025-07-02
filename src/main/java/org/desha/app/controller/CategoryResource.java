package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieQueryParamsDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.CategoryService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("categories")
@ApplicationScoped
public class CategoryResource {

    private final CategoryService categoryService;

    @Inject
    public CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    @Path("count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        return
                categoryService.count(queryParams.getTerm())
                        .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategory(@RestPath Long id) {
        return
                categoryService.getById(id)
                        .onItem().ifNotNull().transform(category -> Response.ok(category).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategories(@BeanParam MovieQueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);

        return
                categoryService.getCategories(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
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
    @Path("{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByCategory(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                categoryService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(movieList ->
                                categoryService.countMovies(id, queryParams.getTerm()).map(total ->
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

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> createCategory(CategoryDTO categoryDTO) {
        if (Objects.isNull(categoryDTO) || Objects.nonNull(categoryDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                categoryService.create(categoryDTO)
                        .map(category -> Response.status(CREATED).entity(category).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> updateCategory(@RestPath Long id, CategoryDTO categoryDTO) {
        if (Objects.isNull(categoryDTO) || Objects.isNull(categoryDTO.getName())) {
            throw new WebApplicationException("Category name was not set on request.", 422);
        }

        return
                categoryService.update(id, categoryDTO)
                        .onItem().ifNotNull().transform(category -> Response.ok(category).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> deleteCategory(@RestPath Long id) {
        return
                categoryService.deleteCategory(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build())
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

}
