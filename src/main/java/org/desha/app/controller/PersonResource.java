package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
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
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.*;

@Slf4j
public abstract class PersonResource<T extends Person> {

    private final PersonService<T> personService;

    @Inject
    protected PersonResource(PersonService<T> personService) {
        this.personService = personService;
    }

    @GET
    @Path("count")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> countPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        return
                personService.count(CriteriasDTO.build(queryParams))
                        .map(aLong -> Response.ok(aLong).build())
                ;
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonById(@RestPath Long id) {
        return
                personService.getById(id)
                        .map(t -> Response.ok(PersonDTO.fromEntity(t, t.getCountries())).build())
                ;
    }

    @GET
    @Path("search")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> searchByName(@QueryParam("query") String query) {
        if (Objects.isNull(query) || query.trim().isEmpty()) {
            return Uni.createFrom()
                    .item(
                            Response
                                    .status(Response.Status.BAD_REQUEST)
                                    .entity("Le paramètre 'query' est requis")
                                    .build()
                    );
        }

        return
                personService.searchByName(query)
                        .map(personDTOS ->
                                personDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(personDTOS).build()
                        )
                ;
    }

    @GET
    @Path("{id}/full")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersonByIdWithCountriesAndMovies(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        return
                personService.getByIdWithCountriesAndMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, CriteriasDTO.build(queryParams))
                        .map(personDTO ->
                                Response.ok(personDTO).build()
                        )
                ;
    }

    @GET
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getPersons(@BeanParam PersonQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                personService.get(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, criteriasDTO)
                        .flatMap(personDTOList ->
                                personService.count(criteriasDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("all")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getAllPersons() {
        return
                personService.getAll()
                        .onItem().ifNotNull().transform(persons -> Response.ok(persons).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
    }

    @GET
    @Path("countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getCountries(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);

        return
                personService.getCountries(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, queryParams.getTerm())
                        .flatMap(countryList ->
                                personService.countCountries(queryParams.getTerm()).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> getMovies(@RestPath Long id, @BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        Sort.Direction sortDirection = queryParams.validateSortDirection(queryParams.getDirection());

        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriasDTO criteriasDTO = CriteriasDTO.build(queryParams);

        return
                personService.getMovies(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, sortDirection, criteriasDTO)
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
    @Path("photos/{fileName}")
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
    @Path("{id}")
    @RolesAllowed({"user", "admin"})
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

    @PUT
    @Path("{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCountries(@RestPath Long id, Set<CountryDTO> countryDTOs) {
        if (Objects.isNull(countryDTOs)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle.");
        }

        return
                personService.saveCountries(id, countryDTOs)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().status(NOT_FOUND)::build)
                ;
    }

    @PATCH
    @Path("{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCountries(@RestPath Long id, Set<CountryDTO> countryDTOS) {
        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle.");
        }

        return
                personService.addCountries(id, countryDTOS)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @PATCH
    @Path("{personId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCountry(@RestPath Long personId, @RestPath Long countryId) {
        return
                personService.removeCountry(personId, countryId)
                        .onItem().ifNotNull().transform(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build())
                        .onItem().ifNull().continueWith(Response.serverError().build())
                ;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("admin")
    public Uni<Response> delete(@RestPath Long id) {
        return
                personService.delete(id)
                        .map(deleted -> Boolean.TRUE.equals(deleted)
                                ? Response.ok().status(NO_CONTENT).build()
                                : Response.ok().status(NOT_FOUND).build())
                ;
    }

    @DELETE
    @Path("{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCountries(@RestPath Long id) {
        return personService.clearCountries(id).map(deleted -> Response.ok(deleted).build());
    }

}
