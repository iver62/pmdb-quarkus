package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Path("admin")
@RolesAllowed("admin")
@ApplicationScoped
@Slf4j
public class AdminResource {

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    private final Keycloak keycloak;

    @Inject
    public AdminResource(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @GET
    @Path("/users/{id}/roles")
    public List<RoleRepresentation> getUserRoles(@RestPath("id") String userId) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        return userResource.roles().realmLevel().listAll(); // ou .getAll() pour composites
    }

    @GET
    @Path("/roles")
    public List<RoleRepresentation> getRealmRoles() {
        List<String> excludedRoles = List.of("offline_access", "uma_authorization", "default-roles-" + realm);

        return
                keycloak.realm(realm).roles().list()
                        .stream()
                        .filter(role -> !excludedRoles.contains(role.getName()))
                        .sorted(Comparator.comparing(RoleRepresentation::getName))
                        .toList();
    }

    @POST
    @Path("/users/{id}/reset-password")
    public Uni<Response> sendResetPasswordEmail(@RestPath String id) {
        return Uni.createFrom().item(() -> {
                    List<String> actions = List.of("UPDATE_PASSWORD");

                    keycloak.realm(realm)
                            .users()
                            .get(id)
                            .executeActionsEmail(actions);

                    return Response.noContent().build();
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor()); // déplacer le blocage hors du thread IO
//                .onFailure().transform(t -> new WebApplicationException("Erreur lors de l'envoi de l'email de réinitialisation", t));
    }

    @PUT
    @Path("/users/{id}")
    public Uni<Response> updateUser(@RestPath("id") String userId, UserRepresentation userRepresentation) {
        return
                Uni.createFrom().item(() -> {
                            UserResource userResource = keycloak.realm(realm).users().get(userId);

                            UserRepresentation existingUser = userResource.toRepresentation();
                            existingUser.setUsername(userRepresentation.getUsername());
                            existingUser.setFirstName(userRepresentation.getFirstName());
                            existingUser.setLastName(userRepresentation.getLastName());
                            existingUser.setEmail(userRepresentation.getEmail());
                            existingUser.setEmailVerified(userRepresentation.isEmailVerified());

                            userResource.update(existingUser);
                            return existingUser;
                        })
                        .runSubscriptionOn(Infrastructure.getDefaultExecutor()) // déplacer le blocage hors du thread IO
                        .onItem().transform(user -> Response.ok(user).build())
                        .onFailure().transform(t -> new WebApplicationException("Erreur lors de la mise à jour de l'utilisateur", t))
                ;
    }

    @PUT
    @Path("/users/{id}/roles")
    public Uni<Response> updateRoles(@RestPath("id") String userId, List<RoleRepresentation> newRoles) {
        return
                Uni.createFrom().item(() -> {
                            UserResource userResource = keycloak.realm(realm).users().get(userId);

                            List<String> excludedRoles = List.of("offline_access", "uma_authorization", "default-roles-" + realm);

                            // Récupérer tous les rôles actuellement assignés
                            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll()
                                    .stream()
                                    .filter(role -> !excludedRoles.contains(role.getName()))
                                    .toList();

                            // Retirer tous les rôles actuels
                            if (!currentRoles.isEmpty()) {
                                userResource.roles().realmLevel().remove(currentRoles);
                            }

                            // Ajouter les nouveaux rôles
                            if (Objects.nonNull(newRoles) && !newRoles.isEmpty()) {
                                userResource.roles().realmLevel().add(newRoles);
                            }

                            return userResource.roles().realmLevel().listAll();
                        })
                        .runSubscriptionOn(Infrastructure.getDefaultExecutor()) // déplacer le blocage hors du thread IO
                        .onItem().transform(user -> Response.ok(user).build())
                        .onFailure().transform(t -> new WebApplicationException("Erreur lors de la mise à jour des rôles de l'utilisateur", t))
                ;
    }

    @DELETE
    @Path("/users/{id}")
    public Uni<Response> deleteUser(@RestPath("id") String userId) {
        return
                Uni.createFrom().item(() -> {
                            UserResource userResource = keycloak.realm(realm).users().get(userId);
                            userResource.remove();
                            return Response.noContent().build(); // HTTP 204 No Content
                        })
                        .runSubscriptionOn(Infrastructure.getDefaultExecutor()) // déplacer le blocage hors du thread IO
                        .onFailure().transform(t -> new WebApplicationException("Erreur lors de la suppression de l'utilisateur", t));
    }

}
