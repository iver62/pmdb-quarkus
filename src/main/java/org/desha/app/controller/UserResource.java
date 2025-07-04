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
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.domain.entity.User;
import org.desha.app.service.UserService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Optional;
import java.util.UUID;

@Path("users")
@Singleton
@RolesAllowed("admin")
@Slf4j
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    /**
     * Récupère un utilisateur à partir de son identifiant unique (UUID).
     *
     * <p>Retourne les informations de l'utilisateur correspondant à l'ID fourni sous forme de réponse HTTP 200.</p>
     * <p>Si aucun utilisateur n'est trouvé, une réponse vide est renvoyée avec un code HTTP 204 (No Content).</p>
     * <p>En cas d'erreur lors de la récupération, une réponse avec un code HTTP 500 (Internal Server Error)
     * contenant le message d'erreur est renvoyée.</p>
     *
     * @param id identifiant unique de l'utilisateur
     * @return un {@link Uni} contenant la réponse HTTP
     */
    @GET
    @Path("/{id}")
    public Uni<Response> getUser(@RestPath UUID id) {
        return
                userService.getUser(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération de l'utilisateur: {}", err.getMessage());
                                    return
                                            Response.serverError()
                                                    .entity("Erreur lors de la récupération de l'utilisateur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée d'utilisateurs selon les critères fournis (pagination, tri, recherche).
     *
     * <p>Le tri est effectué selon le champ spécifié dans les paramètres de requête. Si aucun champ n’est précisé,
     * un tri par défaut est appliqué. Si le champ de tri n'est pas autorisé, une exception est levée.</p>
     *
     * <p>Retourne une réponse HTTP 200 avec la liste des utilisateurs et un en-tête {@code X-Total-Count}
     * indiquant le nombre total d'utilisateurs correspondant à la recherche.</p>
     *
     * <p>Retourne une réponse HTTP 204 (No Content) si aucun utilisateur ne correspond aux critères.</p>
     *
     * <p>En cas d'erreur lors du traitement, retourne une réponse HTTP 500 avec le message d’erreur.</p>
     *
     * @param queryParams les paramètres de requête pour la pagination, le tri et la recherche
     * @return une réponse HTTP contenant la liste des utilisateurs ou une erreur
     */
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
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des utilisateurs: {}", err.getMessage());
                                    return
                                            Response.serverError()
                                                    .entity("Erreur lors de la récupération des utilisateurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Récupère tous les utilisateurs selon les critères de tri et de recherche fournis.
     *
     * <p>Cette méthode permet de récupérer une liste complète d'utilisateurs (sans pagination),
     * triée selon le champ spécifié dans les paramètres. Si aucun champ de tri n'est fourni,
     * un champ de tri par défaut est utilisé.</p>
     *
     * <p>Si le champ de tri fourni n'est pas valide, une exception est levée.</p>
     *
     * <p>Retourne une réponse HTTP 200 avec la liste des utilisateurs si des résultats sont trouvés,
     * ou HTTP 204 (No Content) si la liste est vide.</p>
     *
     * <p>En cas d’erreur lors du traitement, retourne une réponse HTTP 500 avec un message d’erreur détaillé.</p>
     *
     * @param queryParams les paramètres de requête contenant le champ de tri, la direction du tri, et un terme de recherche optionnel
     * @return une réponse HTTP contenant la liste des utilisateurs ou un code de statut approprié
     */
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
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des utilisateurs: {}", err.getMessage());
                                    return
                                            Response.serverError()
                                                    .entity("Erreur lors de la récupération des utilisateurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

}
