package org.desha.app.webservices;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.entity.*;
import org.desha.app.services.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("persons")
@ApplicationScoped
@Slf4j
public class PersonResource {

    private final PersonService personService;
    private final ArtDirectorService artDirectorService;
    private final CasterService casterService;
    private final CostumierService costumierService;
    private final DecoratorService decoratorService;
    private final DirectorService directorService;
    private final EditorService editorService;
    private final MusicianService musicianService;
    private final PhotographerService photographerService;
    private final ProducerService producerService;
    private final ScreenwriterService screenwriterService;
    private final SoundEditorService soundEditorService;

    @Inject
    public PersonResource(
            ArtDirectorService artDirectorService,
            CasterService casterService,
            CostumierService costumierService,
            DecoratorService decoratorService,
            DirectorService directorService,
            EditorService editorService,
            MusicianService musicianService,
            PersonService personService,
            PhotographerService photographerService,
            ProducerService producerService,
            ScreenwriterService screenwriterService,
            SoundEditorService soundEditorService
    ) {
        this.artDirectorService = artDirectorService;
        this.casterService = casterService;
        this.costumierService = costumierService;
        this.decoratorService = decoratorService;
        this.directorService = directorService;
        this.editorService = editorService;
        this.musicianService = musicianService;
        this.personService = personService;
        this.photographerService = photographerService;
        this.producerService = producerService;
        this.screenwriterService = screenwriterService;
        this.soundEditorService = soundEditorService;
    }

    @GET
    @Path("producers/{id}")
    public Uni<Producer> getProducer(Long id) {
        return producerService.getOne(id);
    }

    @GET
    @Path("directors/{id}")
    public Uni<Director> getDirector(Long id) {
        return directorService.getOne(id);
    }

    @GET
    @Path("screenwriters/{id}")
    public Uni<Screenwriter> getScreenwriter(Long id) {
        return screenwriterService.getOne(id);
    }

    @GET
    @Path("musicians/{id}")
    public Uni<Musician> getMusician(Long id) {
        return musicianService.getOne(id);
    }

    @GET
    @Path("photographers/{id}")
    public Uni<Photographer> getPhotographer(Long id) {
        return photographerService.getOne(id);
    }

    @GET
    @Path("costumiers/{id}")
    public Uni<Costumier> getCostumier(Long id) {
        return costumierService.getOne(id);
    }

    @GET
    @Path("decorators/{id}")
    public Uni<Decorator> getDecorator(Long id) {
        return decoratorService.getOne(id);
    }

    @GET
    @Path("editors/{id}")
    public Uni<Editor> getEditor(Long id) {
        return editorService.getOne(id);
    }

    @GET
    @Path("casters/{id}")
    public Uni<Caster> getCaster(Long id) {
        return casterService.getOne(id);
    }

    @GET
    @Path("art-directors/{id}")
    public Uni<ArtDirector> getArtDirector(Long id) {
        return artDirectorService.getOne(id);
    }

    @GET
    @Path("sound-editors/{id}")
    public Uni<SoundEditor> getSoundEditor(Long id) {
        return soundEditorService.getOne(id);
    }

