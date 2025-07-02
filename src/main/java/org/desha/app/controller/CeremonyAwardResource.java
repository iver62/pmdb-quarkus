package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.service.CeremonyAwardsService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;

@Path("ceremony-awards")
@ApplicationScoped
@Slf4j
@RolesAllowed({"user", "admin"})
public class CeremonyAwardResource {

    private final CeremonyAwardsService ceremonyAwardsService;

    @Inject
    public CeremonyAwardResource(CeremonyAwardsService ceremonyAwardsService) {
        this.ceremonyAwardsService = ceremonyAwardsService;
    }

    /**
     * Ajoute des récompenses à un film donné.
     *
     * @param awardDTOList Une liste d'objets {@link AwardDTO} représentant les récompenses à ajouter au film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec les récompenses mises à jour si l'ajout est réussi.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{id}/awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addAwards(@RestPath Long id, List<AwardDTO> awardDTOList) {
        if (Objects.isNull(awardDTOList)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle.");
        }

        return
                ceremonyAwardsService.addAwards(id, awardDTOList)
                        .onItem().ifNotNull().transform(ceremonyAwardsDTO ->
                                ceremonyAwardsDTO.getAwards().isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTO).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la mise à jour des récompenses: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur serveur : " + err.getMessage())
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PATCH
    @Path("/{ceremonyAwardsId}/awards/{awardId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeAward(@RestPath Long ceremonyAwardsId, @RestPath Long awardId) {
        return
                ceremonyAwardsService.removeAward(ceremonyAwardsId, awardId)
                        .onItem().ifNotNull().transform(ceremonyAwardsDTO ->
                                ceremonyAwardsDTO.getAwards().isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTO).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @DELETE
    @Path("/{id}/awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteAwards(@RestPath Long id) {
        return ceremonyAwardsService.clearAwards(id).map(deleted -> Response.ok(deleted).build());
    }

}