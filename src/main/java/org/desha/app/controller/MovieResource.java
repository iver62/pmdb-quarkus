package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.TechnicalTeamDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.CountryService;
import org.desha.app.service.GenreService;
import org.desha.app.service.MovieService;
import org.desha.app.service.PersonService;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("movies")
@ApplicationScoped
@Slf4j
public class MovieResource {

    private final CountryService countryService;
    private final GenreService genreService;
    private final MovieService movieService;

    private final PersonService<ArtDirector> artDirectorService;
    private final PersonService<Caster> casterService;
    private final PersonService<Costumier> costumierService;
    private final PersonService<Decorator> decoratorService;
    private final PersonService<Director> directorService;
    private final PersonService<Editor> editorService;
    private final PersonService<HairDresser> hairDresserService;
    private final PersonService<MakeupArtist> makeupArtistService;
    private final PersonService<Musician> musicianService;
    private final PersonService<Photographer> photographerService;
    private final PersonService<Producer> producerService;
    private final PersonService<Screenwriter> screenwriterService;
    private final PersonService<SoundEditor> soundEditorService;
    private final PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService;
    private final PersonService<Stuntman> stuntmanService;

    @Inject
    public MovieResource(
            CountryService countryService,
            GenreService genreService,
            MovieService movieService,
            @PersonType(Role.ART_DIRECTOR) PersonService<ArtDirector> artDirectorService,
            @PersonType(Role.CASTER) PersonService<Caster> casterService,
            @PersonType(Role.COSTUMIER) PersonService<Costumier> costumierService,
            @PersonType(Role.DECORATOR) PersonService<Decorator> decoratorService,
            @PersonType(Role.DIRECTOR) PersonService<Director> directorService,
            @PersonType(Role.EDITOR) PersonService<Editor> editorService,
            @PersonType(Role.HAIR_DRESSER) PersonService<HairDresser> hairDresserService,
            @PersonType(Role.MAKEUP_ARTIST) PersonService<MakeupArtist> makeupArtistService,
            @PersonType(Role.MUSICIAN) PersonService<Musician> musicianService,
            @PersonType(Role.PHOTOGRAPHER) PersonService<Photographer> photographerService,
            @PersonType(Role.PRODUCER) PersonService<Producer> producerService,
            @PersonType(Role.SCREENWRITER) PersonService<Screenwriter> screenwriterService,
            @PersonType(Role.SOUND_EDITOR) PersonService<SoundEditor> soundEditorService,
            @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService,
            @PersonType(Role.STUNT_MAN) PersonService<Stuntman> stuntmanService
    ) {
        this.countryService = countryService;
        this.genreService = genreService;
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
        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
        this.stuntmanService = stuntmanService;
    }

