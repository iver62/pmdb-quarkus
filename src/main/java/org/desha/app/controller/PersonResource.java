package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.service.PersonService;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("persons")
@ApplicationScoped
public class PersonResource {

    private final PersonService personService;

    @Inject
    public PersonResource(PersonService personService) {
        this.personService = personService;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonById(@RestPath Long id) {
        return
                personService.getById(id)
                        .map(personDTO -> Response.ok(personDTO).build())
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    @Path("/movies-number")
    public Uni<Response> getPersonsWithMoviesNumber(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                personService.getPersonsWithMovieNumbers(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/producers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getProducers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.PRODUCER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/directors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getDirectors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.DIRECTOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/screenwriters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getScreenwriters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.SCREENWRITER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/musicians")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMusicians(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.MUSICIAN);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/photographers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPhotographers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.PHOTOGRAPHER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/costumiers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCostumiers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.COSTUMIER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/decorators")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getDecorators(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.DECORATOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.EDITOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/casters")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCasters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.CASTER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.ARTIST);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sound-editors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSoundEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.SOUND_EDITOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/vfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getVfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.VFX_SUPERVISOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sfx-supervisors")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getSfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.SFX_SUPERVISOR);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/makeup-artists")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMakeupArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.MAKEUP_ARTIST);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/hair-dressers")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getHairDressers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.HAIR_DRESSER);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/stuntmen")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getStuntmen(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams, PersonType.STUNT_MAN);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMoviesByPerson(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                personService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriasDTO)
                        .flatMap(movieList ->
                                personService.countMovies(id, criteriasDTO)
                                        .map(total ->
                                                movieList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                ;
    }

    @GET
    @Path("/photos/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    @RolesAllowed({"user", "admin"})
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

    @GET
    @Path("/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                personService.getCountries(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .flatMap(countryList ->
                                personService.countCountries(term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMovieCountriesByPerson(@RestPath Long id, @BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                personService.getMovieCountriesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .flatMap(countryList ->
                                personService.countMovieCountriesByPerson(id, term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAwardsByPerson(@RestPath Long id) {
        return
                personService.getAwardsByPerson(id).map(awardDTOS ->
                        awardDTOS.isEmpty()
                                ? Response.noContent().build()
                                : Response.ok(awardDTOS).build()
                );
    }

    @POST
    @RolesAllowed({"user", "admin"})
    public Uni<Response> save(PersonDTO personDTO) {
        return
                personService
                        .save(personDTO)
                        .onItem().ifNotNull()
                        .transform(decorator -> Response.ok(decorator).status(CREATED).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> update(@RestPath Long id, @RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) PersonDTO personDTO) {
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
    @Path("/{id}")
    @RolesAllowed("admin")
    public Uni<Response> delete(@RestPath Long id) {
        return
                personService
                        .deletePerson(id)
                        .onItem().ifNotNull().transform(person -> Response.ok(person).build())
                        .onFailure().recoverWithItem(e -> {
                                    log.error(e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la suppression de la personne")
                                            .build();
                                }
                        )
                ;
    }
}
