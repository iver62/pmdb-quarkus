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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.service.MovieService;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Path("/movies")
@ApplicationScoped
@Slf4j
public class MovieResource {

    private final MovieService movieService;
    private final PersonService personService;

    @Inject
    public MovieResource(MovieService movieService, PersonService personService) {
        this.movieService = movieService;
        this.personService = personService;
    }

    /**
     * Récupère le nombre total de films correspondant aux critères de recherche spécifiés.
     * <p>
     * Cette méthode effectue une requête pour compter le nombre de films qui correspondent aux critères fournis dans l'objet
     * {@link MovieQueryParamsDTO}. Si des critères sont spécifiés, elle renvoie une réponse HTTP avec le statut 200 (OK)
     * contenant le nombre total de films correspondants. Si aucun film ne correspond aux critères, la méthode renverra également
     * une réponse HTTP 200 avec la valeur 0.
     *
     * @param queryParams Les paramètres de requête encapsulés dans un objet {@link MovieQueryParamsDTO}, qui contiennent
     *                    les critères de recherche pour filtrer les films.
     * @return Un {@link Uni} contenant une réponse HTTP 200 (OK) avec le nombre total de films correspondant aux critères.
     * Si aucun film ne correspond, la réponse contiendra 0.
     */
    @GET
    @Path("/count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> count(@BeanParam MovieQueryParamsDTO queryParams) {
        return
                movieService.count(CriteriasDTO.build(queryParams))
                        .map(aLong -> Response.ok(aLong).build());
    }

