package org.desha.app.webservices;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Country;
import org.desha.app.domain.Person;
import org.desha.app.services.PersonService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@Path("persons")
@ApplicationScoped
@Slf4j
public class PersonResource {

    private final PersonService personService;

    @Inject
    public PersonResource(PersonService personService) {
        this.personService = personService;
    }

    @GET
    @Path("{id}")
    public Uni<Person> getSingle(Long id) {
        return Person.findById(id);
    }

    @GET
    public Uni<List<Person>> get() {
        return Person.listAll();
    }

    @GET
    @Path("producers")
    public Uni<Response> getProducers() {
        return
                personService.getProducers()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("directors")
    public Uni<Response> getDirectors() {
        return
                personService.getDirectors()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("screenwriters")
    public Uni<Response> getScreenwriters() {
        return
                personService.getScreenwriters()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("musicians")
    public Uni<Response> getMusicians() {
        return
                personService.getMusicians()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("photographers")
    public Uni<Response> getPhotographers() {
        return
                personService.getPhotographers()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("costumiers")
    public Uni<Response> getCostumiers() {
        return
                personService.getCostumiers()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("decorators")
    public Uni<Response> getDecorators() {
        return
                personService.getDecorators()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("editors")
    public Uni<Response> getEditors() {
        return
                personService.getEditors()
                        .onItem().ifNotNull().transform(people -> Response.ok(people).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/producer")
    public Uni<Response> getMoviesAsProducer(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsProducer)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/director")
    public Uni<Response> getMoviesAsDirector(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsDirector)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/screenwriter")
    public Uni<Response> getMoviesAsScreenwriter(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsScreenwriter)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/musician")
    public Uni<Response> getMoviesAsMusician(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsMusician)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/photographer")
    public Uni<Response> getMoviesAsPhotographer(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsPhotographer)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/costumier")
    public Uni<Response> getMoviesAsCostumier(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsCostumier)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies/decorator")
    public Uni<Response> getMoviesAsDecorator(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
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
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getMoviesAsEditor)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/roles")
    public Uni<Response> getRoles(Long id) {
        return
                Person.findById(id)
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
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
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
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
                        .map(panacheEntityBase -> (Person) panacheEntityBase)
                        .chain(personService::getAwards)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
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
        if (Objects.isNull(person) || Objects.isNull(person.getLastName())) {
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
