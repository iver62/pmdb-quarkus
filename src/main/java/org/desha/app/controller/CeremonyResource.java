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
import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.service.CeremonyService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("ceremonies")
@ApplicationScoped
@Slf4j
@RolesAllowed({"user", "admin"})
public class CeremonyResource {

    private final CeremonyService ceremonyService;

    @Inject
    public CeremonyResource(CeremonyService ceremonyService) {
        this.ceremonyService = ceremonyService;
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getCeremony(Long id) {
        return
                ceremonyService.getCeremony(id)
                        .map(ceremonyDTOSet -> Response.ok(ceremonyDTOSet).build())
                ;
    }

    @GET
    public Uni<Response> getCeremonies(@BeanParam QueryParamsDTO queryParamsDTO) {
        return
                ceremonyService.getCeremonies(Page.of(queryParamsDTO.getPageIndex(), queryParamsDTO.getSize()), queryParamsDTO.validateSortDirection(), queryParamsDTO.getTerm())
                        .onItem().ifNull().continueWith(Set::of)
                        .map(ceremonyDTOSet ->
                                ceremonyDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyDTOSet).build()
                        )
                ;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> createCeremony(@Valid CeremonyDTO ceremonyDTO) {
        if (Objects.nonNull(ceremonyDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        if (StringUtils.isBlank(ceremonyDTO.getName())) {
            throw new BadRequestException("Le nom de la cérémonie n’a pas été fourni dans la requête");
        }

        return
                ceremonyService.create(ceremonyDTO)
                        .map(dto -> Response.status(CREATED).entity(dto).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> updateCeremony(@RestPath Long id, CeremonyDTO ceremonyDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(ceremonyDTO)) {
            throw new BadRequestException("Aucune information sur la cérémonie n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(ceremonyDTO.getName())) {
            throw new BadRequestException("Le nom de la cérémonie n’a pas été fourni dans la requête");
        }

        if (!Objects.equals(id, ceremonyDTO.getId())) {
            throw new WebApplicationException("L'identifiant de la cérémonie ne correspond pas à celui de la requête", 422);
        }

        return
                ceremonyService.update(id, ceremonyDTO)
                        .map(category -> Response.ok(category).build())
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> deleteCeremony(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                ceremonyService.deleteCeremony(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.status(NO_CONTENT).build()
                                : Response.status(NOT_FOUND).build())
                ;
    }

}