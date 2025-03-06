package org.desha.app.controller;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.*;

@Slf4j
public abstract class PersonResource<T extends Person> {

    private final PersonService<T> personService;

    @Inject
    protected PersonResource(PersonService<T> personService) {
        this.personService = personService;
    }

    @GET
    @Path("{id}")
    public Uni<T> getPersonById(Long id) {
        return personService.getById(id);
    }

    @GET
    public Uni<Response> getPersons(
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("sort") @DefaultValue("name") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term,
            @QueryParam("country") List<Integer> countryIds,
            @QueryParam("from-birth-date") LocalDate fromBirthDate,
            @QueryParam("to-birth-date") LocalDate toBirthDate,
            @QueryParam("from-death-date") LocalDate fromDeathDate,
            @QueryParam("to-death-date") LocalDate toDeathDate,
            @QueryParam("from-creation-date") LocalDateTime fromCreationDate,
            @QueryParam("to-creation-date") LocalDateTime toCreationDate,
            @QueryParam("from-last-update") LocalDateTime fromLastUpdate,
            @QueryParam("to-last-update") LocalDateTime toLastUpdate
    ) {
        // Vérification de la cohérence des dates
        if (Objects.nonNull(fromBirthDate) && Objects.nonNull(toBirthDate) && fromBirthDate.isAfter(toBirthDate)
                || Objects.nonNull(fromDeathDate) && Objects.nonNull(toDeathDate) && fromDeathDate.isAfter(toDeathDate)
                || Objects.nonNull(fromCreationDate) && Objects.nonNull(toCreationDate) && fromCreationDate.isAfter(toCreationDate)
                || Objects.nonNull(fromLastUpdate) && Objects.nonNull(toLastUpdate) && fromLastUpdate.isAfter(toLastUpdate)
        ) {
            return
                    Uni.createFrom().item(
                            Response.status(Response.Status.BAD_REQUEST)
                                    .entity("La date de début ne peut pas être après la date de fin.")
                                    .build()
                    );
        }

        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                personService.get(pageIndex, size, sort, sortDirection, term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate)
                        .flatMap(personDTOList ->
                                personService.count(term, countryIds, fromBirthDate, toBirthDate, fromDeathDate, toDeathDate, fromCreationDate, toCreationDate, fromLastUpdate, toLastUpdate).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
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
                personService.getMovies(id, page, size, sort, sortDirection, term)
                        .flatMap(movieList ->
                                personService.countMovies(id, term).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
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
                        .save(personDTO)
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
                        .onItem().ifNull().failWith(new NotFoundException("Person with ID " + id + " not found."));
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
        return
                Arrays.stream(Sort.Direction.values())
                        .filter(d -> d.name().equalsIgnoreCase(direction))
                        .findFirst()
                        .orElse(Sort.Direction.Ascending) // Valeur par défaut si invalide
                ;
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

}
