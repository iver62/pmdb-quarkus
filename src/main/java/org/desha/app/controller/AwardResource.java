package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.service.AwardService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("/awards")
@ApplicationScoped
@APIResponse(responseCode = "401", description = "Utilisateur non authentifié")
@APIResponse(responseCode = "403", description = "Accès interdit")
@APIResponse(responseCode = "500", description = "Erreur interne du serveur")
@Tag(name = "Récompenses", description = "Opérations liées aux récompenses")
public class AwardResource {

    private final AwardService awardService;

    @Inject
    public AwardResource(AwardService awardService) {
        this.awardService = awardService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer une récompense par son ID",
            description = "Retourne les informations d'une récompense si elle existe dans la base de données."
    )
    @APIResponse(
            responseCode = "200",
            description = "Récompense trouvée",
            content = @Content(schema = @Schema(implementation = AwardDTO.class))
    )
    @APIResponse(responseCode = "400", description = "Identifiant invalide")
    @APIResponse(responseCode = "404", description = "Récompense non trouvée")
    @Parameter(name = "id", description = "Identifiant de la récompense", required = true, in = ParameterIn.PATH)
    public Uni<Response> getAward(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_AWARD_ID);

        return
                awardService.getAward(id)
                        .map(awardDTO -> Response.ok(awardDTO).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Operation(
            summary = "Mettre à jour une récompense",
            description = """
                    Permet de mettre à jour une récompense existante à partir de son identifiant.
                    Le champ `id` dans le corps de la requête doit correspondre à celui de l'URL.
                    """
    )
    @APIResponse(
            responseCode = "200",
            description = "Récompense mise à jour avec succès",
            content = @Content(schema = @Schema(implementation = AwardDTO.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (nom manquant ou corps absent)"
    )
    @APIResponse(
            responseCode = "404",
            description = "Récompense non trouvée"
    )
    @APIResponse(
            responseCode = "422",
            description = "Identifiant du corps de la requête différent de celui de l'URL"
    )
    @Parameter(name = "id", description = "Identifiant de la récompense", required = true, in = ParameterIn.PATH)
    @RequestBody(
            description = "Informations de la récompense à mettre à jour",
            content = @Content(schema = @Schema(implementation = AwardDTO.class))
    )
    public Uni<Response> updateAward(@RestPath @NotNull Long id, @Valid AwardDTO awardDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_AWARD_ID);

        if (Objects.isNull(awardDTO)) {
            throw new BadRequestException("Aucune information sur la récompense n’a été fournie dans la requête");
        }

        if (!Objects.equals(id, awardDTO.getId())) {
            throw new WebApplicationException("L'identifiant de la récompense ne correspond pas à celui de la requête", 422);
        }

        return
                awardService.updateAward(id, awardDTO)
                        .map(entity -> Response.ok(entity).build())
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer une récompense",
            description = """
                    Supprime une récompense à partir de son identifiant.
                    Retourne 204 si la suppression a réussi, ou 404 si la récompense n'existe pas.
                    """
    )
    @APIResponse(
            responseCode = "204",
            description = "Récompense supprimée avec succès"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Récompense non trouvée"
    )
    @Parameter(name = "id", description = "Identifiant de la récompense", required = true, in = ParameterIn.PATH)
    public Uni<Response> deleteAward(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_AWARD_ID);

        return
                awardService.deleteAward(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build()
                        )
                ;
    }

}
