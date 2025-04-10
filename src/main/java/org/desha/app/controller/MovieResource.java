package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.service.*;
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

@Path("movies")
@ApplicationScoped
@Slf4j
public class MovieResource {

    private final MovieService movieService;
    private final ArtDirectorService artDirectorService;
    private final CasterService casterService;
    private final PersonService<Costumier> costumierService;
    private final DecoratorService decoratorService;
    private final DirectorService directorService;
    private final EditorService editorService;
    private final HairDresserService hairDresserService;
    private final MakeupArtistService makeupArtistService;
    private final MusicianService musicianService;
    private final PhotographerService photographerService;
    private final ProducerService producerService;
    private final ScreenwriterService screenwriterService;
    private final SoundEditorService soundEditorService;
    private final StuntmanService stuntmanService;
    private final VisualEffectsSupervisorService visualEffectsSupervisorService;

    @Inject
    public MovieResource(
            MovieService movieService,
            ArtDirectorService artDirectorService,
            CasterService casterService,
            CostumierService costumierService,
            DecoratorService decoratorService,
            DirectorService directorService,
            EditorService editorService,
            HairDresserService hairDresserService,
            MakeupArtistService makeupArtistService,
            MusicianService musicianService,
            PhotographerService photographerService,
            ProducerService producerService,
            ScreenwriterService screenwriterService,
            SoundEditorService soundEditorService,
            StuntmanService stuntmanService,
            VisualEffectsSupervisorService visualEffectsSupervisorService
    ) {
        this.movieService = movieService;
        this.artDirectorService = artDirectorService;
        this.casterService = casterService;
        this.costumierService = costumierService;
        this.decoratorService = decoratorService;
        this.directorService = directorService;
        this.editorService = editorService;
        this.hairDresserService = hairDresserService;
        this.makeupArtistService = makeupArtistService;
        this.musicianService = musicianService;
        this.photographerService = photographerService;
        this.producerService = producerService;
        this.screenwriterService = screenwriterService;
        this.soundEditorService = soundEditorService;
        this.stuntmanService = stuntmanService;
        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
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
    @Path("count")
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
    @Path("{id}")
    public Uni<Response> getMovie(@RestPath Long id) {
        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @GET
    public Uni<Response> getMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, criteriasDTO)
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
    @Path("all")
    public Uni<Response> getAllMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getAllMovies(finalSort, sortDirection, criteriasDTO)
                        .flatMap(movieList ->
                                movieService.count(criteriasDTO).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("search")
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
    @Path("countries")
    public Uni<Response> getCountriesInMovies(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);

        return
                movieService.getCountriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, queryParams.getTerm())
                        .flatMap(countryList ->
                                movieService.countCountriesInMovies(queryParams.getTerm()).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("title/{title}")
    public Uni<Response> getByTitle(@RestPath String title) {
        return
                movieService.getByTitle(title)
                        .onItem().ifNotNull().transform(panacheEntityBases -> Response.ok(panacheEntityBases).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/actors")
    public Uni<Response> getActors(@RestPath Long id) {
        return
                movieService.getActorsByMovie(id)
                        .map(movieActors -> Response.ok(movieActors).build())
                ;
    }

    @GET
    @Path("{id}/technical-team")
    public Uni<Response> getTechnicalTeam(@RestPath Long id) {
        return
                movieService.getTechnicalTeam(id)
                        .map(technicalTeam -> Response.ok(technicalTeam).build())
//                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                ;
    }

    @GET
    @Path("{id}/producers")
    public Uni<Response> getProducers(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getProducers, producerService, "L'ensemble des producteurs n'est pas initialisé pour ce film")
                        .map(producers ->
                                producers.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(producers).build()
                        )
                ;
    }

    @GET
    @Path("{id}/directors")
    public Uni<Response> getDirectors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getDirectors, directorService, "L'ensemble des réalisateurs n'est pas initialisé pour ce film")
                        .map(directors ->
                                directors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(directors).build()
                        )
                ;
    }

    @GET
    @Path("{id}/screenwriters")
    public Uni<Response> getScreenwriters(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getScreenwriters, screenwriterService, "L'ensemble des scénaristes n'est pas initialisé pour ce film")
                        .map(screenwriters ->
                                screenwriters.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(screenwriters).build()
                        )
                ;
    }

