package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.service.MovieTechnicianService;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Path("/movies/{id}")
@ApplicationScoped
@Slf4j
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
    public Uni<Response> getProducers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/directors")
    public Uni<Response> getDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/assistant-directors")
    public Uni<Response> getAssistantDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/screenwriters")
    public Uni<Response> getScreenwriters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/composers")
    public Uni<Response> getComposers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/musicians")
    public Uni<Response> getMusicians(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/photographers")
    public Uni<Response> getPhotographers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/costume-designers")
    public Uni<Response> getCostumeDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/set-designers")
    public Uni<Response> getSetDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/editors")
    public Uni<Response> getEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/casters")
    public Uni<Response> getCasters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/artists")
    public Uni<Response> getArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les ingénieurs du son associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des ingénieurs du son associés à un film donné, en fonction de l'identifiant
     * du film fourni dans l'URL. Si la liste des ingénieurs du son est vide, une réponse avec le statut HTTP 204 (No Content)
     * est renvoyée. Si des ingénieurs du son sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des ingénieurs
     * du son est renvoyée.
     * <p>
     * La récupération des ingénieurs du son est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des ingénieurs du son du film.
     *
     * @param id L'identifiant du film pour lequel les ingénieurs du son doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun ingénieur du son n'est trouvé, une réponse avec le statut HTTP 204
     * est renvoyée. Sinon, une réponse avec le statut HTTP 200 et la liste des ingénieurs du son est renvoyée.
     */
    @GET
    @Path("/sound-editors")
    public Uni<Response> getSoundEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les superviseurs des effets visuels associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des superviseurs des effets visuels associés à un film donné, en fonction de l'identifiant
     * du film fourni dans l'URL. Si la liste des superviseurs des effets visuels est vide, une réponse avec le statut HTTP 204 (No Content)
     * est renvoyée. Si des superviseurs sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des superviseurs est renvoyée.
     * <p>
     * La récupération des superviseurs des effets visuels est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des superviseurs du film.
     *
     * @param id L'identifiant du film pour lequel les superviseurs des effets visuels doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun superviseur des effets visuels n'est trouvé, une réponse avec le statut HTTP 204
     * est renvoyée. Sinon, une réponse avec le statut HTTP 200 et la liste des superviseurs est renvoyée.
     */
    @GET
    @Path("/vfx-supervisors")
    public Uni<Response> getVfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les superviseurs des effets spéciaux associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des superviseurs des effets spéciaux associés à un film donné, en fonction de l'identifiant
     * du film fourni dans l'URL. Si la liste des superviseurs des effets spéciaux est vide, une réponse avec le statut HTTP 204 (No Content)
     * est renvoyée. Si des superviseurs sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des superviseurs est renvoyée.
     * <p>
     * La récupération des superviseurs des effets spéciaux est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des superviseurs du film.
     *
     * @param id L'identifiant du film pour lequel les superviseurs des effets spéciaux doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun superviseur des effets spéciaux n'est trouvé, une réponse avec le statut HTTP 204
     * est renvoyée. Sinon, une réponse avec le statut HTTP 200 et la liste des superviseurs est renvoyée.
     */
    @GET
    @Path("/sfx-supervisors")
    public Uni<Response> getSfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieSfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les maquilleurs associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des maquilleurs associés à un film donné, en fonction de l'identifiant du film
     * fourni dans l'URL. Si la liste des maquilleurs est vide, une réponse avec le statut HTTP 204 (No Content) est renvoyée.
     * Si des maquilleurs sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des maquilleurs est renvoyée.
     * <p>
     * La récupération des maquilleurs est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des maquilleurs du film.
     *
     * @param id L'identifiant du film pour lequel les maquilleurs doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun maquilleur n'est trouvé, une réponse avec le statut HTTP 204 est renvoyée.
     * Sinon, une réponse avec le statut HTTP 200 et la liste des maquilleurs est renvoyée.
     */
    @GET
    @Path("/makeup-artists")
    public Uni<Response> getMakeupArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les coiffeurs associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des coiffeurs associés à un film donné, en fonction de l'identifiant du film
     * fourni dans l'URL. Si la liste des coiffeurs est vide, une réponse avec le statut HTTP 204 (No Content) est renvoyée.
     * Si des coiffeurs sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des coiffeurs est renvoyée.
     * <p>
     * La récupération des coiffeurs est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des coiffeurs du film.
     *
     * @param id L'identifiant du film pour lequel les coiffeurs doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun coiffeur n'est trouvé, une réponse avec le statut HTTP 204 est renvoyée.
     * Sinon, une réponse avec le statut HTTP 200 et la liste des coiffeurs est renvoyée.
     */
    @GET
    @Path("/hair-dressers")
    public Uni<Response> getHairDressers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les cascadeurs associés à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des cascadeurs associés à un film donné, en fonction de l'identifiant du film
     * fourni dans l'URL. Si la liste des cascadeurs est vide, une réponse avec le statut HTTP 204 (No Content) est renvoyée.
     * Si des cascadeurs sont trouvés, une réponse avec le statut HTTP 200 (OK) contenant la liste des cascadeurs est renvoyée.
     * <p>
     * La récupération des cascadeurs est effectuée par l'appel au service correspondant et l'accès à la méthode
     * spécifiée pour obtenir la liste des cascadeurs du film.
     *
     * @param id L'identifiant du film pour lequel les cascadeurs doivent être récupérés.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucun cascadeur n'est trouvé, une réponse avec le statut HTTP 204 est renvoyée.
     * Sinon, une réponse avec le statut HTTP 200 et la liste des cascadeurs est renvoyée.
     */
    @GET
    @Path("/stuntmen")
    public Uni<Response> getStuntmen(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    private <T extends MovieTechnician> Uni<Response> getTechnicians(Long id, Function<Movie, List<T>> techniciansGetter, String message) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.getMovieTechniciansByMovie(id, techniciansGetter, message)
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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieProducers,
                                preparePerson(PersonType.PRODUCER, MovieProducer::build),
                                Messages.PRODUCERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieDirectors,
                                preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                                Messages.DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieAssistantDirectors,
                                preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                                Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieScreenwriters,
                                preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                                Messages.SCREENWRITERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieComposers,
                                preparePerson(PersonType.COMPOSER, MovieComposer::build),
                                Messages.COMPOSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMusicians,
                                preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                                Messages.MUSICIANS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> savePhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMoviePhotographers,
                                preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                                Messages.PHOTOGRAPHERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCostumeDesigners,
                                preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                                Messages.COSTUME_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSetDesigners,
                                preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                                Messages.SET_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieEditors,
                                preparePerson(PersonType.EDITOR, MovieEditor::build),
                                Messages.EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des directeurs de casting ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCasters,
                                preparePerson(PersonType.CASTER, MovieCaster::build),
                                Messages.CASTERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieArtists,
                                preparePerson(PersonType.ARTIST, MovieArtist::build),
                                Messages.ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs son ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSoundEditors,
                                preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                                Messages.SOUND_EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets visuels ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieVfxSupervisors,
                                preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                                Messages.VFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSfxSupervisors,
                                preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                                Messages.SFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMakeupArtists,
                                preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                                Messages.MAKEUP_ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieHairDressers,
                                preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                                Messages.HAIRDRESSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieStuntmen,
                                preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                                Messages.STUNTMEN_NOT_INITIALIZED
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOS ->
                                movieTechnicianDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOS).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de producteurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les producteurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des producteurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieProducers,
                                preparePerson(PersonType.PRODUCER, MovieProducer::build),
                                Messages.PRODUCERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_PRODUCERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de réalisateurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les réalisateurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des réalisateurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieDirectors,
                                preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                                Messages.DIRECTORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_DIRECTORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste d'assistants réalisateurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les assistants réalisateurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des assistants réalisateurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des assistants réalisateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieAssistantDirectors,
                                preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                                Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_ASSISTANT_DIRECTORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de scénaristes à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les scénaristes doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des scénaristes à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieScreenwriters,
                                preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                                Messages.SCREENWRITERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_SCREENWRITERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de compositeurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les compositeurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des compositeurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des compositeurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieComposers,
                                preparePerson(PersonType.COMPOSER, MovieComposer::build),
                                Messages.COMPOSERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_COMPOSERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de musiciens à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les musiciens doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des musiciens à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMusicians,
                                preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                                Messages.MUSICIANS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_MUSICIANS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de photographes à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les photographes doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des photographes à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addPhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMoviePhotographers,
                                preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                                Messages.PHOTOGRAPHERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_PHOTOGRAPHERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de costumiers à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les costumiers doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des costumiers à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des costumiers si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCostumeDesigners,
                                preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                                Messages.COSTUME_DESIGNERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_COSTUME_DESIGNERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de décorateurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les décorateurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des décorateurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des décorateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSetDesigners,
                                preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                                Messages.SET_DESIGNERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_SET_DESIGNERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de monteurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les monteurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des monteurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des monteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieEditors,
                                preparePerson(PersonType.EDITOR, MovieEditor::build),
                                Messages.EDITORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_EDITORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de directeurs de casting à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les directeurs de casting doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des directeurs de casting à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des directeurs de casting si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des directeurs de casting ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCasters,
                                preparePerson(PersonType.CASTER, MovieCaster::build),
                                Messages.CASTERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_CASTERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de directeurs artistiques à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les directeurs artistiques doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des directeurs artistiques à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des directeurs artistiques si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieArtists,
                                preparePerson(PersonType.ARTIST, MovieArtist::build),
                                Messages.ARTISTS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_ARTISTS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste d'ingénieurs du son à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les ingénieurs du son doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des ingénieurs du son à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des ingénieurs du son si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs son ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSoundEditors,
                                preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                                Messages.SOUND_EDITORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_SOUND_EDITORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de spécialistes des effets visuels à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les spécialistes des effets visuels doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des spécialistes des effets visuels à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets visuels si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets visuels ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieVfxSupervisors,
                                preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                                Messages.VFX_SUPERVISORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_VFX_SUPERVISORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de spécialistes des effets spéciaux à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les spécialistes des effets spéciaux doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des spécialistes des effets spéciaux à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets spéciaux si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSfxSupervisors,
                                preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                                Messages.SFX_SUPERVISORS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_SFX_SUPERVISORS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de maquilleurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les maquilleurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des maquilleurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des maquilleurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMakeupArtists,
                                preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                                Messages.MAKEUP_ARTISTS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_MAKEUP_ARTISTS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de coiffeurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les coiffeurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des coiffeurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des coiffeurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieHairDressers,
                                preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                                Messages.HAIRDRESSERS_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_HAIRDRESSERS
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de cascadeurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les cascadeurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des cascadeurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des cascadeurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle");
        }

        return
                movieTechnicianService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieStuntmen,
                                preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                                Messages.STUNTMEN_NOT_INITIALIZED, Messages.ERROR_WHILE_ADDING_STUNTMEN
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un producteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du producteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/producers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieProducers,
                                Messages.PRODUCERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_PRODUCER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un réalisateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du réalisateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieDirectors,
                                Messages.DIRECTORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_DIRECTOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un assistant réalisateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant de l'assistant réalisateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des assistants réalisateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/assistant-directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeAssistantDirector(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieDirectors,
                                Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_ASSISTANT_DIRECTOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un scénariste d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du scénariste à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/screenwriters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieScreenwriters,
                                Messages.SCREENWRITERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_SCREENWRITER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un compositeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du compositeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des compositeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/composers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeComposer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieComposers,
                                Messages.COMPOSERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_COMPOSER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un musicien d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du musicien à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/musicians/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieMusicians,
                                Messages.MUSICIANS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_MUSICIAN
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un photographe d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du photographe à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/photographers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMoviePhotographers,
                                Messages.PHOTOGRAPHERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_PHOTOGRAPHER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un costumier d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du costumier à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des costumiers si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/costume-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCostumeDesigner(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieCostumeDesigners,
                                Messages.COSTUME_DESIGNERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_COSTUME_DESIGNER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un décorateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du décorateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des décorateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/set-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSetDesigner(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieSetDesigners,
                                Messages.SET_DESIGNERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_SET_DESIGNER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un monteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du monteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des monteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieEditors,
                                Messages.EDITORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_EDITOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un directeur de casting d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du directeur de casting à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des directeurs de casting si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/casters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieCasters,
                                Messages.CASTERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_CASTER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un artiste d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant de l'artiste à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des artistes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeArtist(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieArtists,
                                Messages.ARTISTS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_ARTIST
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un ingénieur du son d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant de l'ingénieur du son à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des ingénieurs du son si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/sound-editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieSoundEditors,
                                Messages.SOUND_EDITORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_SOUND_EDITOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un spécialiste des effets visuels d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du spécialiste des effets visuels à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets visuels si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/vfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeVfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieVfxSupervisors,
                                Messages.VFX_SUPERVISORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_VFX_SUPERVISOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un spécialiste des effets spéciaux d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du spécialiste des effets spéciaux à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets spéciaux si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/sfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieSfxSupervisors,
                                Messages.SFX_SUPERVISORS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_SFX_SUPERVISOR
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un maquilleur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du maquilleur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des maquilleurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/makeup-artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMakeupArtists(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieMakeupArtists,
                                Messages.MAKEUP_ARTISTS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_MAKEUP_ARTIST
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un coiffeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du coiffeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des coiffeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/hair-dressers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeHairDressers(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieHairDressers,
                                Messages.HAIRDRESSERS_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_HAIRDRESSER
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime un cascadeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du cascadeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des cascadeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/stuntmen/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieTechnicianService.removeTechnician(
                                movieId,
                                personId,
                                Movie::getMovieStuntmen,
                                Messages.STUNTMEN_NOT_INITIALIZED,
                                Messages.ERROR_WHILE_REMOVING_STUNTMAN
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                ;
    }

    /**
     * Supprime tous les producteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les producteurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les producteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les producteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des producteurs.
     */
    @DELETE
    @Path("/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteProducers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les réalisateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les réalisateurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les réalisateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les réalisateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des réalisateurs.
     */
    @DELETE
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les assistants réalisateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les assistants réalisateurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les assistants réalisateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les assistants réalisateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des assistants réalisateurs.
     */
    @DELETE
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteAssistantDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les scénaristes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les scénaristes associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les scénaristes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les scénaristes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des scénaristes.
     */
    @DELETE
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteScreenwriters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les compositeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les compositeurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les compositeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les compositeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des compositeurs.
     */
    @DELETE
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteComposers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les musiciens associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les musiciens associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les musiciens doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les musiciens ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des musiciens.
     */
    @DELETE
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteMusicians(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les décorateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les décorateurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les décorateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les décorateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des décorateurs.
     */
    @DELETE
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSetDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les costumiers associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les costumiers associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les costumiers doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les costumiers ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des costumiers.
     */
    @DELETE
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCostumeDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les photographes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les photographes associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les photographes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les photographes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des photographes.
     */
    @DELETE
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deletePhotographers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les monteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les monteurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les monteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les monteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des monteurs.
     */
    @DELETE
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les directeurs de casting associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les directeurs de casting associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les directeurs de casting doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les directeurs de casting ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des directeurs de casting.
     */
    @DELETE
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCasters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les artistes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les artistes associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les artistes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les artistes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des artistes.
     */
    @DELETE
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les ingénieurs du son associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les ingénieurs du son associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les ingénieurs du son doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les ingénieurs du son ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des ingénieurs du son.
     */
    @DELETE
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSoundEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les spécialistes des effets visuels associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les spécialistes des effets visuels associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les spécialistes des effets visuels doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les spécialistes des effets visuels ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des spécialistes des effets visuels.
     */
    @DELETE
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteVfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les spécialistes des effets spéciaux associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les spécialistes des effets spéciaux associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les spécialistes des effets spéciaux doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les spécialistes des effets spéciaux ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des spécialistes des effets spéciaux.
     */
    @DELETE
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les maquilleurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les maquilleurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les maquilleurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les maquilleurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des maquilleurs.
     */
    @DELETE
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteMakeupArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les coiffeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les coiffeurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les coiffeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les coiffeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des coiffeurs.
     */
    @DELETE
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteHairDressers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les cascadeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les cascadeurs associés à un film en appelant la méthode
     * {@link MovieTechnicianService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les cascadeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les cascadeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des cascadeurs.
     */
    @DELETE
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteStuntmen(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieTechnicianService.clearTechnicians(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

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