    @GET
    @Path("producers")
    public Uni<Response> getProducers() {
        return
                producerService.getAll()
                        .onItem().ifNotNull().transform(producers -> Response.ok(producers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("directors")
    public Uni<Response> getDirectors() {
        return
                directorService.getAll()
                        .onItem().ifNotNull().transform(directors -> Response.ok(directors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("screenwriters")
    public Uni<Response> getScreenwriters() {
        return
                screenwriterService.getAll()
                        .onItem().ifNotNull().transform(screenwriters -> Response.ok(screenwriters).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("musicians")
    public Uni<Response> getMusicians() {
        return
                musicianService.getAll()
                        .onItem().ifNotNull().transform(musicians -> Response.ok(musicians).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("photographers")
    public Uni<Response> getPhotographers() {
        return
                photographerService.getAll()
                        .onItem().ifNotNull().transform(photographers -> Response.ok(photographers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("costumiers")
    public Uni<Response> getCostumiers() {
        return
                costumierService.getAll()
                        .onItem().ifNotNull().transform(costumiers -> Response.ok(costumiers).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("decorators")
    public Uni<Response> getDecorators() {
        return
                decoratorService.getAll()
                        .onItem().ifNotNull().transform(decorators -> Response.ok(decorators).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("editors")
    public Uni<Response> getEditors() {
        return
                editorService.getAll()
                        .onItem().ifNotNull().transform(editors -> Response.ok(editors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("casters")
    public Uni<Response> getCasters() {
        return
                casterService.getAll()
                        .onItem().ifNotNull().transform(casters -> Response.ok(casters).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("art-directors")
    public Uni<Response> getArtDirectors() {
        return
                artDirectorService.getAll()
                        .onItem().ifNotNull().transform(artDirectors -> Response.ok(artDirectors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("sound-editors")
    public Uni<Response> getSoundEditors() {
        return
                soundEditorService.getAll()
                        .onItem().ifNotNull().transform(soundEditors -> Response.ok(soundEditors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("producers/{id}/movies")
    public Uni<Response> getMoviesAsProducer(Long id) {
        return
                personService.getProducer(id)
                        .chain(producerService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("directors/{id}/movies")
    public Uni<Response> getMoviesAsDirector(Long id) {
        return
                directorService.getOne(id)
                        .chain(directorService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("screenwriters/{id}/movies")
    public Uni<Response> getMoviesAsScreenwriter(Long id) {
        return
                screenwriterService.getOne(id)
                        .chain(screenwriterService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("musicians/{id}/movies")
    public Uni<Response> getMoviesAsMusician(Long id) {
        return
                musicianService.getOne(id)
                        .chain(musicianService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("photographers/{id}/movies")
    public Uni<Response> getMoviesAsPhotographer(Long id) {
        return
                photographerService.getOne(id)
                        .chain(photographerService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("costumiers/{id}/movies")
    public Uni<Response> getMoviesAsCostumier(Long id) {
        return
                costumierService.getOne(id)
                        .chain(costumierService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("casters/{id}/movies")
    public Uni<Response> getMoviesAsCaster(Long id) {
        return
                casterService.getOne(id)
                        .chain(casterService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/decorator")
    public Uni<Response> getMoviesAsDecorator(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getMoviesAsDecorator)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }


    @GET
    @Path("{id}/movies/editor")
    public Uni<Response> getMoviesAsEditor(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getMoviesAsEditor)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/art-directors")
    public Uni<Response> getMoviesAsArtDirector(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getMoviesAsArtDirector)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/sound-editors")
    public Uni<Response> getMoviesAsSoundEditor(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getMoviesAsSoundEditor)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/roles")
    public Uni<Response> getRoles(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getRolesByActor)
                        .onItem().ifNotNull().transform(roles -> Response.ok(roles).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/countries")
    public Uni<Response> getCountries(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getCountries)
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getAwards)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @POST
    public Uni<Response> createPerson(Person person) {
        return
                Panache
                        .withTransaction(() -> {
                                    person.setCreationDate(LocalDateTime.now());
                                    person.setName(StringUtils.trim(person.getName()));
                                    return person.persist();
                                }
                        )
                        .replaceWith(Response.ok(person).status(CREATED)::build)
                ;
    }

    @POST
    @Path("director")
    public Uni<Response> saveDirector(Director director) {
        return
                Panache
                        .withTransaction(() -> {
                                    director.setCreationDate(LocalDateTime.now());
                                    director.setName(StringUtils.trim(director.getName()));
                                    return director.persist();
                                }
                        )
                        .replaceWith(Response.ok(director).status(CREATED)::build)
                ;
    }

    @PUT
    @Path("{id}/countries")
    public Uni<Response> addCountries(Long id, Set<Country> countrySet) {
        Set<Country> countries = new HashSet<>();
        return
                Uni.join().all(
                                countrySet.stream().filter(c -> Objects.nonNull(c.id)).toList().isEmpty()
                                        ?
                                        List.of(Uni.createFrom().nullItem())
                                        :
                                        countrySet
                                                .stream()
                                                .filter(c -> Objects.nonNull(c.id))
                                                .map(c -> Country.findById(c.id))
                                                .toList()
                        )
                        .usingConcurrencyOf(1)
                        .andFailFast()
                        .map(entities -> entities.stream().filter(Objects::nonNull).map(e -> (Country) e).toList())
                        .map(countryList -> countryList.stream().collect(Collectors.toCollection(() -> countries)))
                        .map(countryList -> countrySet.stream().filter(c -> Objects.isNull(c.id)).collect(Collectors.toCollection(() -> countries)))
                        .chain(countryList -> personService.addCountries(id, countrySet))
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Person person) {
        if (Objects.isNull(person) || Objects.isNull(person.getName())) {
            throw new WebApplicationException("Person lastName was not set on request.", 422);
        }

        return
                personService.updatePerson(id, person)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return
                Panache
                        .withTransaction(() -> Person.deleteById(id))
                        .map(deleted -> deleted
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build());
    }

}
