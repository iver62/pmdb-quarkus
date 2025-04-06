package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @GET
    @Path("count")
    public Uni<Response> count(@BeanParam MovieQueryParamsDTO queryParams) {
        return
                movieService.count(CriteriasDTO.build(queryParams))
                        .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build());
    }

    @GET
    @Path("{id}")
    public Uni<Response> getMovie(@RestPath Long id) {
        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build())
                ;
    }

    @GET
    public Uni<Response> getMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        // Vérification de la cohérence des dates
        if (Objects.nonNull(queryParams.getFromReleaseDate()) && Objects.nonNull(queryParams.getToReleaseDate()) && queryParams.getFromReleaseDate().isAfter(queryParams.getToReleaseDate())
                || Objects.nonNull(queryParams.getFromCreationDate()) && Objects.nonNull(queryParams.getToCreationDate()) && queryParams.getFromCreationDate().isAfter(queryParams.getToCreationDate())
                || Objects.nonNull(queryParams.getFromLastUpdate()) && Objects.nonNull(queryParams.getToLastUpdate()) && queryParams.getFromLastUpdate().isAfter(queryParams.getToLastUpdate())
        ) {
            return
                    Uni.createFrom().item(
                            Response.status(Response.Status.BAD_REQUEST)
                                    .entity("La date de début ne peut pas être après la date de fin.")
                                    .build()
                    );
        }

        // Vérifier si la direction est valide
        Uni<Response> sortValidation = validateSortField(queryParams.getSort(), Movie.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(queryParams.getDirection());

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                movieService.getMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), queryParams.getSort(), sortDirection, criteriasDTO)
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
    public Uni<Response> getAllMovies(MovieQueryParamsDTO queryParams) {
        // Vérifier si la direction est valide
        Uni<Response> sortValidation = validateSortField(queryParams.getSort(), Movie.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(queryParams.getDirection());

        return
                Movie.getAllMovies(queryParams.getSort(), sortDirection, queryParams.getTerm())
                        .flatMap(movieList ->
                                Movie.count(queryParams.getTerm()).map(total ->
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
        Uni<Response> sortValidation = validateSortField(queryParams.getSort(), Country.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(queryParams.getDirection());

        return
                movieService.getCountriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), queryParams.getSort(), sortDirection, queryParams.getTerm())
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
                Movie.getByTitle(title)
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

    @GET
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> getVisualEffectsSupervisors(@RestPath Long id) {
        return
                movieService.getPeopleByMovie(id, Movie::getSoundEditors, soundEditorService, "L'ensemble des spécialistes des effets spéciaux du son n'est pas initialisé pour ce film")
                        .map(visualEffectsSupervisors ->
                                visualEffectsSupervisors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(visualEffectsSupervisors).build()
                        )
                ;
    }

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

    @GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(@RestPath Long id) {
        return
                movieService.getAwardsByMovie(id)
                        .map(awards -> Response.ok(awards).build())
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

//    @POST
//    @Path("full")
//    public Uni<Response> createFullMovie(Movie movie) {
//        if (Objects.isNull(movie) || Objects.nonNull(movie.id)) {
//            throw new WebApplicationException("Id was invalidly set on request.", 422);
//        }
//
//        return
//                movieService.createMovie(movie)
//                        .replaceWith(Response.ok(movie).status(CREATED)::build);
//    }

    @PUT
    @Path("{id}/technical-team")
    public Uni<Response> saveTechnicalTeam(@RestPath Long id, TechnicalTeamDTO technicalTeam) {
        if (Objects.isNull(technicalTeam)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La fiche technique ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des acteurs ne peut pas être nulle.")
                            .build()
            );
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
    @Path("{id}/awards")
    public Uni<Response> saveAwards(@RestPath Long id, Set<AwardDTO> awardDTOSet) {
        if (Objects.isNull(awardDTOSet)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des récompenses ne peut pas être nulle.")
                            .build()
            );
        }

        return
                movieService.saveAwards(id, awardDTOSet)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @PUT
    @Path("{id}/producers")
    public Uni<Response> saveProducers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        if (Objects.isNull(personDTOSet)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des producteurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des réalisateurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des scénaristes ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des musiciens ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des photographes ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des costumiers ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des décorateurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des monteurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des casteurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des directeurs artistiques ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des ingénieurs du son ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des spécialistes des effets spéciaux ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des maquilleurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des coiffeurs ne peut pas être nulle.")
                            .build()
            );
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
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des cascadeurs ne peut pas être nulle.")
                            .build()
            );
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
     * Ajoute un ensemble de producteurs à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les producteurs doivent être ajoutés.
     * @param personDTOSet L'ensemble des producteurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des producteurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/producers")
    public Uni<Response> addProducers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        return
                movieService.addProducers(id, personDTOSet)
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de réalisateurs à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les réalisateurs doivent être ajoutés.
     * @param personDTOSet L'ensemble des réalisateurs à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des réalisateurs si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/directors")
    public Uni<Response> addDirectors(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        return
                movieService.addDirectors(id, personDTOSet)
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de scénaristes à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les scénaristes doivent être ajoutés.
     * @param personDTOSet L'ensemble des scénaristes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des scénaristes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/screenwriters")
    public Uni<Response> addScreenwriters(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        return
                movieService.addScreenwriters(id, personDTOSet)
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de musiciens à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les musiciens doivent être ajoutés.
     * @param personDTOSet L'ensemble des musiciens à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des musiciens si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/musicians")
    public Uni<Response> addMusicians(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        return
                movieService.addMusicians(id, personDTOSet)
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de photographes à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les photographes doivent être ajoutés.
     * @param personDTOSet L'ensemble des photographes à ajouter sous forme de {@link PersonDTO}.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des photographes si l'ajout est réussi.
     * - 500 Server Error si l'ajout a échoué.
     */
    @PATCH
    @Path("{id}/photographers")
    public Uni<Response> addPhotographers(@RestPath Long id, Set<PersonDTO> personDTOSet) {
        return
                movieService.addPhotographers(id, personDTOSet)
                        .onItem().ifNotNull().transform(personDTOS -> Response.ok(personDTOS).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute un ensemble de genres à un film spécifique.
     *
     * @param id          L'identifiant du film auquel les genres doivent être ajoutés.
     * @param genreDTOSet L'ensemble des genres à ajouter, représentés sous forme de DTO.
     * @return Une réponse HTTP contenant le film mis à jour avec ses nouveaux genres :
     * - 200 OK si l'opération réussit et retourne l'entité mise à jour.
     * - 500 Server Error si l'ajout échoue.
     */
    @PUT
    @Path("{id}/genres")
    public Uni<Response> addGenres(@RestPath Long id, Set<GenreDTO> genreDTOSet) {
        return
                movieService.addGenres(id, genreDTOSet)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /**
     * Ajoute une liste de pays associés à un film.
     *
     * @param id            L'identifiant du film auquel les pays doivent être ajoutés.
     * @param countryDTOSet Un ensemble d'objets {@link CountryDTO} représentant les pays à associer au film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si l'ajout est réussi.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PUT
    @Path("{id}/countries")
    public Uni<Response> addCountries(@RestPath Long id, Set<CountryDTO> countryDTOSet) {
        return
                movieService.addCountries(id, countryDTOSet)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

//    @PUT
//    @Path("{id}/actors")
//    public Uni<Response> addActors(Long id, List<MovieActor> movieActors) {
//        if (Objects.isNull(movieActors) || movieActors.isEmpty()) {
//            throw new WebApplicationException("Movie title was not set on request.", 422);
//        }
//
//        return
//                Uni.join().all(
//                                movieActors
//                                        .stream()
//                                        .filter(a -> Objects.nonNull(a.getActor().id))
//                                        .map(a -> (Person) Person.findById(a.getActor().id))
//                                        .toList()
//                        )
//                        .usingConcurrencyOf(1)
//                        .andFailFast()
//                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
//                        .map(personList -> personList.stream().collect(Collectors.toCollection(() -> newMusicians)))
//                        .map(personList -> musicians.stream().filter(m -> Objects.isNull(m.id)).collect(Collectors.toCollection(() -> newMusicians)))
//        Panache
//                .withTransaction(() -> Movie.<Movie>findById(id)
//                        .onItem().ifNotNull()
//                        .invoke(entity -> entity.setMovieActors(movieActors))
//                        .chain(entity -> entity.persist())
//
//                )
//                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
//                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
//
//    }

    /*@PUT
    @Path("{id}/awards")
    public Uni<Response> addAwards(Long id, Set<Award> awardSet) {
        return
                movieService.addAwards(id, awardSet)
                        .map(Movie::getAwards)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }*/

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
                movieService.removeProducer(movieId, producerId)
                        .onItem().ifNotNull().transform(personDTOSet -> Response.ok(personDTOSet).build())
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
                movieService.removeDirector(movieId, directorId)
                        .onItem().ifNotNull().transform(personDTOSet -> Response.ok(personDTOSet).build())
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
                movieService.removeScreenwriter(movieId, screenwriterId)
                        .onItem().ifNotNull().transform(personDTOSet -> Response.ok(personDTOSet).build())
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
                movieService.removeMusician(movieId, musicianId)
                        .onItem().ifNotNull().transform(personDTOSet -> Response.ok(personDTOSet).build())
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
                movieService.removePhotographer(movieId, photographerId)
                        .onItem().ifNotNull().transform(personDTOSet -> Response.ok(personDTOSet).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    /*@PUT
    @Path("{movieId}/costumiers/{costumierId}")
    public Uni<Response> removeCostumier(Long movieId, Long costumierId) {
        return
                costumierService.removeMovie(costumierId, movieId)
                        .chain(() -> movieService.removeCostumier(movieId, costumierId))
                        .map(Movie::getCostumiers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/decorators/{decoratorId}")
    public Uni<Response> removeDecorator(Long movieId, Long decoratorId) {
        return
                decoratorService.removeMovie(decoratorId, movieId)
                        .chain(() -> movieService.removeDecorator(movieId, decoratorId))
                        .map(Movie::getDecorators)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/editors/{editorId}")
    public Uni<Response> removeEditor(Long movieId, Long editorId) {
        return
                editorService.removeMovie(editorId, movieId)
                        .chain(() -> movieService.removeEditor(movieId, editorId))
                        .map(Movie::getEditors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

   /* @PUT
    @Path("{movieId}/casters/{casterId}")
    public Uni<Response> removeCaster(Long movieId, Long casterId) {
        return
                casterService.removeMovie(casterId, movieId)
                        .chain(() -> movieService.removeEditor(movieId, casterId))
                        .map(Movie::getCasters)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/art-directors/{artDirectorId}")
    public Uni<Response> removeArtDirectors(Long movieId, Long artDirectorId) {
        return
                artDirectorService.removeMovie(artDirectorId, movieId)
                        .chain(() -> movieService.removeArtDirector(movieId, artDirectorId))
                        .map(Movie::getArtDirectors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/sound-editors/{soundDirectorId}")
    public Uni<Response> removeSoundEditors(Long movieId, Long soundDirectorId) {
        return
                soundEditorService.removeMovie(soundDirectorId, movieId)
                        .chain(() -> movieService.removeArtDirector(movieId, soundDirectorId))
                        .map(Movie::getSoundEditors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/visual-effects-supervisors/{visualEffectsSupervisorId}")
    public Uni<Response> removeVisualEffectsSupervisor(Long movieId, Long visualEffectsSupervisorId) {
        return
                visualEffectsSupervisorService.removeMovie(visualEffectsSupervisorId, movieId)
                        .chain(() -> movieService.removeVisualEffectsSupervisor(movieId, visualEffectsSupervisorId))
                        .map(Movie::getVisualEffectsSupervisors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

   /* @PUT
    @Path("{movieId}/makeup-artists/{makeupArtistId}")
    public Uni<Response> removeMakeupArtists(Long movieId, Long makeupArtistId) {
        return
                makeupArtistService.removeMovie(makeupArtistId, movieId)
                        .chain(() -> movieService.removeMakeupArtist(movieId, makeupArtistId))
                        .map(Movie::getMakeupArtists)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{movieId}/hair-dressers/{hairDresserId}")
    public Uni<Response> removeHairDressers(Long movieId, Long hairDresserId) {
        return
                hairDresserService.removeMovie(hairDresserId, movieId)
                        .chain(() -> movieService.removeHairDresser(movieId, hairDresserId))
                        .map(Movie::getHairDressers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

   /* @PUT
    @Path("{movieId}/stuntmen/{stuntmanId}")
    public Uni<Response> removeStuntman(Long movieId, Long stuntmanId) {
        return
                stuntmanService.removeMovie(stuntmanId, movieId)
                        .chain(() -> movieService.removeStuntman(movieId, stuntmanId))
                        .map(Movie::getStuntmen)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /**
     * Supprime un genre spécifique d'un film donné.
     *
     * @param movieId L'identifiant du film dont le genre doit être supprimé.
     * @param genreId L'identifiant du genre à supprimer.
     * @return Une réponse HTTP contenant le film mis à jour après la suppression du genre :
     * - 200 OK si la suppression est réussie et retourne l'entité mise à jour.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PUT
    @Path("{movieId}/genres/{genreId}")
    public Uni<Response> removeGenre(@RestPath Long movieId, @RestPath Long genreId) {
        return
                movieService.removeGenre(movieId, genreId)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
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
    @PUT
    @Path("{movieId}/countries/{countryId}")
    public Uni<Response> removeCountry(@RestPath Long movieId, @RestPath Long countryId) {
        return
                movieService.removeCountry(movieId, countryId)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @PUT
    @Path("{movieId}/awards/{awardId}")
    public Uni<Response> removeAward(Long movieId, Long awardId) {
        return
                movieService.removeAward(movieId, awardId)
                        .map(Movie::getAwards)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/actor/{actorId}")
    public Uni<Response> removeRole(Long movieId, Long actorId) {
        return
                Panache
                        .withTransaction(() -> Movie.<Movie>findById(movieId)
                                .onItem().ifNotNull()
                                .invoke(entity -> entity.removeRole(actorId))
                                .chain(entity -> entity.persist())
                        )
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
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
    public Uni<Response> delete(Long id) {
        return movieService.deleteMovie(id)
                .map(deleted -> Response.ok().status(Boolean.TRUE.equals(deleted) ? NO_CONTENT : NOT_FOUND).build());
    }

    private Sort.Direction validateSortDirection(String direction) {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

    private Uni<Response> validateSortField(String sort, List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sort)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(MessageFormat.format("Le champ de tri \"{0}\" est invalide. Valeurs autorisées : {1}", sort, Movie.ALLOWED_SORT_FIELDS))
                            .build()
            );
        }
        return null;
    }

}
