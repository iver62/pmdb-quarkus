package org.desha.app.webservices;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Screenwriter;
import org.desha.app.services.PersonService;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("screenwriters")
@ApplicationScoped
@Slf4j
public class ScreenwriterResource {

    private final PersonService<Screenwriter> personService;

    @Inject
    public ScreenwriterResource(PersonService<Screenwriter> personService) {
        this.personService = personService;
    }

    @GET
    @Path("{id}")
    public Uni<Screenwriter> getScreenwriter(Long id) {
        return personService.getOne(id);
    }

    @GET
    public Uni<Response> getScreenwriters() {
        return
                personService.getAll()
                        .onItem().ifNotNull().transform(screenwriters -> Response.ok(screenwriters).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(Long id) {
        return
                personService.getOne(id)
                        .chain(personService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    /*@GET
    @Path("{id}/countries")
    public Uni<Response> getCountries(Long id) {
        return
                personService.getOne(id)
                        .chain(personService::getCountries)
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }*/

    /*@GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(Long id) {
        return
                personService.getOne(id)
                        .chain(personService::getAwards)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }*/

    @POST
    public Uni<Response> save(PersonDTO personDTO) {
        return
                personService
                        .save(personDTO, Screenwriter.builder().build())
                        .onItem().ifNotNull()
                        .transform(screenwriter -> Response.ok(screenwriter).status(CREATED).build())
                ;
    }

    /*@PUT
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
    }*/

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, PersonDTO personDTO) {
        if (Objects.isNull(personDTO) || Objects.isNull(personDTO.getName())) {
            throw new WebApplicationException("Person name was not set on request.", 422);
        }

        return
                personService.update(id, personDTO)
                        .onItem().ifNotNull().transform(screenwriter -> Response.ok(screenwriter).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return personService.delete(id)
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build())
                ;
    }

}