    @GET
    @Path("{id}/musicians")
    public Uni<Response> getMusicians(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getMusicians, musicianService, "L'ensemble des musiciens n'est pas initialisé pour ce film")
                        .map(musicians ->
                                musicians.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(musicians).build()
                        )
                ;
    }

    @GET
    @Path("{id}/photographers")
    public Uni<Response> getPhotographers(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getPhotographers, photographerService, "L'ensemble des photographes n'est pas initialisé pour ce film")
                        .map(photographers ->
                                photographers.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(photographers).build()
                        )
                ;
    }

    @GET
    @Path("{id}/costumiers")
    public Uni<Response> getCostumiers(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getCostumiers, costumierService, "L'ensemble des costumiers n'est pas initialisé pour ce film")
                        .map(costumiers ->
                                costumiers.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(costumiers).build()
                        )
                ;
    }

    @GET
    @Path("{id}/decorators")
    public Uni<Response> getDecorators(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getDecorators, decoratorService, "L'ensemble des décorateurs n'est pas initialisé pour ce film")
                        .map(decorators ->
                                decorators.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(decorators).build()
                        )
                ;
    }

    @GET
    @Path("{id}/editors")
    public Uni<Response> getEditors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getEditors, editorService, "L'ensemble des monteurs n'est pas initialisé pour ce film")
                        .map(editors ->
                                editors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(editors).build()
                        )
                ;
    }

    @GET
    @Path("{id}/casters")
    public Uni<Response> getCasters(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getCasters, casterService, "L'ensemble des casteurs n'est pas initialisé pour ce film")
                        .map(casters ->
                                casters.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(casters).build()
                        )
                ;
    }

    @GET
    @Path("{id}/art-directors")
    public Uni<Response> getArtDirectors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getArtDirectors, artDirectorService, "L'ensemble des directeurs artistiques n'est pas initialisé pour ce film")
                        .map(artDirectors ->
                                artDirectors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(artDirectors).build()
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
    @Path("{id}/sound-editors")
    public Uni<Response> getSoundEditors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getSoundEditors, soundEditorService, "L'ensemble des ingénieurs du son n'est pas initialisé pour ce film")
                        .map(soundEditors ->
                                soundEditors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(soundEditors).build()
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
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> getVisualEffectsSupervisors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getVisualEffectsSupervisors, visualEffectsSupervisorService, "L'ensemble des spécialistes des effets spéciaux du son n'est pas initialisé pour ce film")
                        .map(visualEffectsSupervisors ->
                                visualEffectsSupervisors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(visualEffectsSupervisors).build()
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
    @Path("{id}/makeup-artists")
    public Uni<Response> getMakeupArtists(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getMakeupArtists, makeupArtistService, "L'ensemble des maquilleurs n'est pas initialisé pour ce film")
                        .map(makeupArtists ->
                                makeupArtists.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(makeupArtists).build()
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
    @Path("{id}/hair-dressers")
    public Uni<Response> getHairDressers(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getHairDressers, hairDresserService, "L'ensemble des coiffeurs n'est pas initialisé pour ce film")
                        .map(hairDressers ->
                                hairDressers.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(hairDressers).build()
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
    @Path("{id}/stuntmen")
    public Uni<Response> getStuntmen(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getStuntmen, stuntmanService, "L'ensemble des cascadeurs n'est pas initialisé pour ce film")
                        .map(stuntmen ->
                                stuntmen.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(stuntmen).build()
                        )
                ;
    }

    /**
     * Récupère les genres associés à un film donné.
     *
     * @param id L'ID du film.
     * @return Une réponse HTTP :
     * - 200 (OK) avec la liste des genres si elle n'est pas vide.
     * - 204 si la liste des genres est vide.
     */
    @GET
    @Path("{id}/genres")
    public Uni<Response> getGenres(@RestPath Long id) {
        return
                movieService.getGenresByMovie(id)
                        .map(genreDTOS ->
                                genreDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOS).build()
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
    @Path("{id}/countries")
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
    @Path("{id}/awards")
    public Uni<Response> getAwards(@RestPath Long id) {
        return
                movieService.getAwardsByMovie(id)
                        .map(awards ->
                                awards.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(awards).build()
                        )
                ;
    }

    @GET
    @Path("creation-date-evolution")
    public Uni<Response> getMoviesCreationDateEvolution() {
        return
                movieService.getMoviesCreationDateEvolution()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("creation-date-repartition")
    public Uni<Response> getMoviesRepartitionByCreationDate() {
        return
                movieService.getMoviesCreationDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("decade-repartition")
    public Uni<Response> getMoviesRepartitionByDecade() {
        return
                movieService.getMoviesReleaseDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("genre-repartition")
    public Uni<Response> getMoviesRepartitionByGenre() {
        return
                movieService.getMoviesGenresRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("country-repartition")
    public Uni<Response> getMoviesRepartitionByCountry() {
        return
                movieService.getMoviesCountriesRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("user-repartition")
    public Uni<Response> getMoviesRepartitionByUser() {
        return
                movieService.getMoviesUsersRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @POST
    public Uni<Response> create(
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO
    ) {
        if (Objects.isNull(movieDTO)) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                movieService.saveMovie(file, movieDTO)
                        .map(movie -> Response.status(CREATED).entity(movie).build());
    }

    @GET
    @Path("posters/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
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
    @Path("{id}/technical-team")
    public Uni<Response> saveTechnicalTeam(@RestPath Long id, TechnicalTeamDTO technicalTeam) {
        if (Objects.isNull(technicalTeam)) {
            throw new BadRequestException("La fiche technique ne peut pas être nulle.");
        }

        return
                movieService.saveTechnicalTeam(id, technicalTeam)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @PUT
    @Path("{id}/casting")
    public Uni<Response> saveCasting(@RestPath Long id, List<MovieActorDTO> movieActorsList) {
        if (Objects.isNull(movieActorsList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle.");
        }

        return
                movieService.saveCasting(id, movieActorsList)
                        .onItem().ifNotNull().transform(movieActorDTOList -> Response.ok(movieActorDTOList).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        /*.onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Erreur lors de la mise à jour du casting: " + e.getMessage())
                                .build()
                        )*/
                ;
    }

    @PUT
    @Path("{id}/producers")
    public Uni<Response> saveProducers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getProducers,
                                (movie, dto) -> {
                                    Producer producer = Producer.fromDTO(dto);
                                    producer.addMovie(movie);
                                    return producer;
                                },
                                producerService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/directors")
    public Uni<Response> saveDirectors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getDirectors,
                                (movie, dto) -> {
                                    Director director = Director.fromDTO(dto);
                                    director.addMovie(movie);
                                    return director;
                                },
                                directorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/screenwriters")
    public Uni<Response> saveScreenwriters(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getScreenwriters,
                                (movie, dto) -> {
                                    Screenwriter screenwriter = Screenwriter.fromDTO(dto);
                                    screenwriter.addMovie(movie);
                                    return screenwriter;
                                },
                                screenwriterService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/musicians")
    public Uni<Response> saveMusicians(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getMusicians,
                                (movie, dto) -> {
                                    Musician musician = Musician.fromDTO(dto);
                                    musician.addMovie(movie);
                                    return musician;
                                },
                                musicianService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/photographers")
    public Uni<Response> savePhotographers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getPhotographers,
                                (movie, dto) -> {
                                    Photographer photographer = Photographer.fromDTO(dto);
                                    photographer.addMovie(movie);
                                    return photographer;
                                },
                                photographerService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/costumiers")
    public Uni<Response> saveCostumiers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getCostumiers,
                                (movie, dto) -> {
                                    Costumier costumier = Costumier.fromDTO(dto);
                                    costumier.addMovie(movie);
                                    return costumier;
                                },
                                costumierService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/decorators")
    public Uni<Response> saveDecorators(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getDecorators,
                                (movie, dto) -> {
                                    Decorator decorator = Decorator.fromDTO(dto);
                                    decorator.addMovie(movie);
                                    return decorator;
                                },
                                decoratorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/editors")
    public Uni<Response> saveEditors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getEditors,
                                (movie, dto) -> {
                                    Editor editor = Editor.fromDTO(dto);
                                    editor.addMovie(movie);
                                    return editor;
                                },
                                editorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/casters")
    public Uni<Response> saveCasters(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste casteurs genres ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getCasters,
                                (movie, dto) -> {
                                    Caster caster = Caster.fromDTO(dto);
                                    caster.addMovie(movie);
                                    return caster;
                                },
                                casterService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/art-directors")
    public Uni<Response> saveArtDirectors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des directeurs artistiques ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getArtDirectors,
                                (movie, dto) -> {
                                    ArtDirector artDirector = ArtDirector.fromDTO(dto);
                                    artDirector.addMovie(movie);
                                    return artDirector;
                                },
                                artDirectorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/sound-editors")
    public Uni<Response> saveSoundEditors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des ingénieurs du son ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getSoundEditors,
                                (movie, dto) -> {
                                    SoundEditor soundEditor = SoundEditor.fromDTO(dto);
                                    soundEditor.addMovie(movie);
                                    return soundEditor;
                                },
                                soundEditorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> saveVisualEffectsSupervisors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getVisualEffectsSupervisors,
                                (movie, dto) -> {
                                    VisualEffectsSupervisor visualEffectsSupervisor = VisualEffectsSupervisor.fromDTO(dto);
                                    visualEffectsSupervisor.addMovie(movie);
                                    return visualEffectsSupervisor;
                                },
                                visualEffectsSupervisorService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/makeup-artists")
    public Uni<Response> saveMakeupArtists(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getMakeupArtists,
                                (movie, dto) -> {
                                    MakeupArtist makeupArtist = MakeupArtist.fromDTO(dto);
                                    makeupArtist.addMovie(movie);
                                    return makeupArtist;
                                },
                                makeupArtistService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/hair-dressers")
    public Uni<Response> saveHairDressers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getHairDressers,
                                (movie, dto) -> {
                                    HairDresser hairDresser = HairDresser.fromDTO(dto);
                                    hairDresser.addMovie(movie);
                                    return hairDresser;
                                },
                                hairDresserService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}/stuntmen")
    public Uni<Response> saveStuntmen(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle.");
        }

        return
                movieService.savePeople(
                                id,
                                personDTOSet,
                                Movie::getStuntmen,
                                (movie, dto) -> {
                                    Stuntman stuntman = Stuntman.fromDTO(dto);
                                    stuntman.addMovie(movie);
                                    return stuntman;
                                },
                                stuntmanService
                        )
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    /**
     * Met à jour les genres associés à un film donné.
     * <p>
     * Cette méthode permet d'ajouter ou de mettre à jour les genres d'un film
     * en fonction des identifiants fournis.
     *
     * @param id        L'identifiant du film dont les genres doivent être mis à jour.
     * @param genreDTOS Un ensemble de {@link GenreDTO} représentant les genres à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des genres mise à jour.
     * - `204 No Content` si aucun genre n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des genres est `null`.
     */
    @PUT
    @Path("{id}/genres")
    public Uni<Response> saveGenres(@RestPath Long id, Set<GenreDTO> genreDTOS) {
        if (Objects.isNull(genreDTOS)) {
            throw new BadRequestException("La liste des genres ne peut pas être nulle.");
        }

        return
                movieService.saveGenres(id, genreDTOS)
                        .onItem().ifNotNull().transform(genreDTOSet ->
                                genreDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOSet).build()
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
    @Path("{id}/countries")
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
     * @param id        L'identifiant du film dont les récompenses doivent être mis à jour.
     * @param awardDTOS Un ensemble de {@link AwardDTO} représentant les récompenses à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des récompenses mise à jour.
     * - `204 No Content` si aucune récompense n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des récompenses est `null`.
     */
    @PUT
    @Path("{id}/awards")
    public Uni<Response> saveAwards(@RestPath Long id, Set<AwardDTO> awardDTOS) {
        if (Objects.isNull(awardDTOS)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle.");
        }

        return
                movieService.saveAwards(id, awardDTOS)
                        .onItem().ifNotNull().transform(awardDTOSet ->
                                awardDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(awardDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    /**
     * Ajoute un ensemble de producteurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les producteurs doivent être ajoutés.
     * @param personDTOS L'ensemble des producteurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/producers")
    public Uni<Response> addProducers(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des producteurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getProducers, producerService, "La liste des producteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de réalisateurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les réalisateurs doivent être ajoutés.
     * @param personDTOS L'ensemble des réalisateurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/directors")
    public Uni<Response> addDirectors(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des réalisateurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getDirectors, directorService, "La liste des réalisateurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de scénaristes à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les scénaristes doivent être ajoutés.
     * @param personDTOS L'ensemble des scénaristes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/screenwriters")
    public Uni<Response> addScreenwriters(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des scénaristes ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getScreenwriters, screenwriterService, "La liste des scénaristes n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de musiciens à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les musiciens doivent être ajoutés.
     * @param personDTOS L'ensemble des musiciens à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/musicians")
    public Uni<Response> addMusicians(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des musiciens ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getMusicians, musicianService, "La liste des musiciens n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de photographes à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les photographes doivent être ajoutés.
     * @param personDTOS L'ensemble des photographes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/photographers")
    public Uni<Response> addPhotographers(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des photographes ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getPhotographers, photographerService, "La liste des photographes n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de costumiers à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les costumiers doivent être ajoutés.
     * @param personDTOS L'ensemble des costumiers à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des costumiers si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/costumiers")
    public Uni<Response> addCostumiers(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des costumiers ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getCostumiers, costumierService, "La liste des costumiers n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de décorateurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les décorateurs doivent être ajoutés.
     * @param personDTOS L'ensemble des décorateurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des décorateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/decorators")
    public Uni<Response> addDecorators(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des décorateurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getDecorators, decoratorService, "La liste des décorateurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de monteurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les monteurs doivent être ajoutés.
     * @param personDTOS L'ensemble des monteurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des monteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/editors")
    public Uni<Response> addEditors(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des monteurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getEditors, editorService, "La liste des monteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de casteurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les casteurs doivent être ajoutés.
     * @param personDTOS L'ensemble des casteurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des casteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/casters")
    public Uni<Response> addCasters(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des casteurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getCasters, casterService, "La liste des casteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de directeurs artistiques à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les directeurs artistiques doivent être ajoutés.
     * @param personDTOS L'ensemble des directeurs artistiques à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des directeurs artistiques si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/art-directors")
    public Uni<Response> addArtDirectors(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des directeurs artistiques ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getArtDirectors, artDirectorService, "La liste des directeurs artistiques n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble d'ingénieurs du son à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les ingénieurs du son doivent être ajoutés.
     * @param personDTOS L'ensemble des ingénieurs du son à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des ingénieurs du son si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/sound-editors")
    public Uni<Response> addSoundEditors(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des ingénieurs du son ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getSoundEditors, soundEditorService, "La liste des ingénieurs du son n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de spécialistes des effets spéciaux à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les spécialistes des effets spéciaux doivent être ajoutés.
     * @param personDTOS L'ensemble des spécialistes des effets spéciaux à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets spéciaux si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> addVisualEffectsSupervisors(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des spécialistes des effets spéciaux ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getVisualEffectsSupervisors, visualEffectsSupervisorService, "La liste des spécialistes des effets spéciaux n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de maquilleurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les maquilleurs doivent être ajoutés.
     * @param personDTOS L'ensemble des maquilleurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des maquilleurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/makeup-artists")
    public Uni<Response> addMakeupArtists(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des maquilleurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getMakeupArtists, makeupArtistService, "La liste des maquilleurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de coiffeurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les coiffeurs doivent être ajoutés.
     * @param personDTOS L'ensemble des coiffeurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des coiffeurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/hair-dressers")
    public Uni<Response> addHairDressers(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des coiffeurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getHairDressers, hairDresserService, "La liste des maquilleurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de cascadeurs à un film spécifique.
     *
     * @param id         L'identifiant du film auquel les cascadeurs doivent être ajoutés.
     * @param personDTOS L'ensemble des cascadeurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des cascadeurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/stuntmen")
    public Uni<Response> addStuntmen(@RestPath Long id, Set<PersonDTO> personDTOS) {
        if (Objects.isNull(personDTOS)) {
            throw new BadRequestException("La liste des cascadeurs ne peut pas être nulle.");
        }

        return
                movieService.addPeople(id, personDTOS, Movie::getStuntmen, stuntmanService, "La liste des cascadeurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @PATCH
    @Path("{id}/roles")
    public Uni<Response> addMovieActors(@RestPath Long id, Set<MovieActorDTO> movieActorsDTO) {
        if (Objects.isNull(movieActorsDTO)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle.");
        }

        return
                movieService.addMovieActors(id, movieActorsDTO)
                        .onItem().ifNotNull().transform(movieActorDTOList ->
                                movieActorDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOList).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de genres à un film spécifique.
     *
     * @param id        L'identifiant du film auquel les genres doivent être ajoutés.
     * @param genreDTOS L'ensemble des genres à ajouter, représentés sous forme de DTO.
     * @return Une réponse HTTP contenant le film mis à jour avec ses nouveaux genres :
     * - 200 OK si l'opération réussit et retourne l'entité mise à jour.
     * - 500 Server Error si l'ajout échoue.
     */
    @PATCH
    @Path("{id}/genres")
    public Uni<Response> addGenres(@RestPath Long id, Set<GenreDTO> genreDTOS) {
        if (Objects.isNull(genreDTOS)) {
            throw new BadRequestException("La liste des genres ne peut pas être nulle.");
        }

        return
                movieService.addGenres(id, genreDTOS)
                        .onItem().ifNotNull().transform(genreDTOSet ->
                                genreDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOSet).build()
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
    @Path("{id}/countries")
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
     * Ajoute des récompenses à un film donné.
     *
     * @param id        L'identifiant du film.
     * @param awardDTOS Un ensemble d'objets {@link AwardDTO} représentant les récompenses à ajouter au film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec les récompenses mises à jour si l'ajout est réussi.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("{id}/awards")
    public Uni<Response> addAwards(@RestPath Long id, Set<AwardDTO> awardDTOS) {
        if (Objects.isNull(awardDTOS)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle.");
        }

        return
                movieService.addAwards(id, awardDTOS)
                        .onItem().ifNotNull().transform(awardDTOSet ->
                                awardDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(awardDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un producteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param producerId L'identifiant du producteur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/producers/{producerId}")
    public Uni<Response> removeProducer(@RestPath Long movieId, @RestPath Long producerId) {
        return
                movieService.removePerson(movieId, producerId, Movie::getProducers, producerService, "La collection des producteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un réalisateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param directorId L'identifiant du réalisateur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/directors/{directorId}")
    public Uni<Response> removeDirector(@RestPath Long movieId, @RestPath Long directorId) {
        return
                movieService.removePerson(movieId, directorId, Movie::getDirectors, directorService, "La collection des réalisateurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un scénariste d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param screenwriterId L'identifiant du scénariste à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/screenwriters/{screenwriterId}")
    public Uni<Response> removeScreenwriter(@RestPath Long movieId, @RestPath Long screenwriterId) {
        return
                movieService.removePerson(movieId, screenwriterId, Movie::getScreenwriters, screenwriterService, "La collection des scénaristes n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un musicien d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param musicianId L'identifiant du musicien à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/musicians/{musicianId}")
    public Uni<Response> removeMusician(@RestPath Long movieId, @RestPath Long musicianId) {
        return
                movieService.removePerson(movieId, musicianId, Movie::getMusicians, musicianService, "La collection des musiciens n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un photographe d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param photographerId L'identifiant du photographe à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/photographers/{photographerId}")
    public Uni<Response> removePhotographer(@RestPath Long movieId, @RestPath Long photographerId) {
        return
                movieService.removePerson(movieId, photographerId, Movie::getPhotographers, photographerService, "La collection des photographes n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un costumier d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId     L'identifiant du film concerné.
     * @param costumierId L'identifiant du costumier à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des costumiers si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/costumiers/{costumierId}")
    public Uni<Response> removeCostumier(@RestPath Long movieId, @RestPath Long costumierId) {
        return
                movieService.removePerson(movieId, costumierId, Movie::getCostumiers, costumierService, "La collection des costumiers n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un décorateur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId     L'identifiant du film concerné.
     * @param decoratorId L'identifiant du décorateur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des décorateurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/decorators/{decoratorId}")
    public Uni<Response> removeDecorator(@RestPath Long movieId, @RestPath Long decoratorId) {
        return
                movieService.removePerson(movieId, decoratorId, Movie::getDecorators, decoratorService, "La collection des décorateurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un monteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param editorId L'identifiant du monteur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des monteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/editors/{editorId}")
    public Uni<Response> removeEditor(@RestPath Long movieId, @RestPath Long editorId) {
        return
                movieService.removePerson(movieId, editorId, Movie::getEditors, editorService, "La collection des monteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un casteur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId  L'identifiant du film concerné.
     * @param casterId L'identifiant du casteur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des casteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/casters/{casterId}")
    public Uni<Response> removeCaster(@RestPath Long movieId, @RestPath Long casterId) {
        return
                movieService.removePerson(movieId, casterId, Movie::getCasters, casterService, "La collection des casteurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un directeur artistique d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId       L'identifiant du film concerné.
     * @param artDirectorId L'identifiant du directeur artistique à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des directeurs artistiques si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/art-directors/{artDirectorId}")
    public Uni<Response> removeArtDirectors(@RestPath Long movieId, @RestPath Long artDirectorId) {
        return
                movieService.removePerson(movieId, artDirectorId, Movie::getArtDirectors, artDirectorService, "La collection des directeurs artistiques n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un ingénieur du son d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId         L'identifiant du film concerné.
     * @param soundDirectorId L'identifiant de l'ingénieur du son à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des ingénieurs du son si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/sound-editors/{soundDirectorId}")
    public Uni<Response> removeSoundEditors(@RestPath Long movieId, @RestPath Long soundDirectorId) {
        return
                movieService.removePerson(movieId, soundDirectorId, Movie::getSoundEditors, soundEditorService, "La collection des ingénieurs du son n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un spécialiste des effets spéciaux d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId                   L'identifiant du film concerné.
     * @param visualEffectsSupervisorId L'identifiant du spécialiste des effets spéciaux à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des spécialistes des effets spéciaux si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/visual-effects-supervisors/{visualEffectsSupervisorId}")
    public Uni<Response> removeVisualEffectsSupervisor(@RestPath Long movieId, @RestPath Long visualEffectsSupervisorId) {
        return
                movieService.removePerson(movieId, visualEffectsSupervisorId, Movie::getVisualEffectsSupervisors, visualEffectsSupervisorService, "La collection des spécialistes des effets spéciaux n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un maquilleur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId        L'identifiant du film concerné.
     * @param makeupArtistId L'identifiant du maquilleur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des maquilleurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/makeup-artists/{makeupArtistId}")
    public Uni<Response> removeMakeupArtists(@RestPath Long movieId, @RestPath Long makeupArtistId) {
        return
                movieService.removePerson(movieId, makeupArtistId, Movie::getMakeupArtists, makeupArtistService, "La collection des maquilleurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un coiffeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId       L'identifiant du film concerné.
     * @param hairDresserId L'identifiant du coiffeur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des coiffeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/hair-dressers/{hairDresserId}")
    public Uni<Response> removeHairDressers(@RestPath Long movieId, @RestPath Long hairDresserId) {
        return
                movieService.removePerson(movieId, hairDresserId, Movie::getHairDressers, hairDresserService, "La collection des coiffeurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Retire un cascadeur d'un film spécifique et retourne une réponse HTTP appropriée.
     *
     * @param movieId    L'identifiant du film concerné.
     * @param stuntmanId L'identifiant du cascadeur à dissocier du film.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des cascadeurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("{movieId}/stuntmen/{stuntmanId}")
    public Uni<Response> removeStuntman(@RestPath Long movieId, @RestPath Long stuntmanId) {
        return
                movieService.removePerson(movieId, stuntmanId, Movie::getStuntmen, stuntmanService, "La collection des cascadeurs n'est pas initialisée")
                        .onItem().ifNotNull().transform(personDTOSet ->
                                personDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
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
    @Path("{movieId}/roles/{movieActorId}")
    public Uni<Response> removeMovieActor(@RestPath Long movieId, @RestPath Long movieActorId) {
        return
                movieService.removeMovieActor(movieId, movieActorId)
                        .onItem().ifNotNull().transform(movieActorDTOList ->
                                movieActorDTOList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOList).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Supprime un genre spécifique d'un film donné.
     *
     * @param movieId L'identifiant du film dont le genre doit être supprimé.
     * @param genreId L'identifiant du genre à supprimer.
     * @return Une réponse HTTP contenant le film mis à jour après la suppression du genre :
     * - 200 OK si la suppression est réussie et retourne l'entité mise à jour.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("{movieId}/genres/{genreId}")
    public Uni<Response> removeGenre(@RestPath Long movieId, @RestPath Long genreId) {
        return
                movieService.removeGenre(movieId, genreId)
                        .onItem().ifNotNull().transform(genreDTOSet ->
                                genreDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(genreDTOSet).build()
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
    @Path("{movieId}/countries/{countryId}")
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
     * Enlève une récompense d'un film donné.
     *
     * @param movieId L'identifiant du film concerné.
     * @param awardId L'identifiant de la récompense à enlever du film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si la suppression est réussie.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("{movieId}/awards/{awardId}")
    public Uni<Response> removeAward(@RestPath Long movieId, @RestPath Long awardId) {
        return
                movieService.removeAward(movieId, awardId)
                        .onItem().ifNotNull().transform(awardDTOSet ->
                                awardDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(awardDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(
            Long id,
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) MovieDTO movieDTO
    ) {
        if (Objects.isNull(movieDTO) || Objects.isNull(movieDTO.getTitle())) {
            throw new WebApplicationException("Movie title was not set on request.", 422);
        }

        return
                movieService.updateMovie(id, file, movieDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@RestPath Long id) {
        return movieService.deleteMovie(id)
                .map(deleted -> Response.ok().status(Boolean.TRUE.equals(deleted) ? NO_CONTENT : NOT_FOUND).build());
    }

    /**
     * Supprime tous les producteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les producteurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les producteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les producteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des producteurs.
     */
    @DELETE
    @Path("{id}/producers")
    public Uni<Response> deleteProducers(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getProducers, "L'ensemble des producteurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les réalisateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les réalisateurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les réalisateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les réalisateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des réalisateurs.
     */
    @DELETE
    @Path("{id}/directors")
    public Uni<Response> deleteDirectors(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getDirectors, "L'ensemble des réalisateurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les scénaristes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les scénaristes associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les scénaristes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les scénaristes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des scénaristes.
     */
    @DELETE
    @Path("{id}/screenwriters")
    public Uni<Response> deleteScreenwriters(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getScreenwriters, "L'ensemble des scénaristes n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les musiciens associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les musiciens associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les musiciens doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les musiciens ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des musiciens.
     */
    @DELETE
    @Path("{id}/musicians")
    public Uni<Response> deleteMusicians(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getMusicians, "L'ensemble des musiciens n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les décorateurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les décorateurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les décorateurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les décorateurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des décorateurs.
     */
    @DELETE
    @Path("{id}/decorators")
    public Uni<Response> deleteDecorators(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getDecorators, "L'ensemble des décorateurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les costumiers associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les costumiers associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les costumiers doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les costumiers ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des costumiers.
     */
    @DELETE
    @Path("{id}/costumiers")
    public Uni<Response> deleteCostumiers(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getCostumiers, "L'ensemble des costumiers n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les photographes associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les photographes associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les photographes doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les photographes ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des photographes.
     */
    @DELETE
    @Path("{id}/photographers")
    public Uni<Response> deletePhotographers(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getPhotographers, "L'ensemble des photographes n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les monteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les monteurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les monteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les monteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des monteurs.
     */
    @DELETE
    @Path("{id}/editors")
    public Uni<Response> deleteEditors(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getEditors, "L'ensemble des monteurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les casteurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les casteurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les casteurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les casteurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des casteurs.
     */
    @DELETE
    @Path("{id}/casters")
    public Uni<Response> deleteCasters(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getCasters, "L'ensemble des casteurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les directeurs artistiques associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les directeurs artistiques associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les directeurs artistiques doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les directeurs artistiques ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des directeurs artistiques.
     */
    @DELETE
    @Path("{id}/art-directors")
    public Uni<Response> deleteArtDirectors(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getArtDirectors, "L'ensemble des directeurs artistiques n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les ingénieurs du son associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les ingénieurs du son associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les ingénieurs du son doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les ingénieurs du son ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des ingénieurs du son.
     */
    @DELETE
    @Path("{id}/sound-editors")
    public Uni<Response> deleteSoundEditors(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getSoundEditors, "L'ensemble des ingénieurs du son n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les spécialistes des effets spéciaux associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les spécialistes des effets spéciaux associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les spécialistes des effets spéciaux doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les spécialistes des effets spéciaux ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des spécialistes des effets spéciaux.
     */
    @DELETE
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> deleteVisualEffectsSupervisors(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getVisualEffectsSupervisors, "L'ensemble des spécialistes des effets spéciaux n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les maquilleurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les maquilleurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les maquilleurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les maquilleurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des maquilleurs.
     */
    @DELETE
    @Path("{id}/makeup-artists")
    public Uni<Response> deleteMakeupArtists(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getMakeupArtists, "L'ensemble des maquilleurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les coiffeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les coiffeurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les coiffeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les coiffeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des coiffeurs.
     */
    @DELETE
    @Path("{id}/hair-dressers")
    public Uni<Response> deleteHairDressers(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getHairDressers, "L'ensemble des coiffeurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les cascadeurs associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les cascadeurs associés à un film en appelant la méthode
     * {@link MovieService#clearPersons(Long, Function, String)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les cascadeurs doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les cascadeurs ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des cascadeurs.
     */
    @DELETE
    @Path("{id}/stuntmen")
    public Uni<Response> deleteStuntmen(@RestPath Long id) {
        return
                movieService.clearPersons(id, Movie::getStuntmen, "L'ensemble des cascadeurs n'est pas initialisé")
                        .map(deleted -> Response.ok(deleted).build())
                ;
    }

    /**
     * Supprime tous les genres associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les genres associés à un film en appelant la méthode
     * {@link MovieService#clearGenres(Long)} (Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les genres doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les genres ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des genres.
     */
    @DELETE
    @Path("{id}/genres")
    public Uni<Response> deleteGenres(@RestPath Long id) {
        return movieService.clearGenres(id).map(deleted -> Response.ok(deleted).build());
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
    @Path("{id}/countries")
    public Uni<Response> deleteCountries(@RestPath Long id) {
        return movieService.clearCountries(id).map(deleted -> Response.ok(deleted).build());
    }

    /**
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de supprimer toutes les récompenses associées à un film en appelant la méthode
     * {@link MovieService#clearAwards(Long)} (Long)} (Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les récompenses doivent être supprimées.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les récompenses ont été supprimées avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des récompenses.
     */
    @DELETE
    @Path("{id}/awards")
    public Uni<Response> deleteAwards(@RestPath Long id) {
        return movieService.clearAwards(id).map(deleted -> Response.ok(deleted).build());
    }

}