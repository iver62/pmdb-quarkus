package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/persons")
@ApplicationScoped
@APIResponse(responseCode = "401", description = "Utilisateur non authentifié")
@APIResponse(responseCode = "403", description = "Accès interdit")
@APIResponse(responseCode = "500", description = "Erreur interne du serveur")
@Tag(name = "Personnes", description = "Opérations liées aux personnes")
public class PersonResource {

    private final PersonService personService;

    @Inject
    public PersonResource(PersonService personService) {
        this.personService = personService;
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Récupère une personne par son identifiant",
            description = "Permet de récupérer les informations détaillées d'une personne à partir de son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Personne récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = PersonDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Identifiant de personne invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Aucune personne trouvée pour l'identifiant fourni"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getPersonById(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        return
                personService.getById(id)
                        .map(personDTO -> Response.ok(personDTO).build())
                ;
    }

    @GET
    @Path("/{id}/light")
    @Operation(
            summary = "Récupère une version allégée d'une personne par son identifiant",
            description = "Permet de récupérer les informations principales (version légère) d'une personne à partir de son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Personne récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = LitePersonDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Identifiant de personne invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Aucune personne trouvée pour l'identifiant fourni"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getLitePersonById(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        return
                personService.getLightById(id)
                        .map(lightPersonDTO -> Response.ok(lightPersonDTO).build())
                ;
    }

    @GET
    @Path("/light")
    @Operation(
            summary = "Récupère la liste des personnes en version allégée",
            description = "Permet de récupérer une liste paginée des personnes avec uniquement les informations essentielles (version légère). " +
                    "Les résultats peuvent être filtrés par critères et triés selon les paramètres fournis."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des personnes récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune personne ne correspond aux critères de recherche"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: date incohérente ou champ de tri invalide)"
            )
    })
    public Uni<Response> getLitePersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Operation(
            summary = "Récupère la liste des personnes avec détails",
            description = """
                    Permet de récupérer une liste paginée des personnes avec leurs informations complètes.
                    Les résultats peuvent être filtrés par critères et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des personnes récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = PersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune personne ne correspond aux critères de recherche"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: date incohérente ou champ de tri invalide)"
            )
    })
    public Uni<Response> getPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/roles")
    @Operation(
            summary = "Récupère la liste des rôles (films) d'une personne",
            description = """
                    Permet de récupérer une liste paginée des rôles (films) associés à une personne identifiée par son ID.
                    Les résultats peuvent être triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des rôles récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "La personne n'a aucun rôle associé"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: ID invalide ou champ de tri non autorisé)"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée avec l'ID fourni"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getRolesByPerson(@RestPath @NotNull Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(MovieActor.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, MovieActor.ALLOWED_SORT_FIELDS);

        return
                personService.getRoles(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection())
                        .flatMap(personDTOList ->
                                personService.countRolesByPerson(id).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/directors")
    @Operation(
            summary = "Récupère la liste paginée des réalisateurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type DIRECTOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des réalisateurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun réalisateur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getDirectors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.DIRECTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/actors")
    @Operation(
            summary = "Récupère la liste paginée des acteurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type ACTOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des acteurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = LitePersonDTO.class, type = SchemaType.ARRAY)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun acteur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getActors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ACTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/assistant-directors")
    @Operation(
            summary = "Récupère la liste paginée des assistants réalisateurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type ASSISTANT_DIRECTOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des assistants réalisateurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun assistant réalisateur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getAssistantDirectors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ASSISTANT_DIRECTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/screenwriters")
    @Operation(
            summary = "Récupère la liste paginée des scénaristes",
            description = """
                    Permet de récupérer une liste paginée des personnes de type SCREENWRITER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des scénaristes récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun scénariste correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Erreur interne lors de la récupération de la liste des scénaristes"
            )
    })
    public Uni<Response> getScreenwriters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SCREENWRITER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/producers")
    @Operation(
            summary = "Récupère la liste paginée des producteurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type PRODUCER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des producteurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun producteur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getProducers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.PRODUCER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/composers")
    @Operation(
            summary = "Récupère la liste paginée des compositeurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type COMPOSER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des compositeurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun compositeur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getComposers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.COMPOSER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/musicians")
    @Operation(
            summary = "Récupère la liste paginée des musiciens",
            description = """
                    Permet de récupérer une liste paginée des personnes de type MUSICIAN.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des musiciens récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun musicien correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getMusicians(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.MUSICIAN);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/photographers")
    @Operation(
            summary = "Récupère la liste paginée des photographes",
            description = """
                    Permet de récupérer une liste paginée des personnes de type PHOTOGRAPHER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des photographes récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun photographe correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getPhotographers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.PHOTOGRAPHER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/costume-designers")
    @Operation(
            summary = "Récupère la liste paginée des costumiers",
            description = """
                    Permet de récupérer une liste paginée des personnes de type COSTUME_DESIGNER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des costumiers récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun costumier correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getCostumeDesigners(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.COSTUME_DESIGNER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/set-designers")
    @Operation(
            summary = "Récupère la liste paginée des décorateurs de plateau",
            description = """
                    Permet de récupérer une liste paginée des personnes de type SET_DESIGNER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des décorateurs de plateau récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun décorateur de plateau correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getSetDesigners(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SET_DESIGNER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/editors")
    @Operation(
            summary = "Récupère la liste paginée des monteurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type EDITOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des monteurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun monteur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.EDITOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/casters")
    @Operation(
            summary = "Récupère la liste paginée des directeurs de casting",
            description = """
                    Permet de récupérer une liste paginée des personnes de type CASTER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des directeurs de casting récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun directeur de casting correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getCasters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.CASTER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/artists")
    @Operation(
            summary = "Récupère la liste paginée des artistes",
            description = """
                    Permet de récupérer une liste paginée des personnes de type ARTIST.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des artistes récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun artiste correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ARTIST);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sound-editors")
    @Operation(
            summary = "Récupère la liste paginée des ingénieurs du son",
            description = """
                    Permet de récupérer une liste paginée des personnes de type SOUND_EDITOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des ingénieurs du son récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun ingénieur du son correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getSoundEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SOUND_EDITOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/makeup-artists")
    @Operation(
            summary = "Récupère la liste paginée des maquilleurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type MAKEUP_ARTIST.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des maquilleurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun maquilleur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getMakeupArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.MAKEUP_ARTIST);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/vfx-supervisors")
    @Operation(
            summary = "Récupère la liste paginée des superviseurs des effets visuels",
            description = """
                    Permet de récupérer une liste paginée des personnes de type VFX_SUPERVISOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets visuels récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun superviseur des effets visuels correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getVfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.VFX_SUPERVISOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sfx-supervisors")
    @Operation(
            summary = "Récupère la liste paginée des superviseurs des effets spéciaux",
            description = """
                    Permet de récupérer une liste paginée des personnes de type SFX_SUPERVISOR.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets spéciaux récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun superviseur des effets spéciaux correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getSfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SFX_SUPERVISOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/hair-dressers")
    @Operation(
            summary = "Récupère la liste paginée des coiffeurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type HAIR_DRESSER.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des coiffeurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun coiffeur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getHairDressers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.HAIR_DRESSER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/stuntmen")
    @Operation(
            summary = "Récupère la liste paginée des cascadeurs",
            description = """
                    Permet de récupérer une liste paginée des personnes de type STUNT_MAN.
                    Les résultats peuvent être filtrés selon les critères de recherche et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des cascadeurs récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = LitePersonDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun cascadeur correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: dates incohérentes ou champ de tri non autorisé)"
            )
    })
    public Uni<Response> getStuntmen(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.STUNT_MAN);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies")
    @Operation(
            summary = "Récupère la liste paginée des films d'une personne",
            description = """
                    Permet de récupérer la liste des films associés à une personne identifiée par son ID.
                    Les résultats peuvent être filtrés et triés selon les paramètres fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des films récupérée avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun film trouvé pour cette personne"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides (ex: ID invalide ou dates incohérentes)"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée avec l'ID fourni"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getMoviesByPerson(@RestPath @NotNull Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getMoviesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(movieList ->
                                personService.countMoviesByPerson(id, criteriaDTO)
                                        .map(total ->
                                                movieList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                ;
    }

    @GET
    @Path("/photos/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    @Operation(
            summary = "Récupère la photo d'une personne",
            description = """
                    Permet de récupérer l'image associée à une personne à partir du nom de fichier fourni.
                    Si le fichier n'existe pas ou est invalide, une réponse appropriée est renvoyée."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Photo récupérée avec succès",
                    content = @Content(
                            mediaType = "image/jpeg",
                            schema = @Schema(
                                    type = SchemaType.STRING,
                                    format = "binary"
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Nom de fichier invalide ou non fourni"
            )
    })
    public Uni<Response> getPhoto(@PathParam("fileName") String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty() || Objects.equals("undefined", fileName)) {
            log.warn("Invalid file request: {}", fileName);
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid file name").build());
        }

        return
                personService.getPhoto(fileName)
                        .onItem().ifNotNull().transformToUni(
                                file -> Uni.createFrom().item(() -> {
                                    try {
                                        byte[] fileBytes = Files.readAllBytes(file.toPath());
                                        String mimeType = Files.probeContentType(file.toPath());

                                        log.info("Serving photo: {}", fileName);
                                        return Response.ok(fileBytes).type(mimeType).build();
                                    } catch (IOException e) {
                                        log.error("Error loading photo {}: {}", fileName, e.getMessage());
                                        return Response.serverError().entity("Erreur lors du chargement de la photo").build();
                                    }
                                })
                        )
                        .onItem().ifNull().continueWith(() -> {
                            log.warn("Photo not found: {}", fileName);
                            return Response.status(Response.Status.NOT_FOUND).entity("Photo introuvable").build();
                        });
    }

    @GET
    @Path("/countries")
    @Operation(
            summary = "Récupère la liste des pays associés aux personnes",
            description = """
                    Retourne la liste des pays associés aux personnes en fonction des critères de recherche,
                    de tri, de pagination et de langue spécifiés."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CountryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays trouvé correspondant aux critères"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides"
            )
    })
    public Uni<Response> getCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                personService.getCountries(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .flatMap(countryList ->
                                personService.countCountries(term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies/countries")
    @Operation(
            summary = "Récupère la liste des pays associés aux films liés à une personne",
            description = """
                    Retourne la liste des pays associés aux films d'une personne identifiée par son ID,
                    avec prise en charge de la pagination, du tri, de la recherche par terme et de la langue."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays des films récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CountryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays trouvé pour les films de cette personne"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres invalides ou ID de personne incorrect"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getMovieCountriesByPerson(@RestPath @NotNull Long id, @BeanParam QueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                personService.getMovieCountriesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .flatMap(countryDTOList ->
                                personService.countMovieCountriesByPerson(id, term, finalLang).map(total ->
                                        countryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies/categories")
    @Operation(
            summary = "Récupère la liste des catégories associées aux films liés à une personne",
            description = """
                    Retourne la liste des catégories associées aux films d'une personne identifiée par son ID,
                    avec prise en charge de la pagination, du tri et de la recherche par terme."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des catégories des films récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CategoryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune catégorie trouvée pour les films de cette personne"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres invalides ou ID de personne incorrect"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getMovieCategoriesByPerson(@RestPath @NotNull Long id, @BeanParam QueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();

        return
                personService.getMovieCategoriesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term)
                        .flatMap(categoryDTOList ->
                                personService.countMovieCategoriesByPerson(id, term).map(total ->
                                        categoryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(categoryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/awards")
    @Operation(
            summary = "Récupère les récompenses associées à une personne",
            description = "Retourne l'ensemble des récompenses regroupées par cérémonie pour une personne identifiée par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des récompenses récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CeremonyAwardsDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune récompense trouvée pour cette personne"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "ID de personne invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getAwardsByPerson(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        return
                personService.getAwardsByPerson(id).map(awardDTOS ->
                        awardDTOS.isEmpty()
                                ? Response.noContent().build()
                                : Response.ok(awardDTOS).build()
                );
    }

    @POST
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Crée une nouvelle personne",
            description = """
                    Permet de créer une nouvelle personne dans le système. Seuls les utilisateurs avec le rôle 'user' ou 'admin'
                    peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Personne créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Données invalides fournies dans la requête"
            )
    })
    public Uni<Response> save(@Valid PersonDTO personDTO) {
        if (Objects.isNull(personDTO)) {
            throw new BadRequestException("Aucune information sur la personne n’a été fournie dans la requête");
        }

        if (Objects.nonNull(personDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        if (StringUtils.isBlank(personDTO.getName())) {
            throw new BadRequestException("Le nom de la personne n’a pas été fourni dans la requête");
        }

        return
                personService.save(personDTO)
                        .map(person -> Response.ok(person).status(CREATED).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour une personne existante",
            description = """
                    Permet de mettre à jour les informations d'une personne existante, y compris sa photo. Seuls les utilisateurs 
                    avec le rôle 'user' ou 'admin' peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Personne mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Données invalides fournies dans la requête"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "L'identifiant fourni dans le corps de la requête ne correspond pas à celui de l'URL"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> update(@RestPath @NotNull Long id, @RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid PersonDTO personDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        if (Objects.isNull(personDTO)) {
            throw new BadRequestException("Aucune information sur la personne n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(personDTO.getName())) {
            throw new BadRequestException("Le nom de la personne n’a pas été fourni dans la requête");
        }

        if (!Objects.equals(id, personDTO.getId())) {
            throw new WebApplicationException("L'identifiant de la personne ne correspond pas à celui de la requête", 422);
        }

        return
                personService.update(id, file, personDTO)
                        .map(person -> Response.ok(person).build())
                ;
    }

    @PUT
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour la liste des pays associés à une personne",
            description = """
                    Permet de remplacer la liste des pays liés à une personne donnée. Seuls les utilisateurs avec
                     le rôle 'user' ou 'admin' peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CountryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays associé après la mise à jour"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "La liste des pays fournie est invalide ou nulle"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> updateCountries(@RestPath @NotNull Long id, Set<CountryDTO> countryDTOSet) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        if (Objects.isNull(countryDTOSet)) {
            throw new WebApplicationException("La liste des pays ne peut être nulle.", 422);
        }

        return
                personService.updateCountries(id, countryDTOSet)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    @PATCH
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajoute des pays à une personne",
            description = """
                    Permet d'ajouter un ou plusieurs pays à la liste des pays associés à une personne donnée. Seuls les utilisateurs
                    avec le rôle 'user' ou 'admin' peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CountryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays ajouté"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "La liste des pays fournie est invalide ou nulle"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> addCountries(@RestPath @NotNull Long id, Set<CountryDTO> countryDTOSet) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        if (Objects.isNull(countryDTOSet)) {
            throw new WebApplicationException("La liste des pays ne peut être nulle.", 422);
        }

        return
                personService.addCountries(id, countryDTOSet)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    @PATCH
    @Path("/{personId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime un pays associé à une personne",
            description = """
                    Permet de retirer un pays spécifique de la liste des pays associés à une personne donnée. Seuls les utilisateurs 
                    avec le rôle 'user' ou 'admin' peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Pays retiré avec succès, liste mise à jour",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = CountryDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays à supprimer ou liste vide après suppression"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne ou pays non trouvé"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "L'identifiant de la personne ou du pays est invalide"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> removeCountry(@RestPath @NotNull Long personId, @RestPath @NotNull Long countryId) {
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);
        ValidationUtils.validateIdOrThrow(countryId, Messages.INVALID_COUNTRY_ID);

        return
                personService.removeCountry(personId, countryId)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Operation(
            summary = "Supprime une personne",
            description = """
                    Permet de supprimer définitivement une personne et les fichiers associés (photo, etc.). Seul un utilisateur
                     avec le rôle 'admin' peut effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Personne supprimée avec succès",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "L'identifiant fourni est invalide"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> delete(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        return
                personService.deletePerson(id)
                        .map(person -> Response.ok(person).build())
                ;
    }

    @DELETE
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime tous les pays associés à une personne",
            description = """
                    Permet de retirer l'ensemble des pays liés à la personne identifiée par son ID.
                    Seuls les utilisateurs avec les rôles 'user' ou 'admin' peuvent effectuer cette opération."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Pays supprimés avec succès",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Personne non trouvée"
            ),
            @APIResponse(
                    responseCode = "422",
                    description = "L'identifiant fourni est invalide"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> deleteCountries(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_PERSON_ID);

        return
                personService.clearCountries(id)
                        .map(person -> Response.ok(person).build())
                ;
    }
}
