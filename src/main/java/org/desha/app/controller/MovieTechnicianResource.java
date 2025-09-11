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
        return getTechniciansByMovieResponse(id, Movie::getMovieProducers, Messages.NULL_PRODUCERS, Messages.ERROR_WHILE_GETTING_PRODUCERS);
    }

    @GET
    @Path("/directors")
    public Uni<Response> getDirectors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieDirectors, Messages.NULL_DIRECTORS, Messages.ERROR_WHILE_GETTING_DIRECTORS);
    }

    @GET
    @Path("/assistant-directors")
    public Uni<Response> getAssistantDirectors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieAssistantDirectors, Messages.NULL_ASSISTANT_DIRECTORS, Messages.ERROR_WHILE_GETTING_ASSISTANT_DIRECTORS);
    }

    @GET
    @Path("/screenwriters")
    public Uni<Response> getScreenwriters(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieScreenwriters, Messages.NULL_SCREENWRITERS, Messages.ERROR_WHILE_GETTING_SCREENWRITERS);
    }

    @GET
    @Path("/composers")
    public Uni<Response> getComposers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieComposers, Messages.NULL_COMPOSERS, Messages.ERROR_WHILE_GETTING_COMPOSERS);
    }

    @GET
    @Path("/musicians")
    public Uni<Response> getMusicians(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieMusicians, Messages.NULL_MUSICIANS, Messages.ERROR_WHILE_GETTING_MUSICIANS);
    }

    @GET
    @Path("/photographers")
    public Uni<Response> getPhotographers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMoviePhotographers, Messages.NULL_PHOTOGRAPHERS, Messages.ERROR_WHILE_GETTING_PHOTOGRAPHERS);
    }

    @GET
    @Path("/costume-designers")
    public Uni<Response> getCostumeDesigners(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieCostumeDesigners, Messages.NULL_COSTUME_DESIGNERS, Messages.ERROR_WHILE_GETTING_COSTUME_DESIGNERS);
    }

    @GET
    @Path("/set-designers")
    public Uni<Response> getSetDesigners(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieSetDesigners, Messages.NULL_SET_DESIGNERS, Messages.ERROR_WHILE_GETTING_SET_DESIGNERS);
    }

    @GET
    @Path("/editors")
    public Uni<Response> getEditors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieEditors, Messages.NULL_EDITORS, Messages.ERROR_WHILE_GETTING_EDITORS);
    }

    @GET
    @Path("/casters")
    public Uni<Response> getCasters(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieCasters, Messages.NULL_CASTERS, Messages.ERROR_WHILE_GETTING_CASTERS);
    }

    @GET
    @Path("/artists")
    public Uni<Response> getArtists(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieArtists, Messages.NULL_ARTISTS, Messages.ERROR_WHILE_GETTING_ARTISTS);
    }

    @GET
    @Path("/sound-editors")
    public Uni<Response> getSoundEditors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieSoundEditors, Messages.NULL_SOUND_EDITORS, Messages.ERROR_WHILE_GETTING_SOUND_EDITORS);
    }

    @GET
    @Path("/vfx-supervisors")
    public Uni<Response> getVfxSupervisors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieVfxSupervisors, Messages.NULL_VFX_SUPERVISORS, Messages.ERROR_WHILE_GETTING_VFX_SUPERVISORS);
    }

    @GET
    @Path("/sfx-supervisors")
    public Uni<Response> getSfxSupervisors(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieSfxSupervisors, Messages.NULL_SFX_SUPERVISORS, Messages.ERROR_WHILE_GETTING_SFX_SUPERVISORS);
    }

    @GET
    @Path("/makeup-artists")
    public Uni<Response> getMakeupArtists(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieMakeupArtists, Messages.NULL_MAKEUP_ARTISTS, Messages.ERROR_WHILE_GETTING_MAKEUP_ARTISTS);
    }

    @GET
    @Path("/hair-dressers")
    public Uni<Response> getHairDressers(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieHairDressers, Messages.NULL_HAIRDRESSERS, Messages.ERROR_WHILE_GETTING_HAIRDRESSERS);
    }

    @GET
    @Path("/stuntmen")
    public Uni<Response> getStuntmen(@RestPath Long id) {
        return getTechniciansByMovieResponse(id, Movie::getMovieStuntmen, Messages.NULL_STUNTMEN, Messages.ERROR_WHILE_GETTING_STUNTMEN);
    }

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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieProducers,
                        preparePerson(PersonType.PRODUCER, MovieProducer::build),
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_UPDATING_PRODUCERS
                );
    }

    @PUT
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieDirectors,
                        preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_UPDATING_DIRECTORS
                );
    }

    @PUT
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieAssistantDirectors,
                        preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_UPDATING_ASSISTANT_DIRECTORS
                );
    }

    @PUT
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieScreenwriters,
                        preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_UPDATING_SCREENWRITERS
                );
    }

    @PUT
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieComposers,
                        preparePerson(PersonType.COMPOSER, MovieComposer::build),
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_UPDATING_COMPOSERS
                );
    }

    @PUT
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieMusicians,
                        preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_UPDATING_MUSICIANS
                );
    }

    @PUT
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> savePhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMoviePhotographers,
                        preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_UPDATING_PHOTOGRAPHERS
                );
    }

    @PUT
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieCostumeDesigners,
                        preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_UPDATING_COSTUME_DESIGNERS
                );
    }

    @PUT
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSetDesigners,
                        preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_UPDATING_SET_DESIGNERS
                );
    }

    @PUT
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieEditors,
                        preparePerson(PersonType.EDITOR, MovieEditor::build),
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_UPDATING_EDITORS
                );
    }

    @PUT
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieCasters,
                        preparePerson(PersonType.CASTER, MovieCaster::build),
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_UPDATING_CASTERS
                );
    }

    @PUT
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieArtists,
                        preparePerson(PersonType.ARTIST, MovieArtist::build),
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_UPDATING_ARTISTS
                );
    }

    @PUT
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSoundEditors,
                        preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_UPDATING_SOUND_EDITORS
                );
    }

    @PUT
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieVfxSupervisors,
                        preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_UPDATING_VFX_SUPERVISORS
                );
    }

    @PUT
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSfxSupervisors,
                        preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_UPDATING_SFX_SUPERVISORS
                );
    }

    @PUT
    @Path("/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieMakeupArtists,
                        preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_UPDATING_MAKEUP_ARTISTS
                );
    }

    @PUT
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {

        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieHairDressers,
                        preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_UPDATING_HAIRDRESSERS
                );
    }

    @PUT
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                saveTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieStuntmen,
                        preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_UPDATING_STUNTMEN
                );
    }

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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieProducers,
                        preparePerson(PersonType.PRODUCER, MovieProducer::build),
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_ADDING_PRODUCERS
                );
    }

    @PATCH
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieDirectors,
                        preparePerson(PersonType.DIRECTOR, MovieDirector::build),
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_ADDING_DIRECTORS
                );
    }

    @PATCH
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieAssistantDirectors,
                        preparePerson(PersonType.ASSISTANT_DIRECTOR, MovieAssistantDirector::build),
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_ADDING_ASSISTANT_DIRECTORS
                );
    }

    @PATCH
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieScreenwriters,
                        preparePerson(PersonType.SCREENWRITER, MovieScreenwriter::build),
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_ADDING_SCREENWRITERS
                );
    }

    @PATCH
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieComposers,
                        preparePerson(PersonType.COMPOSER, MovieComposer::build),
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_ADDING_COMPOSERS
                );
    }

    @PATCH
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieMusicians,
                        preparePerson(PersonType.MUSICIAN, MovieMusician::build),
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_ADDING_MUSICIANS
                );
    }

    @PATCH
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addPhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMoviePhotographers,
                        preparePerson(PersonType.PHOTOGRAPHER, MoviePhotographer::build),
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_ADDING_PHOTOGRAPHERS
                );
    }

    @PATCH
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieCostumeDesigners,
                        preparePerson(PersonType.COSTUME_DESIGNER, MovieCostumeDesigner::build),
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_ADDING_COSTUME_DESIGNERS
                );
    }

    @PATCH
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSetDesigners,
                        preparePerson(PersonType.SET_DESIGNER, MovieSetDesigner::build),
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_ADDING_SET_DESIGNERS
                );
    }

    @PATCH
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieEditors,
                        preparePerson(PersonType.EDITOR, MovieEditor::build),
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_ADDING_EDITORS
                );
    }

    @PATCH
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieCasters,
                        preparePerson(PersonType.CASTER, MovieCaster::build),
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_ADDING_CASTERS
                );
    }

    @PATCH
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieArtists,
                        preparePerson(PersonType.ARTIST, MovieArtist::build),
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_ADDING_ARTISTS
                );
    }

    @PATCH
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSoundEditors,
                        preparePerson(PersonType.SOUND_EDITOR, MovieSoundEditor::build),
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_ADDING_SOUND_EDITORS
                );
    }

    @PATCH
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieVfxSupervisors,
                        preparePerson(PersonType.VFX_SUPERVISOR, MovieVfxSupervisor::build),
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_ADDING_VFX_SUPERVISORS
                );
    }

    @PATCH
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieSfxSupervisors,
                        preparePerson(PersonType.SFX_SUPERVISOR, MovieSfxSupervisor::build),
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_ADDING_SFX_SUPERVISORS
                );
    }

    @PATCH
    @Path("/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieMakeupArtists,
                        preparePerson(PersonType.MAKEUP_ARTIST, MovieMakeupArtist::build),
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_ADDING_MAKEUP_ARTISTS
                );
    }

    @PATCH
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieHairDressers,
                        preparePerson(PersonType.HAIR_DRESSER, MovieHairDresser::build),
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_ADDING_HAIRDRESSERS
                );
    }

    @PATCH
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        return
                addTechniciansByMovieResponse(
                        id,
                        movieTechnicianDTOList,
                        Movie::getMovieStuntmen,
                        preparePerson(PersonType.STUNT_MAN, MovieStuntman::build),
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_ADDING_STUNTMEN
                );
    }

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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieProducers,
                        Messages.NULL_PRODUCERS,
                        Messages.ERROR_WHILE_REMOVING_PRODUCER
                );
    }

    @PATCH
    @Path("/directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieDirectors,
                        Messages.NULL_DIRECTORS,
                        Messages.ERROR_WHILE_REMOVING_DIRECTOR
                );
    }

    @PATCH
    @Path("/assistant-directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeAssistantDirector(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieDirectors,
                        Messages.NULL_ASSISTANT_DIRECTORS,
                        Messages.ERROR_WHILE_REMOVING_ASSISTANT_DIRECTOR
                );
    }

    @PATCH
    @Path("/screenwriters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieScreenwriters,
                        Messages.NULL_SCREENWRITERS,
                        Messages.ERROR_WHILE_REMOVING_SCREENWRITER
                );
    }

    @PATCH
    @Path("/composers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeComposer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieComposers,
                        Messages.NULL_COMPOSERS,
                        Messages.ERROR_WHILE_REMOVING_COMPOSER
                );
    }

    @PATCH
    @Path("/musicians/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieMusicians,
                        Messages.NULL_MUSICIANS,
                        Messages.ERROR_WHILE_REMOVING_MUSICIAN
                );
    }

    @PATCH
    @Path("/photographers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMoviePhotographers,
                        Messages.NULL_PHOTOGRAPHERS,
                        Messages.ERROR_WHILE_REMOVING_PHOTOGRAPHER
                );
    }

    @PATCH
    @Path("/costume-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCostumeDesigner(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieCostumeDesigners,
                        Messages.NULL_COSTUME_DESIGNERS,
                        Messages.ERROR_WHILE_REMOVING_COSTUME_DESIGNER
                );
    }

    @PATCH
    @Path("/set-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSetDesigner(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieSetDesigners,
                        Messages.NULL_SET_DESIGNERS,
                        Messages.ERROR_WHILE_REMOVING_SET_DESIGNER
                );
    }

    @PATCH
    @Path("/editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieEditors,
                        Messages.NULL_EDITORS,
                        Messages.ERROR_WHILE_REMOVING_EDITOR
                );
    }

    @PATCH
    @Path("/casters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieCasters,
                        Messages.NULL_CASTERS,
                        Messages.ERROR_WHILE_REMOVING_CASTER
                );
    }

    @PATCH
    @Path("/artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeArtist(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieArtists,
                        Messages.NULL_ARTISTS,
                        Messages.ERROR_WHILE_REMOVING_ARTIST
                );
    }

    @PATCH
    @Path("/sound-editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieSoundEditors,
                        Messages.NULL_SOUND_EDITORS,
                        Messages.ERROR_WHILE_REMOVING_SOUND_EDITOR
                );
    }

    @PATCH
    @Path("/vfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeVfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieVfxSupervisors,
                        Messages.NULL_VFX_SUPERVISORS,
                        Messages.ERROR_WHILE_REMOVING_VFX_SUPERVISOR
                );
    }

    @PATCH
    @Path("/sfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieSfxSupervisors,
                        Messages.NULL_SFX_SUPERVISORS,
                        Messages.ERROR_WHILE_REMOVING_SFX_SUPERVISOR
                );
    }

    @PATCH
    @Path("/makeup-artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMakeupArtist(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieMakeupArtists,
                        Messages.NULL_MAKEUP_ARTISTS,
                        Messages.ERROR_WHILE_REMOVING_MAKEUP_ARTIST
                );
    }

    @PATCH
    @Path("/hair-dressers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeHairDresser(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieHairDressers,
                        Messages.NULL_HAIRDRESSERS,
                        Messages.ERROR_WHILE_REMOVING_HAIRDRESSER
                );
    }

    @PATCH
    @Path("/stuntmen/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long personId) {
        return
                removeTechnicianByMovieResponse(
                        movieId,
                        personId,
                        Movie::getMovieStuntmen,
                        Messages.NULL_STUNTMEN,
                        Messages.ERROR_WHILE_REMOVING_STUNTMAN
                );
    }

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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteProducers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieProducers, Messages.NULL_PRODUCERS, Messages.ERROR_WHILE_CLEARING_PRODUCERS);
    }

    @DELETE
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteDirectors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieDirectors, Messages.NULL_DIRECTORS, Messages.ERROR_WHILE_CLEARING_DIRECTORS);
    }

    @DELETE
    @Path("/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteAssistantDirectors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieAssistantDirectors, Messages.NULL_ASSISTANT_DIRECTORS, Messages.ERROR_WHILE_CLEARING_ASSISTANT_DIRECTORS);
    }

    @DELETE
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteScreenwriters(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieScreenwriters, Messages.NULL_SCREENWRITERS, Messages.ERROR_WHILE_CLEARING_SCREENWRITERS);
    }

    @DELETE
    @Path("/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteComposers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieComposers, Messages.NULL_COMPOSERS, Messages.ERROR_WHILE_CLEARING_COMPOSERS);
    }

    @DELETE
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteMusicians(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieMusicians, Messages.NULL_MUSICIANS, Messages.ERROR_WHILE_CLEARING_MUSICIANS);
    }

    @DELETE
    @Path("/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSetDesigners(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieSetDesigners, Messages.NULL_SET_DESIGNERS, Messages.ERROR_WHILE_CLEARING_SET_DESIGNERS);
    }

    @DELETE
    @Path("/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCostumeDesigners(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieCostumeDesigners, Messages.NULL_COSTUME_DESIGNERS, Messages.ERROR_WHILE_CLEARING_COSTUME_DESIGNERS);
    }

    @DELETE
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deletePhotographers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMoviePhotographers, Messages.NULL_PHOTOGRAPHERS, Messages.ERROR_WHILE_CLEARING_PHOTOGRAPHERS);
    }

    @DELETE
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteEditors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieEditors, Messages.NULL_EDITORS, Messages.ERROR_WHILE_CLEARING_EDITORS);
    }

    @DELETE
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCasters(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieCasters, Messages.NULL_CASTERS, Messages.ERROR_WHILE_CLEARING_CASTERS);
    }

    @DELETE
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteArtists(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieArtists, Messages.NULL_ARTISTS, Messages.ERROR_WHILE_CLEARING_ARTISTS);
    }

    @DELETE
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSoundEditors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieSoundEditors, Messages.NULL_SOUND_EDITORS, Messages.ERROR_WHILE_CLEARING_SOUND_EDITORS);
    }

    @DELETE
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteVfxSupervisors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieVfxSupervisors, Messages.NULL_VFX_SUPERVISORS, Messages.ERROR_WHILE_CLEARING_VFX_SUPERVISORS);
    }

    @DELETE
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSfxSupervisors(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieVfxSupervisors, Messages.NULL_SFX_SUPERVISORS, Messages.ERROR_WHILE_CLEARING_SFX_SUPERVISORS);
    }

    @DELETE
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteMakeupArtists(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieMakeupArtists, Messages.NULL_MAKEUP_ARTISTS, Messages.ERROR_WHILE_CLEARING_MAKEUP_ARTISTS);
    }

    @DELETE
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteHairDressers(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieHairDressers, Messages.NULL_HAIRDRESSERS, Messages.ERROR_WHILE_CLEARING_HAIRDRESSERS);
    }

    @DELETE
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteStuntmen(@RestPath Long id) {
        return clearTechniciansByMovieResponse(id, Movie::getMovieStuntmen, Messages.NULL_STUNTMEN, Messages.ERROR_WHILE_CLEARING_STUNTMEN);
    }

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
