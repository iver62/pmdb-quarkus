package org.desha.app.controller;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.service.PersonService;
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

import static jakarta.ws.rs.core.Response.Status.*;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public abstract class PersonResource<T extends Person> {

    private PersonService<T> personService;
    private Class<T> personClass; // Ajout d'un attribut pour stocker le type

    @Inject
    protected PersonResource(PersonService<T> personService, Class<T> personClass) {
        this.personService = personService;
        this.personClass = personClass;
    }

    @GET
    @Path("{id}")
    public Uni<T> getPersonById(Long id) {
        return personService.getOne(id);
    }

    @GET
    public Uni<Response> getPersons(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("name") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                personService.get(page, size, sort, sortDirection, term)
                        .flatMap(tList ->
                                personService.count(term).map(total ->
                                        tList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(tList.stream().map(PersonDTO::fromEntity)).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("all")
    public Uni<Response> getAllPersons() {
        return
                personService.getAll()
                        .onItem().ifNotNull().transform(persons -> Response.ok(persons).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMovies(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Movie.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                personService.getMovies(id, personClass, page, size, sort, sortDirection, term)
                        .flatMap(movieList ->
                                personService.countMovies(id, personClass, term).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/countries")
    public Uni<Response> getCountriesByPerson(Long id) {
        return
                personService.getCountries(id)
                        .onItem().transform(countries -> Objects.nonNull(countries) && !countries.isEmpty()
                                ? Response.ok(countries).build()
                                : Response.noContent().build()
                        )
                ;
    }

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

    @GET
    @Path("photos/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    public Uni<Response> getPhoto(@PathParam("fileName") String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty() || Objects.equals("undefined", fileName)) {
            log.warn("Invalid file request: {}", fileName);
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid file name").build());
        }

        return
                personService.getPhoto(fileName)
                        .onItem().ifNotNull().transformToUni(
                                file -> Uni.createFrom().item(() -> {
                                    try {
                                        byte[] fileBytes = Files.readAllBytes(file.toPath());
                                        String mimeType = Files.probeContentType(file.toPath());

                                        log.info("Serving photo: {}", fileName);
                                        return Response.ok(fileBytes).type(mimeType).build();
                                    } catch (IOException e) {
                                        log.error("Error loading photo {}: {}", fileName, e.getMessage());
                                        return Response.serverError().entity("Erreur lors du chargement de la photo").build();
                                    }
                                })
                        )
                        .onItem().ifNull().continueWith(() -> {
                            log.warn("Photo not found: {}", fileName);
                            return Response.status(Response.Status.NOT_FOUND).entity("Photo introuvable").build();
                        });
    }

    @POST
    public Uni<Response> save(PersonDTO personDTO) {
        return
                personService
                        .save(personDTO, createEntityInstance())
                        .onItem().ifNotNull()
                        .transform(decorator -> Response.ok(decorator).status(CREATED).build())
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
    public Uni<Response> update(
            @RestPath Long id,
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) PersonDTO personDTO
    ) {
        if (Objects.isNull(personDTO) || Objects.isNull(personDTO.getName())) {
            throw new WebApplicationException("Person name was not set on request.", 422);
        }

        return
                personService
                        .update(id, file, personDTO)
                        .onItem().ifNotNull().transform(person -> Response.ok(person).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@RestPath Long id) {
        return
                personService.delete(id)
                        .map(deleted -> deleted
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build())
                ;
    }

    protected Sort.Direction validateSortDirection(String direction) {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

    protected Uni<Response> validateSortField(String sort, List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sort)) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(MessageFormat.format("Le champ de tri \"{0}\" est invalide. Valeurs autorisées : {1}", sort, allowedSortFields))
                            .build()
            );
        }
        return null;
    }

    // Méthode abstraite pour permettre aux sous-classes de définir l'instance correcte de l'entité
    protected abstract T createEntityInstance();

}