    @GET
    @Path("count")
    public Uni<Response> count(@QueryParam("title") @DefaultValue("") String title) {
        return Movie.count(title)
                .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build());
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build())
                ;
    }

    @GET
    public Uni<Response> getPaginatedMovies(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("title") @DefaultValue("") String title
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Movie.getPaginatedMovies(page, size, sort, sortDirection, title)
                        .flatMap(movieList ->
                                Movie.count(title).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(movieList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    @GET
    @Path("all")
    public Uni<Response> getAll(
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("title") @DefaultValue("") String title
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Movie.getMovies(sort, sortDirection, title)
                        .flatMap(movieList ->
                                Movie.count(title).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(movieList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
    @Path("search/{pattern}")
    public Uni<Response> searchByTitle(@RestPath String pattern) {
        return
                Movie.searchByTitle(pattern)
                        .onItem().ifNotNull().transform(panacheEntityBases -> Response.ok(panacheEntityBases).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/actors")
    public Uni<Response> getActors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .flatMap(movieService::getActorsByMovie)
                        .map(movieActors -> Response.ok(movieActors).build())
                        .onFailure().recoverWithItem(ex -> Response.serverError().entity(ex.getMessage()).build())
                ;
    }

    @GET
    @Path("{id}/producers")
    public Uni<Set<Producer>> getProducers(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getProducersByMovie)
                ;
    }

    @GET
    @Path("{id}/directors")
    public Uni<Set<Director>> getDirectors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getDirectorsByMovie)
                ;
    }

    @GET
    @Path("{id}/screenwriters")
    public Uni<Set<Screenwriter>> getScreenwriters(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getScreenwritersByMovie)
                ;
    }

    @GET
    @Path("{id}/musicians")
    public Uni<Set<Musician>> getMusicians(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getMusiciansByMovie)
                ;
    }

    @GET
    @Path("{id}/photographers")
    public Uni<Set<Photographer>> getPhotographers(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getPhotographersByMovie)
                ;
    }

    @GET
    @Path("{id}/costumiers")
    public Uni<Set<Costumier>> getCostumiers(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getCostumiersByMovie)
                ;
    }

    @GET
    @Path("{id}/decorators")
    public Uni<Set<Decorator>> getDecorators(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getDecoratorsByMovie)
                ;
    }

    @GET
    @Path("{id}/editors")
    public Uni<Set<Editor>> getEditors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getEditorsByMovie)
                ;
    }

    @GET
    @Path("{id}/casters")
    public Uni<Set<Caster>> getCasters(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getCastersByMovie)
                ;
    }

    @GET
    @Path("{id}/art-directors")
    public Uni<Set<ArtDirector>> getArtDirectors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getArtDirectorsByMovie)
                ;
    }

    @GET
    @Path("{id}/sound-editors")
    public Uni<Set<SoundEditor>> getSoundEditors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getSoundEditorsByMovie)
                ;
    }

    @GET
    @Path("{id}/visual-effects-supervisors")
    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisors(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getVisualEffectsSupervisorsByMovie)
                ;
    }

    @GET
    @Path("{id}/makeup-artists")
    public Uni<Set<MakeupArtist>> getMakeupArtists(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getMakeupArtists)
                ;
    }

    @GET
    @Path("{id}/hair-dressers")
    public Uni<Set<HairDresser>> getHairDressers(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getHairDressers)
                ;
    }

    @GET
    @Path("{id}/stuntmen")
    public Uni<Set<Stuntman>> getStuntmen(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getStuntmen)
                ;
    }

    @GET
    @Path("{id}/genres")
    public Uni<Set<Genre>> getGenres(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getGenresByMovie)
                ;
    }

    @GET
    @Path("{id}/countries")
    public Uni<Set<Country>> getCountries(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getCountriesByMovie)
                ;
    }

    @GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(@RestPath Long id) {
        return
                Movie.getById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException("Ce film n'existe pas"))
                        .chain(movieService::getAwardsByMovie)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.status(404, "Ce film n'existe pas").build())
                ;
    }

    @POST
    public Uni<Response> create(
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) MovieDTO movieDTO
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
        return
                movieService.saveTechnicalTeam(id, technicalTeam)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);

        /*return
                Uni.join().all(
                                technicalSummary.getProducers()
                                        .stream()
                                        .map(p -> Person.findById(p.id))
                                        .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .chain(persons -> movieService.addProducers(id, persons))
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getDirectors()) && !technicalSummary.getDirectors().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getDirectors()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addDirectors(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getScreenwriters()) && !technicalSummary.getScreenwriters().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getScreenwriters()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addScreenwriters(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getMusicians()) && !technicalSummary.getMusicians().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getMusicians()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addMusicians(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getPhotographers()) && !technicalSummary.getPhotographers().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getPhotographers()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addPhotographers(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getCostumiers()) && !technicalSummary.getCostumiers().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getCostumiers()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addCostumiers(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getDecorators()) && !technicalSummary.getDecorators().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getDecorators()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addDecorators(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getEditors()) && !technicalSummary.getEditors().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getEditors()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.addEditors(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .chain(() -> {
                                    if (!Objects.isNull(technicalSummary.getCasting()) && !technicalSummary.getCasting().isEmpty()) {
                                        return Uni.join().all(
                                                        technicalSummary.getCasting()
                                                                .stream()
                                                                .filter(p -> Objects.nonNull(p.id))
                                                                .map(p -> Person.findById(p.id))
                                                                .toList()
                                                )
                                                .usingConcurrencyOf(1)
                                                .andFailFast()
                                                .map(entities -> entities.stream().map(e -> (Person) e).toList())
                                                .map(HashSet::new)
                                                .chain(persons -> movieService.saveCasting(id, persons));
                                    } else {
                                        return Uni.createFrom().nullItem();
                                    }
                                }
                        )
                        .map(
                                movie ->
                                        TechnicalSummaryDTO.build(
                                                movie.getProducers(),
                                                movie.getDirectors(),
                                                movie.getScreenwriters(),
                                                movie.getMusicians(),
                                                movie.getPhotographers(),
                                                movie.getCostumiers(),
                                                movie.getDecorators(),
                                                movie.getEditors(),
                                                movie.getCasting()
                                        )
                        )
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;*/
    }

    @PUT
    @Path("{id}/casting")
    public Uni<Response> saveCasting(@RestPath Long id, List<MovieActorDTO> movieActorsList) {
        if (Objects.isNull(movieActorsList) || movieActorsList.isEmpty()) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("La liste des acteurs ne peut pas être vide.")
                            .build()
            );
        }

        return movieService.saveCasting(id, movieActorsList)
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Erreur lors de la miseà jour du casting: " + e.getMessage())
                        .build()
                )
                ;
    }

    /*@PUT
    @Path("{id}/producers")
    public Uni<Response> addProducers(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addProducers(id, persons))
                        .map(Movie::getProducers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/directors")
    public Uni<Response> addDirectors(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(d -> Objects.isNull(d.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addDirectors(id, persons))
                        .map(Movie::getDirectors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/screenwriters")
    public Uni<Response> addScreenwriters(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(s -> Objects.isNull(s.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addScreenwriters(id, persons))
                        .map(Movie::getScreenwriters)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }*/

    /*@PUT
    @Path("{id}/musicians")
    public Uni<Response> addMusicians(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addMusicians(id, persons))
                        .map(Movie::getMusicians)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/photographers")
    public Uni<Response> addPhotographers(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addPhotographers(id, persons))
                        .map(Movie::getPhotographers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/costumiers")
    public Uni<Response> addCostumiers(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(c -> Person.findById(c.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addCostumiers(id, persons))
                        .map(Movie::getCostumiers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/decorators")
    public Uni<Response> addDecorators(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addDecorators(id, persons))
                        .map(Movie::getDecorators)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    /*@PUT
    @Path("{id}/editors")
    public Uni<Response> addEditors(Long id, Set<Person> personSet) {
        return
                Uni.join().all(
                                personSet.stream().filter(p -> Objects.nonNull(p.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        personSet
                                                .stream()
                                                .filter(p -> Objects.nonNull(p.id))
                                                .map(p -> Person.findById(p.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Person) e).toList())
                        .map(HashSet::new)
                        .map(persons -> personSet.stream().filter(p -> Objects.isNull(p.id)).collect(Collectors.toCollection(() -> persons)))
                        .chain(persons -> movieService.addEditors(id, persons))
                        .map(Movie::getEditors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }*/

    @PUT
    @Path("{id}/actor")
    public Uni<Response> addRole(Long id, MovieActor movieActor) {
        return
                Panache
                        .withTransaction(() ->
                                Objects.nonNull(movieActor.getActor().id)
                                        ? Person.findById(movieActor.getActor().id)
                                        : movieActor.getActor().persist()
                        )
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(() -> movieService.addRole(id, movieActor))
                        .map(
                                movie ->
                                        movie.getMovieActors()
                                                .stream()
                                                .map(movieActor1 -> MovieActor.build(movie, movieActor1.getActor(), movieActor1.getRole(), movieActor1.getRank()))
                                                .collect(Collectors.toSet())
                        )
                        .onItem().ifNotNull().transform(roles -> Response.ok(roles).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
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

    @PUT
    @Path("{id}/genres")
    public Uni<Response> addGenres(Long id, Set<Genre> genreSet) {
        return
                Uni.join().all(
                                genreSet.stream().filter(g -> Objects.nonNull(g.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        genreSet
                                                .stream()
                                                .filter(g -> Objects.nonNull(g.id))
                                                .map(g -> Genre.findById(g.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Genre) e).toList())
                        .map(HashSet::new)
                        .map(genres -> genreSet.stream().filter(g -> Objects.isNull(g.id)).collect(Collectors.toCollection(() -> genres)))
                        .chain(genres -> movieService.addGenres(id, genres))
                        .map(Movie::getGenres)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @PUT
    @Path("{id}/countries")
    public Uni<Response> addCountries(Long id, Set<Country> countrySet) {
        return
                Uni.join().all(
                                countrySet
                                        .stream()
                                        .map(c -> Country.findById(c.id))
                                        .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().map(e -> (Country) e).collect(Collectors.toSet()))
                        .chain(countries -> movieService.addCountries(id, countries))
                        .map(Movie::getCountries)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @PUT
    @Path("{id}/awards")
    public Uni<Response> addAwards(Long id, Set<Award> awardSet) {
        return
                movieService.addAwards(id, awardSet)
                        .map(Movie::getAwards)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @PUT
    @Path("{movieId}/producers/{producerId}")
    public Uni<Response> removeProducer(Long movieId, Long producerId) {
        return
                producerService.removeMovie(movieId, producerId)
                        .chain(() -> movieService.removeProducer(movieId, producerId))
                        .map(Movie::getProducers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/directors/{directorId}")
    public Uni<Response> removeDirector(Long movieId, Long directorId) {
        return
                directorService.removeMovie(directorId, movieId)
                        .chain(() -> movieService.removeDirector(movieId, directorId))
                        .map(Movie::getDirectors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/screenwriters/{screenwriterId}")
    public Uni<Response> removeScreenwriter(Long movieId, Long screenwriterId) {
        return
                screenwriterService.removeMovie(screenwriterId, movieId)
                        .chain(() -> movieService.removeScreenwriter(movieId, screenwriterId))
                        .map(Movie::getScreenwriters)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/musicians/{musicianId}")
    public Uni<Response> removeMusician(Long movieId, Long musicianId) {
        return
                musicianService.removeMovie(musicianId, movieId)
                        .chain(() -> movieService.removeMusician(movieId, musicianId))
                        .map(Movie::getMusicians)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/photographers/{photographerId}")
    public Uni<Response> removePhotographer(Long movieId, Long photographerId) {
        return
                photographerService.removeMovie(photographerId, movieId)
                        .chain(() -> movieService.removePhotographer(movieId, photographerId))
                        .map(Movie::getPhotographers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/costumiers/{costumierId}")
    public Uni<Response> removeCostumier(Long movieId, Long costumierId) {
        return
                costumierService.removeMovie(costumierId, movieId)
                        .chain(() -> movieService.removeCostumier(movieId, costumierId))
                        .map(Movie::getCostumiers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/decorators/{decoratorId}")
    public Uni<Response> removeDecorator(Long movieId, Long decoratorId) {
        return
                decoratorService.removeMovie(decoratorId, movieId)
                        .chain(() -> movieService.removeDecorator(movieId, decoratorId))
                        .map(Movie::getDecorators)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/editors/{editorId}")
    public Uni<Response> removeEditor(Long movieId, Long editorId) {
        return
                editorService.removeMovie(editorId, movieId)
                        .chain(() -> movieService.removeEditor(movieId, editorId))
                        .map(Movie::getEditors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/casters/{casterId}")
    public Uni<Response> removeCaster(Long movieId, Long casterId) {
        return
                casterService.removeMovie(casterId, movieId)
                        .chain(() -> movieService.removeEditor(movieId, casterId))
                        .map(Movie::getCasters)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/art-directors/{artDirectorId}")
    public Uni<Response> removeArtDirectors(Long movieId, Long artDirectorId) {
        return
                artDirectorService.removeMovie(artDirectorId, movieId)
                        .chain(() -> movieService.removeArtDirector(movieId, artDirectorId))
                        .map(Movie::getArtDirectors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/sound-editors/{soundDirectorId}")
    public Uni<Response> removeSoundEditors(Long movieId, Long soundDirectorId) {
        return
                soundEditorService.removeMovie(soundDirectorId, movieId)
                        .chain(() -> movieService.removeArtDirector(movieId, soundDirectorId))
                        .map(Movie::getSoundEditors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/visual-effects-supervisors/{visualEffectsSupervisorId}")
    public Uni<Response> removeVisualEffectsSupervisor(Long movieId, Long visualEffectsSupervisorId) {
        return
                visualEffectsSupervisorService.removeMovie(visualEffectsSupervisorId, movieId)
                        .chain(() -> movieService.removeVisualEffectsSupervisor(movieId, visualEffectsSupervisorId))
                        .map(Movie::getVisualEffectsSupervisors)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/makeup-artists/{makeupArtistId}")
    public Uni<Response> removeMakeupArtists(Long movieId, Long makeupArtistId) {
        return
                makeupArtistService.removeMovie(makeupArtistId, movieId)
                        .chain(() -> movieService.removeMakeupArtist(movieId, makeupArtistId))
                        .map(Movie::getMakeupArtists)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/hair-dressers/{hairDresserId}")
    public Uni<Response> removeHairDressers(Long movieId, Long hairDresserId) {
        return
                hairDresserService.removeMovie(hairDresserId, movieId)
                        .chain(() -> movieService.removeHairDresser(movieId, hairDresserId))
                        .map(Movie::getHairDressers)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/stuntmen/{stuntmanId}")
    public Uni<Response> removeStuntman(Long movieId, Long stuntmanId) {
        return
                stuntmanService.removeMovie(stuntmanId, movieId)
                        .chain(() -> movieService.removeStuntman(movieId, stuntmanId))
                        .map(Movie::getStuntmen)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/genres/{genreId}")
    public Uni<Response> removeGenre(Long movieId, Long genreId) {
        return
                genreService.removeMovie(genreId, movieId)
                        .chain(() -> movieService.removeGenre(movieId, genreId))
                        .map(Movie::getGenres)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{movieId}/countries/{countryId}")
    public Uni<Response> removeCountry(Long movieId, Long countryId) {
        return
                countryService.removeMovie(countryId, movieId)
                        .chain(() -> movieService.removeCountry(movieId, countryId))
                        .map(Movie::getCountries)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
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
        return
                Panache.withTransaction(() -> Movie.deleteById(id))
                        .map(deleted -> Response.ok().status(deleted ? NO_CONTENT : NOT_FOUND).build())
                ;
    }

}
