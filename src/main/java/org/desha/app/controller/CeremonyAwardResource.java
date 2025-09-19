package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.service.CeremonyAwardsService;
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

@Path("ceremony-awards")
@ApplicationScoped
@RolesAllowed({"user", "admin"})
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
@Tag(name = "Ceremony Awards", description = "Opérations liées aux récompenses associées à une cérémonie")
public class CeremonyAwardResource {

    private final CeremonyAwardsService ceremonyAwardsService;

    @Inject
    public CeremonyAwardResource(CeremonyAwardsService ceremonyAwardsService) {
        this.ceremonyAwardsService = ceremonyAwardsService;
    }

    @PATCH
    @Path("/{id}/awards")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Ajouter des récompenses à une cérémonie",
            description = "Associe une liste de récompenses à une cérémonie donnée"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Récompenses ajoutées avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CeremonyAwardsDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune récompense après ajout"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "La liste des récompenses est nulle"
            )
    })
    @Parameter(name = "id", description = "Identifiant de la cérémonie", required = true, in = ParameterIn.PATH)
    public Uni<Response> addAwards(@RestPath @NotNull Long id, List<AwardDTO> awardDTOList) {
        if (Objects.isNull(awardDTOList)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle.");
        }

        return
                ceremonyAwardsService.addAwards(id, awardDTOList)
                        .map(ceremonyAwardsDTO ->
                                ceremonyAwardsDTO.getAwards().isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTO).build()
                        )
                ;
    }

    @PATCH
    @Path("/{ceremonyAwardsId}/awards/{awardId}")
    @Operation(
            summary = "Retirer une récompense d’une cérémonie",
            description = "Dissocie une récompense précise d’une cérémonie"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Récompense retirée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CeremonyAwardsDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Toutes les récompenses ont été retirées"
            )
    })
    @Parameter(name = "ceremonyAwardsId", description = "Identifiant de la cérémonie", required = true, in = ParameterIn.PATH)
    @Parameter(name = "awardId", description = "Identifiant de la récompense", required = true, in = ParameterIn.PATH)
    public Uni<Response> removeAward(@RestPath @NotNull Long ceremonyAwardsId, @RestPath @NotNull Long awardId) {
        return
                ceremonyAwardsService.removeAward(ceremonyAwardsId, awardId)
                        .map(ceremonyAwardsDTO ->
                                ceremonyAwardsDTO.getAwards().isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTO).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}/awards")
    @APIResponse(
            responseCode = "200",
            description = "Récompenses supprimées",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class)
            )
    )
    @Parameter(name = "id", description = "Identifiant de la cérémonie", required = true, in = ParameterIn.PATH)
    public Uni<Response> deleteAwards(@RestPath Long id) {
        return ceremonyAwardsService.clearAwards(id).map(deleted -> Response.ok(deleted).build());
    }

}