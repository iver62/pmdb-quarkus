package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.exception.ErrorResponse;
import org.desha.app.service.MovieTechnicianService;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Path("/movies/{id}")
@ApplicationScoped
@APIResponses(value = {
        @APIResponse(
                responseCode = "401",
                description = "Utilisateur non authentifié",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "403",
                description = "Accès interdit",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
                responseCode = "500",
                description = "Erreur interne du serveur",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
})
@Tag(name = "Techniciens de film", description = "Opérations liées aux techniciens d'un film")
public class MovieTechnicianResource {

    private final MovieTechnicianService movieTechnicianService;
    private final PersonService personService;

    @Inject
    public MovieTechnicianResource(MovieTechnicianService movieTechnicianService, PersonService personService) {
        this.movieTechnicianService = movieTechnicianService;
        this.personService = personService;
    }

    @GET
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des producteurs d'un film",
            description = "Retourne la liste des producteurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des producteurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun producteur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getProducers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieProducers(), Messages.NULL_PRODUCERS, Messages.ERROR_WHILE_GETTING_PRODUCERS);
    }

    @GET
    @Path("/directors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des réalisateurs d'un film",
            description = "Retourne la liste des réalisateurs associés au film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des réalisateurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun réalisateur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Le film correspondant à l'ID fourni n'a pas été trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getDirectors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieDirectors(), Messages.NULL_DIRECTORS, Messages.ERROR_WHILE_GETTING_DIRECTORS);
    }

    @GET
    @Path("/assistant-directors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des assistants réalisateurs d'un film",
            description = "Retourne la liste des assistants réalisateurs associés au film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des assistants réalisateurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun assistant réalisateur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Le film correspondant à l'ID fourni n'a pas été trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getAssistantDirectors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieAssistantDirectors(), Messages.NULL_ASSISTANT_DIRECTORS, Messages.ERROR_WHILE_GETTING_ASSISTANT_DIRECTORS);
    }

    @GET
    @Path("/screenwriters")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des scénaristes d'un film",
            description = "Retourne la liste des scénaristes associés au film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des scénaristes du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun scénariste trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Le film correspondant à l'ID fourni n'a pas été trouvé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getScreenwriters(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieScreenwriters(), Messages.NULL_SCREENWRITERS, Messages.ERROR_WHILE_GETTING_SCREENWRITERS);
    }

    @GET
    @Path("/composers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des compositeurs d'un film",
            description = "Retourne la liste des compositeurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des compositeurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun compositeur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getComposers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieComposers(), Messages.NULL_COMPOSERS, Messages.ERROR_WHILE_GETTING_COMPOSERS);
    }

    @GET
    @Path("/musicians")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des musiciens d'un film",
            description = "Retourne la liste des musiciens associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des musiciens du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun musicien trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getMusicians(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieMusicians(), Messages.NULL_MUSICIANS, Messages.ERROR_WHILE_GETTING_MUSICIANS);
    }

    @GET
    @Path("/photographers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des photographes d'un film",
            description = "Retourne la liste des photographes associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des photographes du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun photographe trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getPhotographers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMoviePhotographers(), Messages.NULL_PHOTOGRAPHERS, Messages.ERROR_WHILE_GETTING_PHOTOGRAPHERS);
    }

    @GET
    @Path("/costume-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des créateurs de costumes d'un film",
            description = "Retourne la liste des créateurs de costumes associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des créateurs de costumes du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun créateur de costumes trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getCostumeDesigners(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieCostumeDesigners(), Messages.NULL_COSTUME_DESIGNERS, Messages.ERROR_WHILE_GETTING_COSTUME_DESIGNERS);
    }

    @GET
    @Path("/set-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des décorateurs de plateau d'un film",
            description = "Retourne la liste des décorateurs de plateau associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des décorateurs de plateau du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun décorateur de plateau trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getSetDesigners(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieSetDesigners(), Messages.NULL_SET_DESIGNERS, Messages.ERROR_WHILE_GETTING_SET_DESIGNERS);
    }

    @GET
    @Path("/editors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des monteurs d'un film",
            description = "Retourne la liste des monteurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des monteurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun monteur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getEditors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieEditors(), Messages.NULL_EDITORS, Messages.ERROR_WHILE_GETTING_EDITORS);
    }

    @GET
    @Path("/casters")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des directeurs de casting d'un film",
            description = "Retourne la liste des directeurs de casting associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des directeurs de casting du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun directeur de casting trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getCasters(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieCasters(), Messages.NULL_CASTERS, Messages.ERROR_WHILE_GETTING_CASTERS);
    }

    @GET
    @Path("/artists")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des artistes d'un film",
            description = "Retourne la liste des artistes associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des artistes du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun artiste trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getArtists(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieArtists(), Messages.NULL_ARTISTS, Messages.ERROR_WHILE_GETTING_ARTISTS);
    }

    @GET
    @Path("/sound-editors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des ingénieurs du son d'un film",
            description = "Retourne la liste des ingénieurs du son associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des ingénieurs du son du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun ingénieur du son trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getSoundEditors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieSoundEditors(), Messages.NULL_SOUND_EDITORS, Messages.ERROR_WHILE_GETTING_SOUND_EDITORS);
    }

    @GET
    @Path("/vfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des superviseurs des effets visuels d'un film",
            description = "Retourne la liste des superviseurs des effets visuels associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets visuels du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun superviseur des effets visuels trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getVfxSupervisors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(), Messages.NULL_VFX_SUPERVISORS, Messages.ERROR_WHILE_GETTING_VFX_SUPERVISORS);
    }

    @GET
    @Path("/sfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des superviseurs des effets spéciaux d'un film",
            description = "Retourne la liste des superviseurs des effets spéciaux associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets spéciaux du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun superviseur des effets spéciaux trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getSfxSupervisors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieSfxSupervisors(), Messages.NULL_SFX_SUPERVISORS, Messages.ERROR_WHILE_GETTING_SFX_SUPERVISORS);
    }

    @GET
    @Path("/makeup-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des maquilleurs d'un film",
            description = "Retourne la liste des maquilleurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des maquilleurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun maquilleur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getMakeupArtists(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieMakeupArtists(), Messages.NULL_MAKEUP_ARTISTS, Messages.ERROR_WHILE_GETTING_MAKEUP_ARTISTS);
    }

    @GET
    @Path("/hair-dressers")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des coiffeurs d'un film",
            description = "Retourne la liste des coiffeurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des coiffeurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun coiffeur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getHairDressers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieHairDressers(), Messages.NULL_HAIRDRESSERS, Messages.ERROR_WHILE_GETTING_HAIRDRESSERS);
    }

    @GET
    @Path("/stuntmen")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Récupère la liste des cascadeurs d'un film",
            description = "Retourne la liste des cascadeurs associés à un film identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des cascadeurs du film",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun cascadeur trouvé pour ce film"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> getStuntmen(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieStuntmen(), Messages.NULL_STUNTMEN, Messages.ERROR_WHILE_GETTING_STUNTMEN);
    }

    /**
     * Récupère la liste des techniciens d'un type donné pour un film identifié par son ID.
     * <p>
     * Cette méthode centralise la logique de récupération et de transformation des techniciens en {@link MovieTechnicianDTO}.
     * Si aucun technicien n’est trouvé pour le film, la réponse HTTP sera 204 No Content.
     *
     * @param id                    L'identifiant du film. Ne peut pas être {@code null}.
     * @param techniciansGetter     Fonction permettant de récupérer la liste des techniciens du film.
     * @param nullCheckErrorMessage Message d'erreur utilisé si la liste des techniciens est nulle.
     * @param globalErrorMessage    Message d'erreur global utilisé en cas d'échec inattendu.
     * @param <T>                   Type de technicien étendant {@link MovieProducer}.
     * @return Un {@link Uni} contenant la réponse HTTP avec la liste des {@link MovieTechnicianDTO}.
     * @throws WebApplicationException si l'identifiant du film est invalide ou en cas d'erreur serveur.
     */
    private <T extends MovieTechnician> Uni<Response> getTechniciansByMovieResponse(
            Long id,
            Function<Movie, List<T>> techniciansGetter,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, techniciansGetter, nullCheckErrorMessage, globalErrorMessage)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @PUT
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les producteurs d’un film",
            description = "Met à jour la liste des producteurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des producteurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieProducers(),
                        preparePerson(PersonType.PRODUCER, MovieProducer::build),
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_UPDATING_PRODUCERS
                );
    }

    @PUT
    @Path("/directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les réalisateurs d’un film",
            description = "Met à jour la liste des réalisateurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des réalisateurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieDirectors(),
                        preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_UPDATING_DIRECTORS
                );
    }

    @PUT
    @Path("/assistant-directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les assistants réalisateurs d’un film",
            description = "Met à jour la liste des assistants réalisateurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des assistants réalisateurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieAssistantDirectors(),
                        preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_UPDATING_ASSISTANT_DIRECTORS
                );
    }

    @PUT
    @Path("/screenwriters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les scénaristes d’un film",
            description = "Met à jour la liste des scénaristes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des scénaristes mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieScreenwriters(),
                        preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_UPDATING_SCREENWRITERS
                );
    }

    @PUT
    @Path("/composers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les compositeurs d’un film",
            description = "Met à jour la liste des compositeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des compositeurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieComposers(),
                        preparePerson(PersonType.COMPOSER, MovieComposer::build),
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_UPDATING_COMPOSERS
                );
    }

    @PUT
    @Path("/musicians")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les musiciens d’un film",
            description = "Met à jour la liste des musiciens associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des musiciens mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieMusicians(),
                        preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_UPDATING_MUSICIANS
                );
    }

    @PUT
    @Path("/photographers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les photographes d’un film",
            description = "Met à jour la liste des photographes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des photographes mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> savePhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMoviePhotographers(),
                        preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_UPDATING_PHOTOGRAPHERS
                );
    }

    @PUT
    @Path("/costume-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les costumiers d’un film",
            description = "Met à jour la liste des costumiers associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des costumiers mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieCostumeDesigners(),
                        preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_UPDATING_COSTUME_DESIGNERS
                );
    }

    @PUT
    @Path("/set-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les décorateurs d’un film",
            description = "Met à jour la liste des décorateurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des décorateurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSetDesigners(),
                        preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_UPDATING_SET_DESIGNERS
                );
    }

    @PUT
    @Path("/editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les monteurs d’un film",
            description = "Met à jour la liste des monteurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des monteurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieEditors(),
                        preparePerson(PersonType.EDITOR, MovieEditor::build),
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_UPDATING_EDITORS
                );
    }

    @PUT
    @Path("/casters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les directeurs de casting d’un film",
            description = "Met à jour la liste des directeurs de casting associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des directeurs de casting mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieCasters(),
                        preparePerson(PersonType.CASTER, MovieCaster::build),
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_UPDATING_CASTERS
                );
    }

    @PUT
    @Path("/artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les directeurs de casting d’un film",
            description = "Met à jour la liste des directeurs de casting associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des directeurs de casting mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieArtists(),
                        preparePerson(PersonType.ARTIST, MovieArtist::build),
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_UPDATING_ARTISTS
                );
    }

    @PUT
    @Path("/sound-editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les monteurs son d’un film",
            description = "Met à jour la liste des monteurs son associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des monteurs son mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSoundEditors(),
                        preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_UPDATING_SOUND_EDITORS
                );
    }

    @PUT
    @Path("/vfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les superviseurs des effets visuels d’un film",
            description = "Met à jour la liste des superviseurs des effets visuels associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets visuels mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(),
                        preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_UPDATING_VFX_SUPERVISORS
                );
    }

    @PUT
    @Path("/sfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les superviseurs des effets spéciaux d’un film",
            description = "Met à jour la liste des superviseurs des effets spéciaux associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets spéciaux mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSfxSupervisors(),
                        preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_UPDATING_SFX_SUPERVISORS
                );
    }

    @PUT
    @Path("/makeup-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les maquilleurs d’un film",
            description = "Met à jour la liste des maquilleurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des maquilleurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieMakeupArtists(),
                        preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_UPDATING_MAKEUP_ARTISTS
                );
    }


    @PUT
    @Path("/hair-dressers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les coiffeurs d’un film",
            description = "Met à jour la liste des coiffeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des coiffeurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {

        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieHairDressers(),
                        preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_UPDATING_HAIRDRESSERS
                );
    }

    @PUT
    @Path("/stuntmen")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Mettre à jour les cascadeurs d’un film",
            description = "Remplace ou met à jour la liste des cascadeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des cascadeurs mise à jour avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> saveStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieStuntmen(),
                        preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_UPDATING_STUNTMEN
                );
    }

    /**
     * Sauvegarde ou met à jour une liste de techniciens pour un film donné.
     * <p>
     * Cette méthode effectue les opérations suivantes :
     * <ul>
     *     <li>Valide l'ID du film et lance une exception si celui-ci est invalide.</li>
     *     <li>Vérifie que la liste de DTOs des techniciens n'est pas nulle.</li>
     *     <li>Délègue la sauvegarde à {@link MovieTechnicianService} en utilisant le getter et la factory asynchrone fournis.</li>
     *     <li>Retourne un {@link Uni} de {@link Response} :
     *         <ul>
     *             <li>204 No Content si la liste de techniciens sauvegardés est vide</li>
     *             <li>200 OK avec la liste de techniciens sauvegardés sinon</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param <T>                    Le type spécifique de {@link MovieTechnician} à sauvegarder.
     * @param id                     L'identifiant du film pour lequel les techniciens doivent être sauvegardés. Ne doit pas être null.
     * @param movieTechnicianDTOList La liste des DTOs représentant les techniciens à sauvegarder. Ne doit pas être null.
     * @param techniciansGetter      Fonction qui retourne la liste actuelle de techniciens du film.
     * @param asyncFactory           Fonction asynchrone qui transforme un DTO en entité technique spécifique du film.
     * @param nullCheckErrorMessage  Message d'erreur utilisé si {@code movieTechnicianDTOList} est null.
     * @param globalErrorMessage     Message d'erreur global à utiliser en cas d'échec lors de la sauvegarde.
     * @return Un {@link Uni} de {@link Response} correspondant au résultat de la sauvegarde.
     * @throws BadRequestException si l'ID du film est invalide ou {@code movieTechnicianDTOList} est null.
     */
    private <T extends MovieTechnician> Uni<Response> saveTechniciansByMovieResponse(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncFactory,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException(nullCheckErrorMessage);
        }

        return
                movieTechnicianService.saveTechnicians(id, movieTechnicianDTOList, techniciansGetter, asyncFactory, nullCheckErrorMessage, globalErrorMessage)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PATCH
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des producteurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des producteurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des producteurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieProducers(),
                        preparePerson(PersonType.PRODUCER, MovieProducer::build),
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_ADDING_PRODUCERS
                );
    }

    @PATCH
    @Path("/directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des réalisateurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des réalisateurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des réalisateurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieDirectors(),
                        preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_ADDING_DIRECTORS
                );
    }

    @PATCH
    @Path("/assistant-directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des assistants réalisateurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des assistants réalisateurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des assistants réalisateurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieAssistantDirectors(),
                        preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_ADDING_ASSISTANT_DIRECTORS
                );
    }

    @PATCH
    @Path("/screenwriters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des scénaristes à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des scénaristes d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des scénaristes ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieScreenwriters(),
                        preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_ADDING_SCREENWRITERS
                );
    }

    @PATCH
    @Path("/composers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des compositeurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des compositeurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des compositeurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieComposers(),
                        preparePerson(PersonType.COMPOSER, MovieComposer::build),
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_ADDING_COMPOSERS
                );
    }

    @PATCH
    @Path("/musicians")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des musiciens à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des musiciens d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des musiciens ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieMusicians(),
                        preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_ADDING_MUSICIANS
                );
    }

    @PATCH
    @Path("/photographers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des photographes à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des photographes d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des photographes ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addPhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMoviePhotographers(),
                        preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_ADDING_PHOTOGRAPHERS
                );
    }

    @PATCH
    @Path("/costume-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des costumiers à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des costumiers d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des costumiers ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieCostumeDesigners(),
                        preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_ADDING_COSTUME_DESIGNERS
                );
    }

    @PATCH
    @Path("/set-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des décorateurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des décorateurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des décorateurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSetDesigners(),
                        preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_ADDING_SET_DESIGNERS
                );
    }

    @PATCH
    @Path("/editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des monteurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des monteurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des monteurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieEditors(),
                        preparePerson(PersonType.EDITOR, MovieEditor::build),
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_ADDING_EDITORS
                );
    }

    @PATCH
    @Path("/casters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des directeurs de casting à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des directeurs de casting d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des directeurs de casting ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieCasters(),
                        preparePerson(PersonType.CASTER, MovieCaster::build),
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_ADDING_CASTERS
                );
    }

    @PATCH
    @Path("/artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des artistes à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des artistes d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des artistes ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieArtists(),
                        preparePerson(PersonType.ARTIST, MovieArtist::build),
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_ADDING_ARTISTS
                );
    }

    @PATCH
    @Path("/sound-editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des ingénieurs son à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des ingénieurs son d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des ingénieurs son ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSoundEditors(),
                        preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_ADDING_SOUND_EDITORS
                );
    }

    @PATCH
    @Path("/vfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des superviseurs des effets visuels à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des superviseurs des effets visuels d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets visuels ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(),
                        preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_ADDING_VFX_SUPERVISORS
                );
    }

    @PATCH
    @Path("/sfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des superviseurs des effets spéciaux à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des superviseurs des effets spéciaux d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des superviseurs des effets spéciaux ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieSfxSupervisors(),
                        preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_ADDING_SFX_SUPERVISORS
                );
    }

    @PATCH
    @Path("/makeup-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des maquilleurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des maquilleurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des maquilleurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieMakeupArtists(),
                        preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_ADDING_MAKEUP_ARTISTS
                );
    }

    @PATCH
    @Path("/hair-dressers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des coiffeurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des coiffeurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des coiffeurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieHairDressers(),
                        preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_ADDING_HAIRDRESSERS
                );
    }

    @PATCH
    @Path("/stuntmen")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajouter des cascadeurs à un film",
            description = "Ajoute une ou plusieurs personnes à la liste des cascadeurs d’un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des cascadeurs ajoutés avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = MovieTechnicianDTO.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (données manquantes ou mal formées)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> addStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        movie -> movie.getTechnicalTeam().getMovieStuntmen(),
                        preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_ADDING_STUNTMEN
                );
    }

    /**
     * Ajoute une liste de techniciens à un film donné.
     * <p>
     * Cette méthode effectue plusieurs étapes :
     * <ul>
     *     <li>Validation de l'ID du film via {@link ValidationUtils#validateIdOrThrow(Long, String)}.</li>
     *     <li>Vérification que la liste des {@link MovieTechnicianDTO} n'est pas nulle.
     *     En cas de liste nulle, une {@link BadRequestException} est levée avec le message fourni.</li>
     *     <li>Appel au service {@link MovieTechnicianService#addTechnicians} pour ajouter les techniciens au film.
     *     Cette opération est asynchrone et renvoie un {@link Uni} contenant la liste des techniciens ajoutés.</li>
     *     <li>Transformation du résultat :
     *         <ul>
     *             <li>Si la liste renvoyée est nulle, elle est remplacée par une liste vide.</li>
     *             <li>Si la liste est vide, renvoie une réponse HTTP 204 No Content.</li>
     *             <li>Sinon, renvoie une réponse HTTP 200 OK contenant la liste des techniciens ajoutés.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param <T>                    le type de technicien du film étendant {@link MovieTechnician}
     * @param id                     l'identifiant du film auquel les techniciens doivent être ajoutés
     * @param movieTechnicianDTOList la liste des DTO représentant les techniciens à ajouter
     * @param techniciansGetter      une fonction permettant d'extraire la liste des techniciens existants d'un film
     * @param asyncFactory           une fonction asynchrone qui convertit un DTO en entité technicien spécifique au film
     * @param nullCheckErrorMessage  message d'erreur à utiliser si {@code movieTechnicianDTOList} est null
     * @param globalErrorMessage     message d'erreur global à utiliser en cas d'échec lors de l'ajout des techniciens
     * @return un {@link Uni} de {@link Response} contenant soit :
     * <ul>
     *     <li>HTTP 200 OK avec la liste des {@link MovieTechnicianDTO} ajoutés</li>
     *     <li>HTTP 204 No Content si aucun technicien n'a été ajouté</li>
     * </ul>
     * @throws BadRequestException si {@code movieTechnicianDTOList} est null
     */
    private <T extends MovieTechnician> Uni<Response> addTechniciansByMovieResponse(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncFactory,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException(nullCheckErrorMessage);
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                techniciansGetter,
                                asyncFactory,
                                nullCheckErrorMessage,
                                globalErrorMessage
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    @PATCH
    @Path("/producers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un producteur d’un film",
            description = "Supprime un producteur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Producteur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieProducers(),
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_REMOVING_PRODUCER
                );
    }

    @PATCH
    @Path("/directors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un réalisateur d’un film",
            description = "Supprime un réalisateur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Réalisateur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieDirectors(),
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_REMOVING_DIRECTOR
                );
    }

    @PATCH
    @Path("/assistant-directors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un assistant réalisateur d’un film",
            description = "Supprime un assistant réalisateur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Assistant réalisateur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeAssistantDirector(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieDirectors(),
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_REMOVING_ASSISTANT_DIRECTOR
                );
    }

    @PATCH
    @Path("/screenwriters/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un scénariste d’un film",
            description = "Supprime un scénariste spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Scénariste supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieScreenwriters(),
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_REMOVING_SCREENWRITER
                );
    }

    @PATCH
    @Path("/composers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un compositeur d’un film",
            description = "Supprime un compositeur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Compositeur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeComposer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieComposers(),
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_REMOVING_COMPOSER
                );
    }

    @PATCH
    @Path("/musicians/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un musicien d’un film",
            description = "Supprime un musicien spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Musicien supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieMusicians(),
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_REMOVING_MUSICIAN
                );
    }

    @PATCH
    @Path("/photographers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un photographe d’un film",
            description = "Supprime un photographe spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Photographe supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMoviePhotographers(),
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_REMOVING_PHOTOGRAPHER
                );
    }

    @PATCH
    @Path("/costume-designers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un costumier d’un film",
            description = "Supprime un costumier spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Costumier supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeCostumeDesigner(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieCostumeDesigners(),
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_REMOVING_COSTUME_DESIGNER
                );
    }

    @PATCH
    @Path("/set-designers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un décorateur de plateau d’un film",
            description = "Supprime un décorateur de plateau spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Décorateur de plateau supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeSetDesigner(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieSetDesigners(),
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_REMOVING_SET_DESIGNER
                );
    }

    @PATCH
    @Path("/editors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un monteur d’un film",
            description = "Supprime un monteur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Monteur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieEditors(),
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_REMOVING_EDITOR
                );
    }

    @PATCH
    @Path("/casters/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un directeur de casting d’un film",
            description = "Supprime un directeur de casting spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Directeur de casting supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieCasters(),
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_REMOVING_CASTER
                );
    }

    @PATCH
    @Path("/artists/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un artiste d’un film",
            description = "Supprime un artiste spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Artiste supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeArtist(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieArtists(),
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_REMOVING_ARTIST
                );
    }

    @PATCH
    @Path("/sound-editors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un ingénieur son d’un film",
            description = "Supprime un ingénieur son spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Ingénieur son supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieSoundEditors(),
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_REMOVING_SOUND_EDITOR
                );
    }

    @PATCH
    @Path("/vfx-supervisors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un superviseur des effets visuels d’un film",
            description = "Supprime un superviseur des effets visuels spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Superviseur des effets visuels supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeVfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(),
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_REMOVING_VFX_SUPERVISOR
                );
    }

    @PATCH
    @Path("/sfx-supervisors/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un superviseur des effets spéciaux d’un film",
            description = "Supprime un superviseur des effets spéciaux spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Superviseur des effets spéciaux supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeSfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieSfxSupervisors(),
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_REMOVING_SFX_SUPERVISOR
                );
    }

    @PATCH
    @Path("/makeup-artists/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un maquilleur d’un film",
            description = "Supprime un maquilleur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Maquilleur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeMakeupArtist(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieMakeupArtists(),
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_REMOVING_MAKEUP_ARTIST
                );
    }

    @PATCH
    @Path("/hair-dressers/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un coiffeur d’un film",
            description = "Supprime un coiffeur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Coiffeur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeHairDresser(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieHairDressers(),
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_REMOVING_HAIRDRESSER
                );
    }

    @PATCH
    @Path("/stuntmen/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer un cascadeur d’un film",
            description = "Supprime un cascadeur spécifique d’un film donné, identifié par l’ID du film et l’ID de la personne."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Cascadeur supprimé avec succès",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieTechnicianDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film ou de la personne manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        movie -> movie.getTechnicalTeam().getMovieStuntmen(),
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_REMOVING_STUNTMAN
                );
    }

    /**
     * Supprime un technicien spécifique d’un film donné.
     * <p>
     * Cette méthode effectue plusieurs étapes :
     * <ul>
     *     <li>Validation de l'ID du film et de l'ID de la personne via {@link ValidationUtils#validateIdOrThrow(Long, String)}.</li>
     *     <li>Appel au service {@link MovieTechnicianService#removeTechnician} pour supprimer le technicien du film.
     *     Cette opération est asynchrone et renvoie un {@link Uni} contenant la liste des techniciens restant après suppression.</li>
     *     <li>Transformation du résultat :
     *         <ul>
     *             <li>Si la liste renvoyée est nulle, elle est remplacée par une liste vide.</li>
     *             <li>Si la liste est vide, renvoie une réponse HTTP 204 No Content.</li>
     *             <li>Sinon, renvoie une réponse HTTP 200 OK contenant la liste des techniciens restants après suppression.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param <T>                   le type de technicien du film étendant {@link MovieTechnician}
     * @param movieId               l'identifiant du film dont le technicien doit être supprimé
     * @param personId              l'identifiant du technicien à supprimer
     * @param techniciansGetter     une fonction permettant d'extraire la liste des techniciens existants d'un film
     * @param nullCheckErrorMessage message d'erreur à utiliser si la liste de techniciens est nulle
     * @param globalErrorMessage    message d'erreur global à utiliser en cas d'échec lors de la suppression
     * @return un {@link Uni} de {@link Response} contenant soit :
     * <ul>
     *     <li>HTTP 200 OK avec la liste des {@link MovieTechnicianDTO} restants après suppression</li>
     *     <li>HTTP 204 No Content si aucun technicien n’est présent après suppression</li>
     * </ul>
     * @throws BadRequestException si {@code movieId} ou {@code personId} sont invalides
     */
    private <T extends MovieTechnician> Uni<Response> removeTechnicianByMovieResponse(
            Long movieId,
            Long personId,
            Function<Movie, List<T>> techniciansGetter,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                techniciansGetter,
                                nullCheckErrorMessage,
                                globalErrorMessage
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    @DELETE
    @Path("/producers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les producteurs d’un film",
            description = "Supprime la totalité des producteurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des producteurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteProducers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieProducers(), Messages.NULL_PRODUCERS, Messages.ERROR_WHILE_CLEARING_PRODUCERS);
    }

    @DELETE
    @Path("/directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les réalisateurs d’un film",
            description = "Supprime la totalité des réalisateurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des réalisateurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteDirectors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieDirectors(), Messages.NULL_DIRECTORS, Messages.ERROR_WHILE_CLEARING_DIRECTORS);
    }

    @DELETE
    @Path("/assistant-directors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les assistants réalisateurs d’un film",
            description = "Supprime la totalité des assistants réalisateurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des assistants réalisateurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteAssistantDirectors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieAssistantDirectors(), Messages.NULL_ASSISTANT_DIRECTORS, Messages.ERROR_WHILE_CLEARING_ASSISTANT_DIRECTORS);
    }

    @DELETE
    @Path("/screenwriters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les scénaristes d’un film",
            description = "Supprime la totalité des scénaristes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des scénaristes supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteScreenwriters(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieScreenwriters(), Messages.NULL_SCREENWRITERS, Messages.ERROR_WHILE_CLEARING_SCREENWRITERS);
    }

    @DELETE
    @Path("/composers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les compositeurs d’un film",
            description = "Supprime la totalité des compositeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des compositeurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteComposers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieComposers(), Messages.NULL_COMPOSERS, Messages.ERROR_WHILE_CLEARING_COMPOSERS);
    }

    @DELETE
    @Path("/musicians")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les compositeurs d’un film",
            description = "Supprime la totalité des compositeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des compositeurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteMusicians(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieMusicians(), Messages.NULL_MUSICIANS, Messages.ERROR_WHILE_CLEARING_MUSICIANS);
    }

    @DELETE
    @Path("/set-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les décorateurs de plateau d’un film",
            description = "Supprime la totalité des décorateurs de plateau associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des décorateurs de plateau supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteSetDesigners(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieSetDesigners(), Messages.NULL_SET_DESIGNERS, Messages.ERROR_WHILE_CLEARING_SET_DESIGNERS);
    }

    @DELETE
    @Path("/costume-designers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les costumiers d’un film",
            description = "Supprime la totalité des costumiers associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des costumiers supprimée avec succès",
                    content = @Content
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteCostumeDesigners(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieCostumeDesigners(), Messages.NULL_COSTUME_DESIGNERS, Messages.ERROR_WHILE_CLEARING_COSTUME_DESIGNERS);
    }

    @DELETE
    @Path("/photographers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les photographes d’un film",
            description = "Supprime la totalité des photographes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des photographes supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deletePhotographers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMoviePhotographers(), Messages.NULL_PHOTOGRAPHERS, Messages.ERROR_WHILE_CLEARING_PHOTOGRAPHERS);
    }

    @DELETE
    @Path("/editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les photographes d’un film",
            description = "Supprime la totalité des photographes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des photographes supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteEditors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieEditors(), Messages.NULL_EDITORS, Messages.ERROR_WHILE_CLEARING_EDITORS);
    }

    @DELETE
    @Path("/casters")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les directeurs de casting d’un film",
            description = "Supprime la totalité des directeurs de casting associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des directeurs de casting supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteCasters(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieCasters(), Messages.NULL_CASTERS, Messages.ERROR_WHILE_CLEARING_CASTERS);
    }

    @DELETE
    @Path("/artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les artistes d’un film",
            description = "Supprime la totalité des artistes associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des artistes supprimée avec succès",
                    content = @Content
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteArtists(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieArtists(), Messages.NULL_ARTISTS, Messages.ERROR_WHILE_CLEARING_ARTISTS);
    }

    @DELETE
    @Path("/sound-editors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les ingénieurs son d’un film",
            description = "Supprime la totalité des ingénieurs son associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des ingénieurs son supprimée avec succès",
                    content = @Content
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteSoundEditors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieSoundEditors(), Messages.NULL_SOUND_EDITORS, Messages.ERROR_WHILE_CLEARING_SOUND_EDITORS);
    }

    @DELETE
    @Path("/vfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les superviseurs des effets visuels d’un film",
            description = "Supprime la totalité des superviseurs des effets visuels associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des superviseurs des effets visuels supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteVfxSupervisors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(), Messages.NULL_VFX_SUPERVISORS, Messages.ERROR_WHILE_CLEARING_VFX_SUPERVISORS);
    }

    @DELETE
    @Path("/sfx-supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les superviseurs des effets spéciaux d’un film",
            description = "Supprime la totalité des superviseurs SFX des effets spéciaux associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des superviseurs des effets spéciaux supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteSfxSupervisors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieVfxSupervisors(), Messages.NULL_SFX_SUPERVISORS, Messages.ERROR_WHILE_CLEARING_SFX_SUPERVISORS);
    }

    @DELETE
    @Path("/makeup-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les maquilleurs d’un film",
            description = "Supprime la totalité des maquilleurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des maquilleurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteMakeupArtists(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieMakeupArtists(), Messages.NULL_MAKEUP_ARTISTS, Messages.ERROR_WHILE_CLEARING_MAKEUP_ARTISTS);
    }

    @DELETE
    @Path("/hair-dressers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les coiffeurs d’un film",
            description = "Supprime la totalité des coiffeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des coiffeurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteHairDressers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieHairDressers(), Messages.NULL_HAIRDRESSERS, Messages.ERROR_WHILE_CLEARING_HAIRDRESSERS);
    }

    @DELETE
    @Path("/stuntmen")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprimer tous les cascadeurs d’un film",
            description = "Supprime la totalité des cascadeurs associés à un film donné, identifié par son ID."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "Liste des cascadeurs supprimée avec succès"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Requête invalide (ID du film manquant ou invalide)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Uni<Response> deleteStuntmen(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, movie -> movie.getTechnicalTeam().getMovieStuntmen(), Messages.NULL_STUNTMEN, Messages.ERROR_WHILE_CLEARING_STUNTMEN);
    }

    /**
     * Supprime tous les techniciens d’un type donné pour un film spécifié.
     * <p>
     * Cette méthode effectue les étapes suivantes :
     * <ul>
     *     <li>Validation de l'identifiant du film via {@link ValidationUtils#validateIdOrThrow(Long, String)}.</li>
     *     <li>Appel au service {@link MovieTechnicianService#clearTechnicians} pour supprimer tous les techniciens correspondant du film.
     *     Cette opération renvoie un {@link Uni} indiquant la réussite de la suppression.</li>
     *     <li>Transformation du résultat en réponse HTTP 204 No Content.</li>
     * </ul>
     *
     * @param <T>                   le type de technicien du film étendant {@link MovieTechnician}
     * @param id                    l'identifiant du film dont les techniciens doivent être supprimés
     * @param techniciansGetter     une fonction permettant d'extraire la liste des techniciens existants d'un film
     * @param nullCheckErrorMessage message d'erreur à utiliser si la liste de techniciens est null
     * @param globalErrorMessage    message d'erreur global à utiliser en cas d'échec lors de la suppression des techniciens
     * @return un {@link Uni} de {@link Response} contenant HTTP 204 No Content en cas de succès
     */
    private <T extends MovieTechnician> Uni<Response> clearTechniciansByMovieResponse(
            Long id,
            Function<Movie, List<T>> techniciansGetter,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, techniciansGetter, nullCheckErrorMessage, globalErrorMessage)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Prépare une personne en tant que technicien de film et retourne une fonction asynchrone
     * capable de convertir un {@link MovieTechnicianDTO} en entité spécifique au film.
     * <p>
     * Cette méthode effectue les étapes suivantes :
     * <ul>
     *     <li>Appelle {@link PersonService#prepareAndPersistPerson(LitePersonDTO, PersonType)} pour valider et persister la personne
     *     avec le type de rôle fourni.</li>
     *     <li>Crée un technicien de film spécifique en utilisant {@code technicianFactory} avec le film et la personne persistée.</li>
     *     <li>Attribue le rôle défini dans le DTO au technicien créé.</li>
     *     <li>Retourne un {@link Uni} contenant le technicien prêt à être ajouté à un film.</li>
     * </ul>
     *
     * @param <T>               le type de technicien du film étendant {@link MovieTechnician}
     * @param personType        le type de personne (ex : réalisateur, compositeur, acteur, etc.)
     * @param technicianFactory une fonction qui crée une instance spécifique de technicien à partir d'un {@link Movie} et d'une {@link Person}
     * @return une fonction asynchrone ({@link BiFunction}) prenant un {@link Movie} et un {@link MovieTechnicianDTO} et renvoyant un {@link Uni} de technicien prêt à être utilisé
     */
    private <T extends MovieTechnician> BiFunction<Movie, MovieTechnicianDTO, Uni<T>> preparePerson(
            PersonType personType,
            BiFunction<Movie, Person, T> technicianFactory
    ) {
        return (movie, dto) ->
                personService.prepareAndPersistPerson(dto.getPerson(), personType)
                        .map(person -> {
                            T technician = technicianFactory.apply(movie, person);
                            technician.setRole(dto.getRole());
                            return technician;
                        })
                ;
    }
}
