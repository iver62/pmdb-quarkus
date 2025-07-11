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
import java.util.function.Function;

import static jakarta.ws.rs.core.Response.Status.*;

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
                        .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build());
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
        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération du film: {}", err.getMessage());
                                    return Response.serverError()
                                            .entity("Erreur lors de la récupération du film")
                                            .build();
                                }
                        )
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
                        .flatMap(movieList ->
                                movieService.count(criteriasDTO)
                                        .map(total ->
                                                movieList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des films: {}", err.getMessage());
                                    return Response.serverError()
                                            .entity("Erreur lors de la récupération des films")
                                            .build();
                                }
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
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des films: {}", err.getMessage());
                                    return Response.serverError()
                                            .entity("Erreur lors de la récupération des films")
                                            .build();
                                }
                        )
                ;
    }

    @GET
    @Path("/search")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> searchByTitle(@QueryParam("query") String query) {
        if (Objects.isNull(query) || query.trim().isEmpty()) {
            return Uni.createFrom()
                    .item(
                            Response
                                    .status(Response.Status.BAD_REQUEST)
                                    .entity("Le paramètre 'query' est requis")
                                    .build()
                    );
        }

        return
                movieService.searchByTitle(query)
                        .map(movieDTOS ->
                                movieDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieDTOS).build()
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
                        .flatMap(countryList ->
                                movieService.countCountriesInMovies(term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des pays: {}", err.getMessage());
                                    return Response.serverError()
                                            .entity("Erreur lors de la récupération des pays")
                                            .build();
                                }
                        )
                ;
    }

    @GET
    @Path("/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCategories(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();

        return
                movieService.getCategoriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term)
                        .flatMap(categoryDTOList ->
                                movieService.countCategoriesInMovies(term).map(total ->
                                        categoryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(categoryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la récupération des catégories: {}", err.getMessage());
                                    return Response.serverError()
                                            .entity("Erreur lors de la récupération des catégories")
                                            .build();
                                }
                        )
                ;
    }

    @GET
    @Path("/title/{title}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getByTitle(@RestPath String title) {
        return
                movieService.getByTitle(title)
                        .onItem().ifNotNull().transform(panacheEntityBases -> Response.ok(panacheEntityBases).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("/{id}/persons")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonsByMovie(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getPersonsByMovie(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
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
    public Uni<Response> getActors(@RestPath Long id) {
        return
                movieService.getActorsByMovie(id)
                        .onItem().ifNotNull().transform(movieActors ->
                                movieActors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActors).build()
                        )
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la récupération du casting: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la récupération du casting")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @GET
    @Path("/{id}/technical-team")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getTechnicalTeam(@RestPath Long id) {
        return
                movieService.getTechnicalTeam(id)
                        .map(technicalTeam -> Response.ok(technicalTeam).build())
                ;
    }

    @GET
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getProducers(@RestPath Long id) {
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieSfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
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
        return
                movieService.getMovieTechniciansByMovie(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
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
        return
                movieService.getCategoriesByMovie(id)
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
        return
                movieService.getCountriesByMovie(id)
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
        return
                movieService.getCeremoniesAwardsByMovie(id)
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

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> create(@RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO) {
        if (Objects.isNull(movieDTO) || Objects.nonNull(movieDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                movieService.saveMovie(file, movieDTO)
                        .map(movie -> Response.status(CREATED).entity(movie).build());
    }

    @GET
    @Path("/posters/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPoster(String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty() || Objects.equals("undefined", fileName)) {
            log.warn("Invalid file request: {}", fileName);
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid file name").build());
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

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> update(@RestPath Long id, @RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) MovieDTO movieDTO) {
        if (Objects.isNull(movieDTO) || Objects.isNull(movieDTO.getTitle())) {
            throw new WebApplicationException("Movie title was not set on request.", 422);
        }

        return
                movieService.updateMovie(id, file, movieDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la modification du film: " + e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la modification du film")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/cast")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCast(@RestPath Long id, List<MovieActorDTO> movieActorsList) {
        if (Objects.isNull(movieActorsList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle.");
        }

        return
                movieService.saveCast(
                                id,
                                movieActorsList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, StringUtils.defaultString(dto.getRole()).trim(), dto.getRank()))
                        )
                        .onItem().ifNotNull().transform(movieActorDTOList -> Response.ok(movieActorDTOList).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour du casting: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour du casting")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveProducers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieProducers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.PRODUCER)
                                        .map(person -> MovieProducer.build(movie, person, dto.getRole())),
                                Messages.PRODUCERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des producteurs: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des producteurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieDirectors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.DIRECTOR)
                                        .map(person -> MovieDirector.build(movie, person, dto.getRole())),
                                Messages.DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des réalisateurs: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des réalisateurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/assistant-directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveAssistantDirectors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieAssistantDirectors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ASSISTANT_DIRECTOR)
                                        .map(person -> MovieAssistantDirector.build(movie, person, dto.getRole())),
                                Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des assistants réalisateurs: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des assistants réalisateurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveScreenwriters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieScreenwriters,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SCREENWRITER)
                                        .map(person -> MovieScreenwriter.build(movie, person, dto.getRole())),
                                Messages.SCREENWRITERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des scénaristes: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des scénaristes")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/composers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveComposers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieComposers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.COMPOSER)
                                        .map(person -> MovieComposer.build(movie, person, dto.getRole())),
                                Messages.COMPOSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des compositeurs: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des compositeurs")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMusicians(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMusicians,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.MUSICIAN)
                                        .map(person -> MovieMusician.build(movie, person, dto.getRole())),
                                Messages.MUSICIANS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des musiciens: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la mise à jour des musiciens")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> savePhotographers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMoviePhotographers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.PHOTOGRAPHER)
                                        .map(person -> MoviePhotographer.build(movie, person, dto.getRole())),
                                Messages.PHOTOGRAPHERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des photographes")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/costume-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCostumeDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCostumeDesigners,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.COSTUME_DESIGNER)
                                        .map(person -> MovieCostumeDesigner.build(movie, person, dto.getRole())),
                                Messages.COSTUME_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des costumiers")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/set-designers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSetDesigners(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSetDesigners,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SET_DESIGNER)
                                        .map(person -> MovieSetDesigner.build(movie, person, dto.getRole())),
                                Messages.SET_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des décorateurs")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieEditors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.EDITOR)
                                        .map(person -> MovieEditor.build(movie, person, dto.getRole())),
                                Messages.EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des monteurs")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCasters(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des casteurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCasters,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.CASTER)
                                        .map(person -> MovieCaster.build(movie, person, dto.getRole())),
                                Messages.CASTERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des casteurs")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieArtists,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ARTIST)
                                        .map(person -> MovieArtist.build(movie, person, dto.getRole())),
                                Messages.ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des artistes")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSoundEditors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs son ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSoundEditors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SOUND_EDITOR)
                                        .map(person -> MovieSoundEditor.build(movie, person, dto.getRole())),
                                Messages.SOUND_EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des ingénieurs son")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveVfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets visuels ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieVfxSupervisors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.VFX_SUPERVISOR)
                                        .map(person -> MovieVfxSupervisor.build(movie, person, dto.getRole())),
                                Messages.VFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des spécialistes des effets visuels")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveSfxSupervisors(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSfxSupervisors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SFX_SUPERVISOR)
                                        .map(person -> MovieSfxSupervisor.build(movie, person, dto.getRole())),
                                Messages.SFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des spécialistes des effets spéciaux")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveMakeupArtists(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMakeupArtists,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.MAKEUP_ARTIST)
                                        .map(person -> MovieMakeupArtist.build(movie, person, dto.getRole())),
                                Messages.MAKEUP_ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des maquilleurs")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveHairDressers(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieHairDressers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.HAIR_DRESSER)
                                        .map(person -> MovieHairDresser.build(movie, person, dto.getRole())),
                                Messages.HAIRDRESSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des coiffeurs")
                                            .build();
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveStuntmen(@RestPath Long id, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieStuntmen,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.STUNT_MAN)
                                        .map(person -> MovieStuntman.build(movie, person, dto.getRole())),
                                Messages.STUNTMEN_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des cascadeurs")
                                            .build();
                                }
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
        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle.");
        }

        return
                movieService.saveCategories(id, categoryDTOS)
                        .onItem().ifNotNull().transform(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
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
        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle.");
        }

        return
                movieService.saveCountries(id, countryDTOS)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
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
    @Path("/{id}/ceremony-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCeremonyAwards(@RestPath Long id, CeremonyAwardsDTO ceremonyAwardsDTO) {
        if (Objects.isNull(ceremonyAwardsDTO)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle.");
        }

        return
                movieService.saveCeremonyAwards(id, ceremonyAwardsDTO)
                        .onItem().ifNotNull().transform(dto ->
                                Objects.isNull(dto)
                                        ? Response.noContent().build()
                                        : Response.ok(dto).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieProducers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.PRODUCER)
                                        .map(person -> MovieProducer.build(movie, person, dto.getRole())),
                                Messages.PRODUCERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieDirectors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.DIRECTOR)
                                        .map(person -> MovieDirector.build(movie, person, dto.getRole())),
                                Messages.DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des assistants réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieAssistantDirectors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ASSISTANT_DIRECTOR)
                                        .map(person -> MovieAssistantDirector.build(movie, person, dto.getRole())),
                                Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieScreenwriters,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SCREENWRITER)
                                        .map(person -> MovieScreenwriter.build(movie, person, dto.getRole())),
                                Messages.SCREENWRITERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des compositeurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieComposers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.COMPOSER)
                                        .map(person -> MovieComposer.build(movie, person, dto.getRole())),
                                Messages.COMPOSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMusicians,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.MUSICIAN)
                                        .map(person -> MovieMusician.build(movie, person, dto.getRole())),
                                Messages.MUSICIANS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMoviePhotographers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.PHOTOGRAPHER)
                                        .map(person -> MoviePhotographer.build(movie, person, dto.getRole())),
                                Messages.PHOTOGRAPHERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCostumeDesigners,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.COSTUME_DESIGNER)
                                        .map(person -> MovieCostumeDesigner.build(movie, person, dto.getRole())),
                                Messages.COSTUME_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSetDesigners,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SET_DESIGNER)
                                        .map(person -> MovieSetDesigner.build(movie, person, dto.getRole())),
                                Messages.SET_DESIGNERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieEditors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.EDITOR)
                                        .map(person -> MovieEditor.build(movie, person, dto.getRole())),
                                Messages.EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des casteurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieCasters,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.CASTER)
                                        .map(person -> MovieCaster.build(movie, person, dto.getRole())),
                                Messages.CASTERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des artistes ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieArtists,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ARTIST)
                                        .map(person -> MovieArtist.build(movie, person, dto.getRole())),
                                Messages.ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des ingénieurs du son ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSoundEditors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SOUND_EDITOR)
                                        .map(person -> MovieSoundEditor.build(movie, person, dto.getRole())),
                                Messages.SOUND_EDITORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieVfxSupervisors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.VFX_SUPERVISOR)
                                        .map(person -> MovieVfxSupervisor.build(movie, person, dto.getRole())),
                                Messages.VFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieSfxSupervisors,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.SFX_SUPERVISOR)
                                        .map(person -> MovieSfxSupervisor.build(movie, person, dto.getRole())),
                                Messages.SFX_SUPERVISORS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieMakeupArtists,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.MAKEUP_ARTIST)
                                        .map(person -> MovieMakeupArtist.build(movie, person, dto.getRole())),
                                Messages.MAKEUP_ARTISTS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieHairDressers,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.HAIR_DRESSER)
                                        .map(person -> MovieHairDresser.build(movie, person, dto.getRole())),
                                Messages.HAIRDRESSERS_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(movieTechnicianDTOList)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle.");
        }

        return
                movieService.addTechnicians(
                                id,
                                movieTechnicianDTOList,
                                Movie::getMovieStuntmen,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.STUNT_MAN)
                                        .map(person -> MovieStuntman.build(movie, person, dto.getRole())),
                                Messages.STUNTMEN_NOT_INITIALIZED
                        )
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @PATCH
    @Path("/{id}/roles")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addMovieActors(@RestPath Long id, List<MovieActorDTO> movieActorDTOList) {
        if (Objects.isNull(movieActorDTOList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle.");
        }

        return
                movieService.addMovieActors(
                                id,
                                movieActorDTOList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, dto.getRole(), dto.getRank()))
                        )
                        .onItem().ifNotNull().transform(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle.");
        }

        return
                movieService.addCategories(id, categoryDTOS)
                        .onItem().ifNotNull().transform(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle.");
        }

        return
                movieService.addCountries(id, countryDTOS)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Supprime un producteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param producerId L'identifiant du producteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/producers/{producerId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long producerId) {
        return
                movieService.removeTechnician(movieId, producerId, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du producteur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du producteur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un réalisateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param directorId L'identifiant du réalisateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/directors/{directorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long directorId) {
        return
                movieService.removeTechnician(movieId, directorId, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du réalisateur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du réalisateur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un assistant réalisateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId             L'identifiant du film concerné.
     * @param assistantDirectorId L'identifiant de l'assistant réalisateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des assistants réalisateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/assistant-directors/{assistantDirectorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeAssistantDirector(@RestPath Long movieId, @RestPath Long assistantDirectorId) {
        return
                movieService.removeTechnician(movieId, assistantDirectorId, Movie::getMovieDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression de l'assistant réalisateur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression de l'assistant réalisateur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un scénariste d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param screenwriterId L'identifiant du scénariste à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/screenwriters/{screenwriterId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long screenwriterId) {
        return
                movieService.removeTechnician(movieId, screenwriterId, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du scénariste: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du scénariste")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un compositeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param composerId L'identifiant du compositeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des compositeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/composers/{composerId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeComposer(@RestPath Long movieId, @RestPath Long composerId) {
        return
                movieService.removeTechnician(movieId, composerId, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du compositeur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du compositeur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un musicien d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param musicianId L'identifiant du musicien à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/musicians/{musicianId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long musicianId) {
        return
                movieService.removeTechnician(movieId, musicianId, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du musicien: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du musicien")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un photographe d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param photographerId L'identifiant du photographe à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/photographers/{photographerId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long photographerId) {
        return
                movieService.removeTechnician(movieId, photographerId, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du photographe: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du photographe")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un costumier d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId           L'identifiant du film concerné.
     * @param costumeDesignerId L'identifiant du costumier à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des costumiers si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/costume-designers/{costumeDesignerId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCostumeDesigner(@RestPath Long movieId, @RestPath Long costumeDesignerId) {
        return
                movieService.removeTechnician(movieId, costumeDesignerId, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du costumier: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du costumier")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un décorateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId       L'identifiant du film concerné.
     * @param setDesignerId L'identifiant du décorateur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des décorateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/set-designers/{setDesignerId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSetDesigner(@RestPath Long movieId, @RestPath Long setDesignerId) {
        return
                movieService.removeTechnician(movieId, setDesignerId, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du décorateur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du décorateur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un monteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param editorId L'identifiant du monteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des monteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/editors/{editorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long editorId) {
        return
                movieService.removeTechnician(movieId, editorId, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du monteur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du monteur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un casteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param casterId L'identifiant du casteur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des casteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/casters/{casterId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long casterId) {
        return
                movieService.removeTechnician(movieId, casterId, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du casteur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du casteur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un artiste d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param artistId L'identifiant de l'artiste à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des artistes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/artists/{artistId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeArtist(@RestPath Long movieId, @RestPath Long artistId) {
        return
                movieService.removeTechnician(movieId, artistId, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression de l'artiste: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression de l'artiste")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un ingénieur du son d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId         L'identifiant du film concerné.
     * @param soundDirectorId L'identifiant de l'ingénieur du son à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des ingénieurs du son si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/sound-editors/{soundDirectorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long soundDirectorId) {
        return
                movieService.removeTechnician(movieId, soundDirectorId, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression de l'ingénieur du son: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression de l'ingénieur du son")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un spécialiste des effets visuels d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId         L'identifiant du film concerné.
     * @param vfxSupervisorId L'identifiant du spécialiste des effets visuels à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets visuels si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/vfx-supervisors/{vfxSupervisorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeVfxSupervisor(@RestPath Long movieId, @RestPath Long vfxSupervisorId) {
        return
                movieService.removeTechnician(movieId, vfxSupervisorId, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du spécialiste des effets visuels: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du spécialiste des effets visuels")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un spécialiste des effets spéciaux d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId         L'identifiant du film concerné.
     * @param sfxSupervisorId L'identifiant du spécialiste des effets spéciaux à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets spéciaux si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/sfx-supervisors/{sfxSupervisorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeSfxSupervisor(@RestPath Long movieId, @RestPath Long sfxSupervisorId) {
        return
                movieService.removeTechnician(movieId, sfxSupervisorId, Movie::getMovieSfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du spécialiste des effets spéciaux: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du spécialiste des effets spéciaux")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un maquilleur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param makeupArtistId L'identifiant du maquilleur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des maquilleurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/makeup-artists/{makeupArtistId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMakeupArtists(@RestPath Long movieId, @RestPath Long makeupArtistId) {
        return
                movieService.removeTechnician(movieId, makeupArtistId, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du maquilleur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du maquilleur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un coiffeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId       L'identifiant du film concerné.
     * @param hairDresserId L'identifiant du coiffeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des coiffeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/hair-dressers/{hairDresserId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeHairDressers(@RestPath Long movieId, @RestPath Long hairDresserId) {
        return
                movieService.removeTechnician(movieId, hairDresserId, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du coiffeur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du coiffeur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un cascadeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param stuntmanId L'identifiant du cascadeur à supprimer du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des cascadeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/stuntmen/{stuntmanId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long stuntmanId) {
        return
                movieService.removeTechnician(movieId, stuntmanId, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .onItem().ifNotNull().transform(movieTechnicianDTOs ->
                                movieTechnicianDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieTechnicianDTOs).build()
                        )
                        .onFailure().recoverWithItem(err -> {
                                    log.error("Erreur lors de la suppression du cascadeur: {}", err.getMessage());
                                    return
                                            Response
                                                    .serverError()
                                                    .entity("Erreur lors de la suppression du cascadeur")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    /**
     * Supprime un acteur associé à un film donné.
     *
     * @param movieId      L'identifiant du film dont l'acteur doit être supprimé.
     * @param movieActorId L'identifiant de l'association acteur-film à supprimer.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des acteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/roles/{movieActorId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMovieActor(@RestPath Long movieId, @RestPath Long movieActorId) {
        return
                movieService.removeMovieActor(movieId, movieActorId)
                        .onItem().ifNotNull().transform(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        return
                movieService.removeCategory(movieId, categoryId)
                        .onItem().ifNotNull().transform(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
        return
                movieService.removeCountry(movieId, countryId)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
    @DELETE
    @Path("/{movieId}/ceremony-awards/{ceremonyAwardsId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCeremonyAwards(@RestPath Long movieId, @RestPath Long ceremonyAwardsId) {
        return
                movieService.removeCeremonyAwards(movieId, ceremonyAwardsId)
                        .map(deleted -> Response.ok(deleted).build())
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la suppression de la cérémonie")
                                            .build();
                                }
                        )
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> delete(@RestPath Long id) {
        return
                movieService.deleteMovie(id)
                        .map(deleted ->
                                Response.ok()
                                        .status(Boolean.TRUE.equals(deleted) ? NO_CONTENT : NOT_FOUND)
                                        .build()
                        )
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
        return
                movieService.clearTechnicians(id, Movie::getMovieProducers, Messages.PRODUCERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieDirectors, Messages.DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieAssistantDirectors, Messages.ASSISTANT_DIRECTORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieScreenwriters, Messages.SCREENWRITERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieComposers, Messages.COMPOSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieMusicians, Messages.MUSICIANS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieSetDesigners, Messages.SET_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieCostumeDesigners, Messages.COSTUME_DESIGNERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMoviePhotographers, Messages.PHOTOGRAPHERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieEditors, Messages.EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieCasters, Messages.CASTERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieArtists, Messages.ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieSoundEditors, Messages.SOUND_EDITORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.VFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieVfxSupervisors, Messages.SFX_SUPERVISORS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieMakeupArtists, Messages.MAKEUP_ARTISTS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieHairDressers, Messages.HAIRDRESSERS_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return
                movieService.clearTechnicians(id, Movie::getMovieStuntmen, Messages.STUNTMEN_NOT_INITIALIZED)
                        .map(deleted -> Response.ok(deleted).build())
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
        return movieService.clearCategories(id).map(deleted -> Response.ok(deleted).build());
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
        return movieService.clearCountries(id).map(deleted -> Response.ok(deleted).build());
    }

    @DELETE
    @Path("/{id}/ceremony-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCeremonyAwards(@RestPath Long id) {
        return movieService.clearCeremonyAwards(id).map(deleted -> Response.ok(deleted).build());
    }

}