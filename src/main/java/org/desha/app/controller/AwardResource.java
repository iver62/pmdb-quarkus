package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.service.AwardService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("awards")
@ApplicationScoped
@RolesAllowed({"user", "admin"})
public class AwardResource {

    private final AwardService awardService;

    @Inject
    public AwardResource(AwardService awardService) {
        this.awardService = awardService;
    }

    @GET
    @Path("{id}")
    public Uni<Response> getAward(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_AWARD_ID);

        return
                awardService.getAward(id)
                        .map(awardDTO -> Response.ok(awardDTO).build())
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateAward(@RestPath Long id, AwardDTO awardDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_AWARD_ID);

        if (Objects.isNull(awardDTO)) {
            throw new BadRequestException("Aucune information sur la récompense n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(awardDTO.getName())) {
            throw new BadRequestException("Le nom de la récompense n’a pas été fourni dans la requête");
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
    @Path("{id}")
    public Uni<Response> deleteAward(@RestPath Long id) {
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