    /**
     * Récupère un film par son identifiant.
     * <p>
     * Cette méthode permet de récupérer les détails d'un film en fonction de son identifiant unique. Si le film existe,
     * elle renvoie une réponse HTTP avec le statut 200 (OK) contenant les informations du film. Si une erreur se produit
     * lors de la récupération du film (par exemple, film non trouvé ou problème interne), la méthode renvoie une réponse
     * HTTP avec le statut 500 (Internal Server Error).
     *
     * @param id L'identifiant du film à récupérer.
     * @return Un {@link Uni} contenant une réponse HTTP. Si le film est trouvé, la réponse contient le film avec le statut 200.
     * Si le film n'est pas trouvé ou une erreur se produit, la réponse contient un statut 500 (Internal Server Error).
     */
    @GET
    @Path("/{id}")
    public Uni<Response> getMovie(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(movieList ->
                                movieService.count(criteriasDTO)
                                        .map(total ->
                                                movieList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                ;
    }

    @GET
    @Path("/all")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAllMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getMovies(finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(movieList ->
                                movieService.count(criteriasDTO).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountriesInMovies(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                movieService.getCountriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(countryList ->
                                movieService.countCountriesInMovies(term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategoriesInMovies(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();

        return
                movieService.getCategoriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(categoryDTOList ->
                                movieService.countCategoriesInMovies(term).map(total ->
                                        categoryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(categoryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/title/{title}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByTitle(@RestPath String title) {
        if (Objects.isNull(title)) {
            throw new BadRequestException("Le titre ne peut pas être nul");
        }

        return
                movieService.getByTitle(title)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieList ->
                                movieList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/persons")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonsByMovie(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getPersonsByMovie(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(personDTOList ->
                                movieService.countPersonsByMovie(id, criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/actors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getActorsByMovie(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getActorsByMovie(id)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActors ->
                                movieActors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActors).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/technical-team")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getTechnicalTeam(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getTechnicalTeam(id)
                        .map(technicalTeam -> Response.ok(technicalTeam).build())
                ;
    }

    @GET
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getProducers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAssistantDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getScreenwriters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getComposers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMusicians(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPhotographers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCostumeDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSetDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCasters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
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
    @Path("/{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSoundEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
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
    @Path("/{id}/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getVfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
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
    @Path("/{id}/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
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
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMakeupArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
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
    @Path("/{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getHairDressers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
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
    @Path("/{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getStuntmen(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieTechnicianDTOList ->
                                movieTechnicianDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOList).build()
                        )
                ;
    }

    /**
     * Récupère les catégories associées à un film donné.
     *
     * @param id L'ID du film.
     * @return Une réponse HTTP :
     * - 200 (OK) avec la liste des catégories si elle n'est pas vide.
     * - 204 si la liste des catégories est vide.
     */
    @GET
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategories(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCategoriesByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    /**
     * Récupère les pays associés à un film donné.
     *
     * @param id L'ID du film.
     * @return Une réponse HTTP :
     * - 200 (OK) avec la liste des pays si elle contient des données.
     * - 204 si la liste des pays est vide.
     */
    @GET
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountries(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCountriesByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    /**
     * Récupère les récompenses associées à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des récompenses associées à un film donné, en fonction de l'identifiant du film
     * fourni dans l'URL. Si la liste des récompenses est vide, une réponse avec le statut HTTP 204 (No Content) est renvoyée.
     * Si des récompenses sont trouvées, une réponse avec le statut HTTP 200 (OK) contenant la liste des récompenses est renvoyée.
     *
     * @param id L'identifiant du film pour lequel les récompenses doivent être récupérées.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucune récompense n'est trouvée, une réponse avec le statut HTTP 204 est renvoyée.
     * Sinon, une réponse avec le statut HTTP 200 et la liste des récompenses est renvoyée.
     */
    @GET
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCeremoniesAwards(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCeremoniesAwardsByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(ceremonyAwardsDTOS ->
                                ceremonyAwardsDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTOS).build()
                        )
                ;
    }

    @GET
    @Path("/creation-date-evolution")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesCreationDateEvolution() {
        return
                movieService.getMoviesCreationDateEvolution()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/creation-date-repartition")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesRepartitionByCreationDate() {
        return
                movieService.getMoviesCreationDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/decade-repartition")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesRepartitionByDecade() {
        return
                movieService.getMoviesReleaseDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/posters/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPoster(String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty() || Objects.equals("undefined", fileName)) {
            log.warn("Invalid file request: {}", fileName);
            throw new BadRequestException("Invalid file name");
        }

        return
                movieService.getPoster(fileName)
                        .onItem().ifNotNull().transform(
                                file -> {
                                    try {
                                        byte[] fileBytes = Files.readAllBytes(file.toPath());
                                        String mimeType = Files.probeContentType(file.toPath()); // Détecte automatiquement le type MIME

                                        log.info("Serving poster: {}", fileName);
                                        return Response.ok(fileBytes).type(mimeType).build();
                                    } catch (IOException e) {
                                        log.error("Error loading poster {}: {}", fileName, e.getMessage());
                                        return Response.serverError().entity("Erreur lors du chargement de l'affiche").build();
                                    }
                                }
                        )
                        .onItem().ifNull().continueWith(() -> {
                            log.warn("Poster not found: {}", fileName);
                            return Response.status(Response.Status.NOT_FOUND).entity("Affiche introuvable").build();
                        })
                ;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> create(@RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO) {
        if (Objects.isNull(movieDTO)) {
            throw new BadRequestException("Aucune information sur le film n’a été fournie dans la requête");
        }

        if (Objects.nonNull(movieDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        if (StringUtils.isBlank(movieDTO.getTitle())) {
            throw new BadRequestException("Le titre du film n’a pas été fourni dans la requête");
        }

        return
                movieService.saveMovie(file, movieDTO)
                        .map(movie -> Response.status(CREATED).entity(movie).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> update(@RestPath Long id, @RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieDTO)) {
            throw new BadRequestException("Aucune information sur le film n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(movieDTO.getTitle())) {
            throw new BadRequestException("Le titre du film n’a pas été fourni dans la requête");
        }

        if (!Objects.equals(id, movieDTO.getId())) {
            throw new WebApplicationException("L'identifiant du film ne correspond pas à celui de la requête", 422);
        }

        return movieService.updateMovie(id, file, movieDTO)
                .map(entity -> Response.ok(entity).build());
    }

    @PUT
    @Path("/{id}/cast")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCast(@RestPath Long id, List<MovieActorDTO> movieActorsList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieActorsList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle");
        }

        return
                movieService.saveCast(
                                id,
                                movieActorsList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, StringUtils.defaultString(dto.getRole()).trim(), dto.getRank()))
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOS ->
                                movieActorDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOS).build()
                        )
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

    @PUT
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> savePhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des casteurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs son ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets visuels ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
    @Path("/{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle");
        }

        return
                movieService.saveTechnicians(
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
     * Met à jour les catégories associées à un film donné.
     * <p>
     * Cette méthode permet d'ajouter ou de mettre à jour les catégories d'un film
     * en fonction des identifiants fournis.
     *
     * @param id           L'identifiant du film dont les catégories doivent être mises à jour.
     * @param categoryDTOS Un ensemble de {@link CategoryDTO} représentant les catégories à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des catégories mises à jour.
     * - `204 No Content` si aucune catégorie n'est associée.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des catégories est `null`.
     */
    @PUT
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCategories(@RestPath Long id, Set<CategoryDTO> categoryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle");
        }

        return
                movieService.saveCategories(id, categoryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    /**
     * Met à jour les pays associés à un film donné.
     * <p>
     * Cette méthode permet de mettre à jour les pays d'un film en fonction des identifiants fournis.
     *
     * @param id          L'identifiant du film dont les pays doivent être mis à jour.
     * @param countryDTOS Un ensemble de {@link CountryDTO} représentant les pays à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des pays mise à jour.
     * - `204 No Content` si aucun pays n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des pays est `null`.
     */
    @PUT
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCountries(@RestPath Long id, Set<CountryDTO> countryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle");
        }

        return
                movieService.saveCountries(id, countryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                ;
    }

    /**
     * Met à jour les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de mettre à jour les récompenses d'un film en fonction des identifiants fournis.
     *
     * @param id                L'identifiant du film dont les récompenses doivent être mis à jour.
     * @param ceremonyAwardsDTO Un {@link CeremonyAwardsDTO} représentant les récompenses à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des récompenses mise à jour.
     * - `204 No Content` si aucune récompense n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des récompenses est `null`.
     */
    @PUT
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCeremonyAwards(@RestPath Long id, CeremonyAwardsDTO ceremonyAwardsDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(ceremonyAwardsDTO)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle");
        }

        return
                movieService.saveCeremonyAwards(id, ceremonyAwardsDTO)
                        .map(dto -> Response.ok(dto).build())
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
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addPhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
     * Ajoute une liste de casteurs à un film spécifique.
     *
     * @param id                     L'identifiant du film auquel les casteurs doivent être ajoutés.
     * @param movieTechnicianDTOList La liste des casteurs à ajouter sous forme de {@link MovieTechnicianDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des casteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("/{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des casteurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs son ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets visuels ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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
    @Path("/{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle");
        }

        return
                movieService.addTechnicians(
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

    @PATCH
    @Path("/{id}/roles")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMovieActors(@RestPath Long id, List<MovieActorDTO> movieActorDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieActorDTOList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle");
        }

        return
                movieService.addMovieActors(
                                id,
                                movieActorDTOList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, dto.getRole(), dto.getRank()))
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                ;
    }

    /**
     * Ajoute un ensemble de catégories à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les catégories doivent être ajoutées.
     * @param categoryDTOS L'ensemble des catégories à ajouter, représentées sous forme de DTO.
     * @return Une réponse HTTP contenant le film mis à jour avec ses nouvelles catégories :
     * - 200 OK si l'opération réussit et retourne l'entité mise à jour.
     * - 500 Server Error si l'ajout échoue.
     */
    @PATCH
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCategories(@RestPath Long id, Set<CategoryDTO> categoryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle");
        }

        return
                movieService.addCategories(id, categoryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    /**
     * Ajoute une liste de pays associés à un film.
     *
     * @param id          L'identifiant du film auquel les pays doivent être ajoutés.
     * @param countryDTOS Un ensemble d'objets {@link CountryDTO} représentant les pays à associer au film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si l'ajout est réussi.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCountries(@RestPath Long id, Set<CountryDTO> countryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle");
        }

        return
                movieService.addCountries(id, countryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
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
    @Path("/{movieId}/producers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/assistant-directors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeAssistantDirector(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/screenwriters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/composers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeComposer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/musicians/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/photographers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/costume-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCostumeDesigner(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/set-designers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSetDesigner(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
     * Supprime un casteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param personId L'identifiant du casteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des casteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/casters/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeArtist(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/sound-editors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/vfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeVfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/sfx-supervisors/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSfxSupervisor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/makeup-artists/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMakeupArtists(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/hair-dressers/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeHairDressers(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
    @Path("/{movieId}/stuntmen/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeTechnician(
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
     * Supprime un acteur associé à un film donné.
     *
     * @param movieId  L'identifiant du film dont l'acteur doit être supprimé.
     * @param personId L'identifiant de l'association acteur-film à supprimer.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des acteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/roles/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMovieActor(@RestPath Long movieId, @RestPath Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeMovieActor(movieId, personId)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                ;
    }

    /**
     * Supprime une catégorie spécifique d'un film donné.
     *
     * @param movieId    L'identifiant du film dont la catégorie doit être supprimée.
     * @param categoryId L'identifiant de la catégorie à supprimer.
     * @return Une réponse HTTP contenant le film mis à jour après la suppression de la catégorie :
     * - 200 OK si la suppression est réussie et retourne l'entité mise à jour.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{movieId}/categories/{categoryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCategory(@RestPath Long movieId, @RestPath Long categoryId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(categoryId, Messages.INVALID_CATEGORY_ID);

        return
                movieService.removeCategory(movieId, categoryId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    /**
     * Supprime l'association d'un pays avec un film donné.
     *
     * @param movieId   L'identifiant du film concerné.
     * @param countryId L'identifiant du pays à dissocier du film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si la suppression est réussie.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{movieId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCountry(@RestPath Long movieId, @RestPath Long countryId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(countryId, Messages.INVALID_COUNTRY_ID);

        return
                movieService.removeCountry(movieId, countryId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                ;
    }

    /**
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de supprimer toutes les récompenses associées à un film en appelant la méthode
     * {@link MovieService#removeCeremonyAwards(Long, Long)} (Long)} (Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param movieId L'identifiant du film dont les récompenses doivent être supprimées.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les récompenses ont été supprimées avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des récompenses.
     */
    @PATCH
    @Path("/{movieId}/ceremonies-awards/{ceremonyAwardsId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCeremonyAwards(@RestPath Long movieId, @RestPath Long ceremonyAwardsId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(ceremonyAwardsId, Messages.INVALID_CEREMONY_AWARDS_ID);

        return
                movieService.removeCeremonyAwards(movieId, ceremonyAwardsId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(ceremonyAwardsDTOSet ->
                                ceremonyAwardsDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTOSet).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> delete(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.deleteMovie(id)
                        .map(deleted ->
                                Boolean.TRUE.equals(deleted)
                                        ? Response.noContent().build()
                                        : Response.status(Response.Status.NOT_FOUND).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}/actors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteActors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearActors(id)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les producteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les producteurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)} (Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les producteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les producteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des producteurs.
     */
    @DELETE
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteProducers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les réalisateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les réalisateurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les réalisateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les réalisateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des réalisateurs.
     */
    @DELETE
    @Path("/{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les assistants réalisateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les assistants réalisateurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les assistants réalisateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les assistants réalisateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des assistants réalisateurs.
     */
    @DELETE
    @Path("/{id}/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteAssistantDirectors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les scénaristes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les scénaristes associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les scénaristes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les scénaristes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des scénaristes.
     */
    @DELETE
    @Path("/{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteScreenwriters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les compositeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les compositeurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les compositeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les compositeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des compositeurs.
     */
    @DELETE
    @Path("/{id}/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteComposers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les musiciens associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les musiciens associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les musiciens doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les musiciens ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des musiciens.
     */
    @DELETE
    @Path("/{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteMusicians(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les décorateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les décorateurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les décorateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les décorateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des décorateurs.
     */
    @DELETE
    @Path("/{id}/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSetDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les costumiers associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les costumiers associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les costumiers doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les costumiers ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des costumiers.
     */
    @DELETE
    @Path("/{id}/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCostumeDesigners(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les photographes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les photographes associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les photographes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les photographes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des photographes.
     */
    @DELETE
    @Path("/{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deletePhotographers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les monteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les monteurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les monteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les monteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des monteurs.
     */
    @DELETE
    @Path("/{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les casteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les casteurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les casteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les casteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des casteurs.
     */
    @DELETE
    @Path("/{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCasters(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les artistes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les artistes associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les artistes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les artistes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des artistes.
     */
    @DELETE
    @Path("/{id}/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteArtists(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les ingénieurs du son associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les ingénieurs du son associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les ingénieurs du son doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les ingénieurs du son ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des ingénieurs du son.
     */
    @DELETE
    @Path("/{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSoundEditors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les spécialistes des effets visuels associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les spécialistes des effets visuels associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les spécialistes des effets visuels doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les spécialistes des effets visuels ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des spécialistes des effets visuels.
     */
    @DELETE
    @Path("/{id}/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteVfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les spécialistes des effets spéciaux associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les spécialistes des effets spéciaux associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les spécialistes des effets spéciaux doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les spécialistes des effets spéciaux ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des spécialistes des effets spéciaux.
     */
    @DELETE
    @Path("/{id}/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteSfxSupervisors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les maquilleurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les maquilleurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
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
                movieService.clearTechnicians(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les coiffeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les coiffeurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les coiffeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les coiffeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des coiffeurs.
     */
    @DELETE
    @Path("/{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteHairDressers(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime tous les cascadeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les cascadeurs associés à un film en appelant la méthode
     * {@link MovieService#clearTechnicians(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les cascadeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les cascadeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des cascadeurs.
     */
    @DELETE
    @Path("/{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteStuntmen(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearTechnicians(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime toutes les catégories associées à un film donné.
     * <p>
     * Cette méthode permet de supprimer toutes les catégories associées à un film en appelant la méthode
     * {@link MovieService#clearCategories(Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les catégories doivent être supprimées.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les catégories ont été supprimées avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des catégories.
     */
    @DELETE
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCategories(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCategories(id)
                .map(deleted -> Response.noContent().build());
    }

    /**
     * Supprime tous les pays associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les pays associés à un film en appelant la méthode
     * {@link MovieService#clearCountries(Long)} (Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les pays doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les pays ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des pays.
     */
    @DELETE
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCountries(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCountries(id).map(deleted -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCeremoniesAwards(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCeremoniesAwards(id).map(deleted -> Response.noContent().build());
    }

}