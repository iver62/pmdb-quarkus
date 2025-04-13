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
import org.jboss.resteasy.reactive.RestPath;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("awards")
@ApplicationScoped
@Slf4j
@RolesAllowed({"user", "admin"})
public class AwardResource {

    private final AwardService awardService;

    @Inject
    public AwardResource(AwardService awardService) {
        this.awardService = awardService;
    }

    /**
     * Récupère une récompense à partir de son identifiant.
     *
     * @param id l'identifiant de la récompense à récupérer
     * @return un {@link Uni} contenant une réponse HTTP :
     * <ul>
     *     <li>{@link Response#ok(Object)} avec un {@link org.desha.app.domain.dto.AwardDTO} si la récompense est trouvée</li>
     *     <li>{@link Response#serverError()} avec un message d’erreur en cas d’échec</li>
     * </ul>
     */
    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return awardService.getAward(id)
                .onItem().ifNotNull().transform(awardDTO -> Response.ok(awardDTO).build())
                .onFailure().recoverWithItem(err -> {
                            log.error("Erreur lors de la récupération de la récompense: {}", err.getMessage());
                            return Response.serverError().entity("Erreur serveur : " + err.getMessage()).build();
                        }
                );
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
    @Path("ceremonies")
    public Uni<Response> getCeremonies(@BeanParam QueryParamsDTO queryParamsDTO) {
        return
                awardService.getCeremonies(Page.of(queryParamsDTO.getPageIndex(), queryParamsDTO.getSize()), queryParamsDTO.validateSortDirection(), queryParamsDTO.getTerm())
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

    /**
     * Met à jour une récompense existante à partir de son identifiant et des nouvelles données fournies.
     *
     * <p>Cette méthode effectue une validation minimale sur l'objet {@link AwardDTO} reçu pour s'assurer
     * que les champs essentiels (nom de la cérémonie et nom de la récompense) ne sont pas nuls.</p>
     * <p>En cas de succès, la récompense mise à jour est retournée avec un code HTTP 200 (OK).</p>
     * <p>Si une erreur se produit pendant la mise à jour, une réponse d'erreur serveur est retournée.</p>
     *
     * @param id       identifiant de la récompense à mettre à jour
     * @param awardDTO les nouvelles données de la récompense
     * @return un {@link Uni} contenant la réponse HTTP
     * @throws WebApplicationException si les données fournies sont incomplètes (code 422)
     */
    @PUT
    @Path("{id}")
    public Uni<Response> updateAward(@RestPath Long id, AwardDTO awardDTO) {
        if (Objects.isNull(awardDTO) || Objects.isNull(awardDTO.getCeremony()) || Objects.isNull(awardDTO.getName())) {
            throw new WebApplicationException("Award name was not set on request.", 422);
        }

        return
                awardService.updateAward(id, awardDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la modification de la récompense: {}", err.getMessage());
                                    return Response.serverError().entity("Erreur serveur : " + err.getMessage()).build();
                                }
                        )
                ;
    }

    /**
     * Supprime une récompense (Award) par son identifiant.
     *
     * <p>Cette méthode appelle le service {@code awardService} pour effectuer la suppression,
     * puis retourne une réponse HTTP indiquant le résultat de l'opération.</p>
     *
     * @param id l'identifiant de la récompense à supprimer
     * @return un {@link Uni} contenant une {@link Response} HTTP :
     * <ul>
     *     <li>{@code 204 NO_CONTENT} si la suppression a réussi</li>
     *     <li>{@code 500 INTERNAL_SERVER_ERROR} en cas d'erreur lors de la suppression</li>
     * </ul>
     */
    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@RestPath Long id) {
        return
                awardService.deleteAward(id)
                        .map(deleted -> Response.ok(deleted).status(NO_CONTENT).build())
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression de la récompense: {}", err.getMessage());
                                    return Response.serverError().entity("Erreur serveur : " + err.getMessage()).build();
                                }
                        );
    }

}
