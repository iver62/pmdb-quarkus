package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieQueryParamsDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.entity.Movie;
import org.desha.app.service.CategoryService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("/categories")
@ApplicationScoped
@Slf4j
public class CategoryResource {

    private final CategoryService categoryService;

    @Inject
    public CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    @Path("/count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> count(@BeanParam QueryParamsDTO queryParams) {
        return
                categoryService.count(queryParams.getTerm())
                        .map(aLong -> Response.ok(aLong).build())
                ;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategory(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        return
                categoryService.getById(id)
                        .map(categoryDTO -> Response.ok(categoryDTO).build())
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategories(@BeanParam MovieQueryParamsDTO queryParams) {
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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByCategory(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                categoryService.getMoviesByCategory(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
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
    public Uni<Response> createCategory(@Valid CategoryDTO categoryDTO) {
        if (Objects.nonNull(categoryDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        if (StringUtils.isBlank(categoryDTO.getName())) {
            throw new BadRequestException("Le nom de la catégorie n’a pas été fourni dans la requête");
        }

        return
                categoryService.create(categoryDTO)
                        .map(category -> Response.status(CREATED).entity(category).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> updateCategory(@RestPath Long id, CategoryDTO categoryDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_CATEGORY_ID);

        if (Objects.isNull(categoryDTO)) {
            throw new BadRequestException("Aucune information sur la catégorie n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(categoryDTO.getName())) {
            throw new BadRequestException("Le nom de la catégorie n’a pas été fourni dans la requête");
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
    @RolesAllowed("admin")
    public Uni<Response> deleteCategory(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                categoryService.deleteCategory(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build())
                ;
    }

}
