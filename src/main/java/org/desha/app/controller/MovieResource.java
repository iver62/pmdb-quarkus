package org.desha.app.controller;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.TechnicalSummaryDTO;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.CountryService;
import org.desha.app.service.GenreService;
import org.desha.app.service.MovieService;
import org.desha.app.service.PersonService;
import org.jboss.resteasy.reactive.RestPath;

import java.time.LocalDateTime;
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
            @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService
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
    }

    @GET
    @Path("count")
    public Uni<Response> count() {
        return PanacheEntityBase.count()
                .onItem().ifNotNull().transform(aLong -> Response.ok(aLong).build());
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return
                movieService.getSingle(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                        .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build())
                ;
    }

    @GET
    public Uni<Response> get() {
        return
                Movie.listAll()
                        .onItem().ifNotNull().transform(panacheEntityBases ->
                                panacheEntityBases.isEmpty()
                                        ?
                                        Response.noContent().build()
                                        :
                                        Response.ok(panacheEntityBases).build()
                        )
                        .onItem().ifNull().continueWith(Response.noContent().build())
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
    @Path("{id}/producers")
    public Uni<Set<Producer>> getProducers(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getProducersByMovie)
                ;
    }

    @GET
    @Path("{id}/directors")
    public Uni<Set<Director>> getDirectors(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getDirectorsByMovie)
                ;
    }

    @GET
    @Path("{id}/screenwriters")
    public Uni<Set<Screenwriter>> getScreenwriters(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getScreenwritersByMovie)
                ;
    }

    @GET
    @Path("{id}/musicians")
    public Uni<Set<Musician>> getMusicians(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getMusiciansByMovie)
                ;
    }

    @GET
    @Path("{id}/photographers")
    public Uni<Set<Photographer>> getPhotographers(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getPhotographersByMovie)
                ;
    }

    @GET
    @Path("{id}/costumiers")
    public Uni<Set<Costumier>> getCostumiers(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getCostumiersByMovie)
                ;
    }

    @GET
    @Path("{id}/decorators")
    public Uni<Set<Decorator>> getDecorators(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getDecoratorsByMovie)
                ;
    }

    @GET
    @Path("{id}/editors")
    public Uni<Set<Editor>> getEditors(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getEditorsByMovie)
                ;
    }

    @GET
    @Path("{id}/casters")
    public Uni<Set<Caster>> getCasters(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getCastersByMovie)
                ;
    }

    @GET
    @Path("{id}/art-directors")
    public Uni<Set<ArtDirector>> getArtDirectors(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getArtDirectorsByMovie)
                ;
    }

    @GET
    @Path("{id}/sound-editors")
    public Uni<Set<SoundEditor>> getSoundEditors(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getSoundEditorsByMovie)
                ;
    }

    @GET
    @Path("{id}/visual-effects-supervisors")
    public Uni<Set<VisualEffectsSupervisor>> getVisualEffectsSupervisors(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getVisualEffectsSupervisorsByMovie)
                ;
    }

    @GET
    @Path("{id}/makeup-artists")
    public Uni<Set<MakeupArtist>> getMakeupArtists(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getMakeupArtists)
                ;
    }

    @GET
    @Path("{id}/hairdressers")
    public Uni<Set<HairDresser>> getHairDressers(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getHairDressers)
                ;
    }

    @GET
    @Path("{id}/actors")
    public Uni<Response> getRoles(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getActorsByMovie)
                        .onItem().transform(roles -> Response.ok(roles).build())
                        .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build())
                ;
    }

    @GET
    @Path("{id}/genres")
    public Uni<Set<Genre>> getGenres(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getGenresByMovie)
                ;
    }

    @GET
    @Path("{id}/countries")
    public Uni<Set<Country>> getCountries(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getCountriesByMovie)
                ;
    }

    @GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(Long id) {
        return
                Movie.findById(id)
                        .map(Movie.class::cast)
                        .chain(movieService::getAwardsByMovie)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.status(404, "Ce film n'existe pas").build())
                ;
    }

    @POST
    public Uni<Response> create(MovieDTO movieDTO) {
        if (Objects.isNull(movieDTO)) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                movieService.createMovie(movieDTO)
                        .map(movie -> Response.status(CREATED).entity(movie).build());
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
    @Path("{id}/technical-summary")
    public Uni<Response> addTechnicalSummary(Long id, TechnicalSummaryDTO technicalSummary) {
        return
                movieService.saveTechnicalSummary(id, technicalSummary)
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
                                                .map(movieActor1 -> MovieActor.build(null, movieActor1.getActor(), movieActor1.getName()))
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
    @Path("{movieId}/hairdressers/{hairDresserId}")
    public Uni<Response> removeHairDressers(Long movieId, Long hairDresserId) {
        return
                hairDresserService.removeMovie(hairDresserId, movieId)
                        .chain(() -> movieService.removeHairDresser(movieId, hairDresserId))
                        .map(Movie::getMakeupArtists)
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
                                .invoke(
                                        entity -> {
                                            entity.removeRole(actorId);
                                            entity.setLastUpdate(LocalDateTime.now());
                                        }
                                )
                                .chain(entity -> entity.persist())
                        )
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, MovieDTO movieDTO) {
        if (Objects.isNull(movieDTO) || Objects.isNull(movieDTO.getTitle())) {
            throw new WebApplicationException("Movie title was not set on request.", 422);
        }

        return
                movieService.updateMovie(id, movieDTO)
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
