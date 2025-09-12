package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
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
import org.desha.app.domain.enums.PersonType;
import org.desha.app.service.PersonService;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Slf4j
@Path("/persons")
@ApplicationScoped
public class PersonResource {

    private final PersonService personService;

    @Inject
    public PersonResource(PersonService personService) {
        this.personService = personService;
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getPersonById(@RestPath Long id) {
        return
                personService.getById(id)
                        .map(personDTO -> Response.ok(personDTO).build())
                ;
    }

    @GET
    @Path("/{id}/light")
    public Uni<Response> getLightPersonById(@RestPath Long id) {
        return
                personService.getLightById(id)
                        .map(lightPersonDTO -> Response.ok(lightPersonDTO).build())
                ;
    }

    @GET
    @Path("/light")
    public Uni<Response> getLightPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    public Uni<Response> getPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/roles")
    public Uni<Response> getRolesByPerson(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(MovieActor.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, MovieActor.ALLOWED_SORT_FIELDS);

        return
                personService.getRoles(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection())
                        .flatMap(personDTOList ->
                                personService.countRolesByPerson(id).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/actors")
    public Uni<Response> getActors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ACTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/directors")
    public Uni<Response> getDirectors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.DIRECTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/assistant-directors")
    public Uni<Response> getAssistantDirectors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ASSISTANT_DIRECTOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/screenwriters")
    public Uni<Response> getScreenwriters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SCREENWRITER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/producers")
    public Uni<Response> getProducers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.PRODUCER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/composers")
    public Uni<Response> getComposers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.COMPOSER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/musicians")
    public Uni<Response> getMusicians(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.MUSICIAN);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/photographers")
    public Uni<Response> getPhotographers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.PHOTOGRAPHER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/costume-designers")
    public Uni<Response> getCostumeDesigners(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.COSTUME_DESIGNER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/set-designers")
    public Uni<Response> getSetDesigners(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SET_DESIGNER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/editors")
    public Uni<Response> getEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.EDITOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/casters")
    public Uni<Response> getCasters(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.CASTER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/artists")
    public Uni<Response> getArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.ARTIST);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sound-editors")
    public Uni<Response> getSoundEditors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SOUND_EDITOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/makeup-artists")
    public Uni<Response> getMakeupArtists(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.MAKEUP_ARTIST);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/vfx-supervisors")
    public Uni<Response> getVfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.VFX_SUPERVISOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/sfx-supervisors")
    public Uni<Response> getSfxSupervisors(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.SFX_SUPERVISOR);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/hair-dressers")
    public Uni<Response> getHairDressers(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.HAIR_DRESSER);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/stuntmen")
    public Uni<Response> getStuntmen(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams, PersonType.STUNT_MAN);

        return
                personService.getLightPersons(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(personDTOList ->
                                personService.countPersons(criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies")
    public Uni<Response> getMoviesByPerson(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                personService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(movieList ->
                                personService.countMovies(id, criteriaDTO)
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
    public Uni<Response> getMovieCountriesByPerson(@RestPath Long id, @BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                personService.getMovieCountriesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .flatMap(countryDTOList ->
                                personService.countMovieCountriesByPerson(id, term, finalLang).map(total ->
                                        countryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/movies/categories")
    public Uni<Response> getMovieCategoriesByPerson(@RestPath Long id, @BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();

        return
                personService.getMovieCategoriesByPerson(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term)
                        .flatMap(categoryDTOList ->
                                personService.countMovieCategoriesByPerson(id, term).map(total ->
                                        categoryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(categoryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/awards")
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
    public Uni<Response> save(@Valid PersonDTO personDTO) {
        if (Objects.isNull(personDTO) || Objects.nonNull(personDTO.getId())) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return
                personService
                        .save(personDTO)
                        .onItem().ifNotNull()
                        .transform(person -> Response.ok(person).status(CREATED).build())
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
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la modification de la personne: {}", e.getMessage());
                                    return
                                            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity("Erreur lors de la modification de la personne")
                                                    .build()
                                            ;
                                }
                        )
                ;
    }

    @PUT
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> updateCountries(@RestPath Long id, Set<CountryDTO> countryDTOSet) {
        if (Objects.isNull(countryDTOSet)) {
            throw new WebApplicationException("La liste des pays ne peut être nulle.", 422);
        }

        return
                personService
                        .updateCountries(id, countryDTOSet)
                        .onItem().ifNotNull().transform(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la mise à jour des pays: {}", e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la mise à jour des pays")
                                            .build();
                                }
                        )
                ;
    }

    @PATCH
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCountries(@RestPath Long id, Set<CountryDTO> countryDTOSet) {
        if (Objects.isNull(countryDTOSet)) {
            throw new WebApplicationException("La liste des pays ne peut être nulle.", 422);
        }

        return
                personService
                        .addCountries(id, countryDTOSet)
                        .onItem().ifNotNull().transform(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de l'ajout des pays: {}", e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de l'ajout des pays")
                                            .build();
                                }
                        )
                ;
    }

    @PATCH
    @Path("/{personId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCountry(@RestPath Long personId, @RestPath Long countryId) {
        return
                personService
                        .removeCountry(personId, countryId)
                        .onItem().ifNotNull().transform(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la suppression du pays: {}", e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la suppression du pays")
                                            .build();
                                }
                        )
                ;
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
                                    log.error("Erreur lors de la suppression de la personne: {}", e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la suppression de la personne")
                                            .build();
                                }
                        )
                ;
    }

    @DELETE
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCountries(@RestPath Long id) {
        return
                personService
                        .clearCountries(id)
                        .onItem().ifNotNull().transform(person -> Response.ok(person).build())
                        .onFailure().recoverWithItem(e -> {
                                    log.error("Erreur lors de la suppression des pays: {}", e.getMessage());
                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity("Erreur lors de la suppression des pays")
                                            .build();
                                }
                        )
                ;
    }
}
