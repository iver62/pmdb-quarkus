package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.User;
import org.desha.app.service.UserService;

import java.util.Optional;

@Path("users")
@Singleton
@RolesAllowed("admin")
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    public Uni<Response> getUsers(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(User.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, User.ALLOWED_SORT_FIELDS);

        return
                userService.getUsers(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .flatMap(userList ->
                                userService.countUsers(queryParams.getTerm())
                                        .map(total ->
                                                userList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(userList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                ;
    }

    @GET
    @Path("all")
    public Uni<Response> getAllUsers(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(User.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, User.ALLOWED_SORT_FIELDS);

        return
                userService.getUsers(finalSort, queryParams.validateSortDirection(), queryParams.getTerm())
                        .map(userList ->
                                userList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(userList).build()
                        )
                ;
    }

}
