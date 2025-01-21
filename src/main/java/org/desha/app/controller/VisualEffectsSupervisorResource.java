package org.desha.app.controller;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.VisualEffectsSupervisor;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Path("visual-effects-supervisors")
@ApplicationScoped
@Slf4j
public class VisualEffectsSupervisorResource {

    private final PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService;

    @Inject
    public VisualEffectsSupervisorResource(@PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonService<VisualEffectsSupervisor> visualEffectsSupervisorService) {
        this.visualEffectsSupervisorService = visualEffectsSupervisorService;
    }

    @GET
    @Path("{id}")
    public Uni<VisualEffectsSupervisor> getVisualEffectsSupervisor(Long id) {
        return visualEffectsSupervisorService.getOne(id);
    }

    @GET
    public Uni<Response> getVisualEffectsSupervisors() {
        return
                visualEffectsSupervisorService.getAll()
                        .onItem().ifNotNull().transform(visualEffectsSupervisors -> Response.ok(visualEffectsSupervisors).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(Long id) {
        return
                visualEffectsSupervisorService.getOne(id)
                        .chain(visualEffectsSupervisorService::getMovies)
                        .onItem().ifNotNull().transform(movies -> Response.ok(movies).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    /*@GET
    @Path("{id}/countries")
    public Uni<Response> getCountries(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getCountries)
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }*/

    /*@GET
    @Path("{id}/awards")
    public Uni<Response> getAwards(Long id) {
        return
                Person.findById(id)
                        .map(Person.class::cast)
                        .chain(personService::getAwards)
                        .onItem().ifNotNull().transform(awards -> Response.ok(awards).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }*/

    @POST
    public Uni<Response> save(PersonDTO personDTO) {
        return
                visualEffectsSupervisorService
                        .save(personDTO, VisualEffectsSupervisor.builder().build())
                        .onItem().ifNotNull()
                        .transform(visualEffectsSupervisor -> Response.ok(visualEffectsSupervisor).status(CREATED).build())
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
        if (Objects.isNull(personDTO) || Objects.isNull(personDTO.name())) {
            throw new WebApplicationException("Person name was not set on request.", 422);
        }

        return
                visualEffectsSupervisorService.update(id, personDTO)
                        .onItem().ifNotNull().transform(visualEffectsSupervisor -> Response.ok(visualEffectsSupervisor).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return
                visualEffectsSupervisorService.delete(id)
                        .map(deleted -> deleted
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build())
                ;
    }

}
