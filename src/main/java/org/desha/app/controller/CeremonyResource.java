package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.domain.dto.QueryParamsDTO;
import org.desha.app.service.CeremonyService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("ceremonies")
@ApplicationScoped
@APIResponse(responseCode = "401", description = "Utilisateur non authentifié")
@APIResponse(responseCode = "403", description = "Accès interdit")
@APIResponse(responseCode = "500", description = "Erreur interne du serveur")
@Tag(name = "Cérémonies", description = "Opérations liées aux cérémonies")
public class CeremonyResource {

    private final CeremonyService ceremonyService;

    @Inject
    public CeremonyResource(CeremonyService ceremonyService) {
        this.ceremonyService = ceremonyService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer une cérémonie par son ID",
            description = "Retourne les informations d'une cérémonie si elle existe dans la base de données."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cérémonie trouvée",
            content = @Content(schema = @Schema(implementation = CeremonyDTO.class))
    )
    @APIResponse(responseCode = "400", description = "Identifiant invalide")
    @APIResponse(responseCode = "404", description = "Cérémonie non trouvée")
    @Parameter(name = "id", description = "Identifiant de la cérémonie", required = true)
    public Uni<Response> getCeremony(@RestPath Long id) {
        return
                ceremonyService.getCeremony(id)
                        .map(ceremonyDTOSet -> Response.ok(ceremonyDTOSet).build())
                ;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupérer les cérémonies avec pagination, tri et recherche",
            description = "Retourne la liste des cérémonies avec prise en charge de la pagination, du tri et de la recherche par nom."
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des cérémonies trouvée",
            content = @Content(schema = @Schema(implementation = List.class)),
            headers = {
                    @Header(
                            name = "X-Total-Count",
                            description = "Nombre total de cérémonies correspondant aux critères",
                            schema = @Schema(type = SchemaType.NUMBER)
                    )
            }
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune cérémonie trouvée avec les critères fournis",
            headers = {
                    @Header(name = "X-Total-Count",
                            description = "Nombre total de cérémonies correspondant aux critères",
                            schema = @Schema(type = SchemaType.NUMBER)
                    )
            }
    )
    @APIResponse(
            responseCode = "400",
            description = "Paramètres de tri invalides"
    )
    @Parameter(name = "term", description = "Terme de recherche pour filtrer les cérémonies sur le nom", in = ParameterIn.QUERY)
    @Parameter(name = "lang", in = ParameterIn.QUERY, hidden = true)
    @Parameter(name = "from-creation-date", in = ParameterIn.QUERY, hidden = true)
    @Parameter(name = "from-last-update", in = ParameterIn.QUERY, hidden = true)
    @Parameter(name = "to-creation-date", in = ParameterIn.QUERY, hidden = true)
    @Parameter(name = "to-last-update", in = ParameterIn.QUERY, hidden = true)
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Créer une nouvelle cérémonie",
            description = "Permet de créer une nouvelle cérémonie. Le champ `id` ne doit pas être renseigné dans la requête."
    )
    @APIResponse(
            responseCode = "201",
            description = "Cérémonie créée avec succès",
            content = @Content(schema = @Schema(implementation = CeremonyDTO.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (champ manquant ou ID fourni)"
    )
    @RequestBody(
            description = "Les informations de la cérémonie à créer",
            required = true,
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    public Uni<Response> createCeremony(@Valid CeremonyDTO ceremonyDTO) {
        if (Objects.isNull(ceremonyDTO)) {
            throw new BadRequestException("Aucune information sur la cérémonie n’a été fournie dans la requête");
        }

        if (Objects.nonNull(ceremonyDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        return
                ceremonyService.create(ceremonyDTO)
                        .map(dto -> Response.status(CREATED).entity(dto).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour une cérémonie",
            description = """
                    Permet de mettre à jour une cérémonie existante à partir de son identifiant.
                    Le champ `id` dans le corps de la requête doit correspondre à celui de l'URL.
                    """
    )
    @APIResponse(
            responseCode = "200",
            description = "Cérémonie mise à jour avec succès",
            content = @Content(schema = @Schema(implementation = CeremonyDTO.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide (nom manquant ou corps absent)"
    )
    @APIResponse(
            responseCode = "404",
            description = "Cérémonie non trouvée"
    )
    @APIResponse(
            responseCode = "422",
            description = "Identifiant du corps de la requête différent de celui de l'URL"
    )
    @Parameter(name = "id", description = "Identifiant de la cérémonie", required = true)
    @RequestBody(
            description = "Informations de la cérémonie à mettre à jour",
            required = true,
            content = @Content(schema = @Schema(implementation = CeremonyDTO.class))
    )
    public Uni<Response> updateCeremony(@RestPath Long id, @Valid CeremonyDTO ceremonyDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(ceremonyDTO)) {
            throw new BadRequestException("Aucune information sur la cérémonie n’a été fournie dans la requête");
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
    @Operation(
            summary = "Supprimer une cérémonie",
            description = """
                    Supprime une cérémonie à partir de son identifiant.
                    Retourne 204 si la suppression a réussi, 404 si la cérémonie n'existe pas.
                    """
    )
    @APIResponse(
            responseCode = "204",
            description = "Cérémonie supprimée avec succès"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Cérémonie non trouvée"
    )
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