package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.service.AwardService;
import org.desha.app.service.CeremonyService;
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

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

    /**
     * Récupère une liste paginée de noms de cérémonies correspondant au terme de recherche fourni.
     *
     * <p>Les paramètres de tri, de pagination et de recherche sont encapsulés dans {@link QueryParamsDTO}.</p>
     *
     * @param queryParamsDTO l'objet contenant les paramètres de requête :
     *                       <ul>
     *                           <li>pageIndex : index de la page</li>
     *                           <li>size : taille de la page</li>
     *                           <li>direction : direction du tri (ascendant ou descendant)</li>
     *                           <li>term : terme de recherche</li>
     *                       </ul>
     * @return un {@link Uni} contenant une {@link Response} HTTP :
     * <ul>
     *     <li>{@link Response#ok(Object)} avec la liste des cérémonies si elle n'est pas vide</li>
     *     <li>{@link Response#noContent()} si aucune cérémonie ne correspond</li>
     *     <li>{@link Response#serverError()} avec un message d'erreur en cas d'exception</li>
     * </ul>
     */
        @GET
        public Uni<Response> getCeremonies(@BeanParam QueryParamsDTO queryParamsDTO) {
            return
                    ceremonyService.getCeremonies(Page.of(queryParamsDTO.getPageIndex(), queryParamsDTO.getSize()), queryParamsDTO.validateSortDirection(), queryParamsDTO.getTerm())
                            .onItem().ifNotNull().transform(stringList ->
                                    stringList.isEmpty()
                                            ? Response.noContent().build()
                                            : Response.ok(stringList).build()
                            )
                            .onItem().ifNull().continueWith(Response.noContent().build())
                            .onFailure().recoverWithItem(err -> {
                                        log.error("Erreur lors de la récupération des cérémonies: {}", err.getMessage());
                                        return Response.serverError().entity("Erreur serveur : " + err.getMessage()).build();
                                    }
                            )
                    ;
        }

}