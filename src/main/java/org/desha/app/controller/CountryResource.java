package org.desha.app.controller;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.service.CountryService;
import org.jboss.resteasy.reactive.RestPath;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("countries")
@ApplicationScoped
@Slf4j
public class CountryResource {
    
    private final CountryService countryService;

    @Inject
    public CountryResource(CountryService countryService) {
        this.countryService = countryService;
    }

    @GET
    @Path("{id}")
    public Uni<Response> getCountry(Long id) {
        return
                Country.getById(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    public Uni<Response> getCountries() {
        return
                Country.getAll()
                        .onItem().ifNotNull().transform(countries -> Response.ok(countries).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @Path("{id}/full")
    public Uni<Response> getFullCountry(Long id) {
        return
                countryService.getFull(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    @Path("{id}/movies/all")
    public Uni<Response> getAllMoviesByCountry(
            @RestPath Long id,
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
                Country.getMovies(id, sort, sortDirection, term)
                        .flatMap(movieList ->
                                Country.countMovies(id, term).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/movies")
    public Uni<Response> getMoviesByCountry(
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
                Country.getMovies(id, page, size, sort, sortDirection, term)
                        .flatMap(movieList ->
                                Country.countMovies(id, term).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/actors")
    public Uni<Response> getActorsByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getActors(id, page, size, sort, sortDirection, term)
                        .flatMap(actorList ->
                                Country.countActors(id, term).map(total ->
                                        actorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(actorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/producers")
    public Uni<Response> getProducersByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getProducers(id, page, size, sort, sortDirection, term)
                        .flatMap(producerList ->
                                Country.countProducers(id, term).map(total ->
                                        producerList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(producerList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/directors")
    public Uni<Response> getDirectorsByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getDirectors(id, page, size, sort, sortDirection, term)
                        .flatMap(directorList ->
                                Country.countDirectors(id, term).map(total ->
                                        directorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(directorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/screenwriters")
    public Uni<Response> getScreenwritersByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getScreenwriters(id, page, size, sort, sortDirection, term)
                        .flatMap(screenwriterList ->
                                Country.countScreenwriters(id, term).map(total ->
                                        screenwriterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(screenwriterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/musicians")
    public Uni<Response> getMusiciansByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getMusicians(id, page, size, sort, sortDirection, term)
                        .flatMap(musicianList ->
                                Country.countMusicians(id, term).map(total ->
                                        musicianList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(musicianList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/decorators")
    public Uni<Response> getDecoratorsByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getDecorators(id, page, size, sort, sortDirection, term)
                        .flatMap(decoratorList ->
                                Country.countDecorators(id, term).map(total ->
                                        decoratorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(decoratorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/costumiers")
    public Uni<Response> getCostumiers(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getCostumiers(id, page, size, sort, sortDirection, term)
                        .flatMap(costumierList ->
                                Country.countCostumiers(id, term).map(total ->
                                        costumierList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(costumierList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/photographers")
    public Uni<Response> getPhotographersByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getPhotographers(id, page, size, sort, sortDirection, term)
                        .flatMap(photographerList ->
                                Country.countPhotographers(id, term).map(total ->
                                        photographerList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(photographerList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/editors")
    public Uni<Response> getEditorsByCountry(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getEditors(id, page, size, sort, sortDirection, term)
                        .flatMap(editorList ->
                                Country.countEditors(id, term).map(total ->
                                        editorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(editorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/casters")
    public Uni<Response> getCasters(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getCasters(id, page, size, sort, sortDirection, term)
                        .flatMap(casterList ->
                                Country.countCasters(id, term).map(total ->
                                        casterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(casterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/art-directors")
    public Uni<Response> getArtDirectors(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getArtDirectors(id, page, size, sort, sortDirection, term)
                        .flatMap(artDirectorList ->
                                Country.countArtDirectors(id, term).map(total ->
                                        artDirectorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(artDirectorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/sound-editors")
    public Uni<Response> getSoundEditors(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getSoundEditors(id, page, size, sort, sortDirection, term)
                        .flatMap(soundEditorList ->
                                Country.countSoundEditors(id, term).map(total ->
                                        soundEditorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(soundEditorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/visual-effects-supervisors")
    public Uni<Response> getVisualEffectsSupervisors(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getVisualEffectsSupervisors(id, page, size, sort, sortDirection, term)
                        .flatMap(visualEffectsSupervisorList ->
                                Country.countVisualEffectsSupervisors(id, term).map(total ->
                                        visualEffectsSupervisorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(visualEffectsSupervisorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/makeup-artists")
    public Uni<Response> getMakeupArtists(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getMakeupArtists(id, page, size, sort, sortDirection, term)
                        .flatMap(makeupArtistList ->
                                Country.countMakeupArtists(id, term).map(total ->
                                        makeupArtistList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(makeupArtistList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/hair-dressers")
    public Uni<Response> getHairDressers(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getHairDressers(id, page, size, sort, sortDirection, term)
                        .flatMap(hairDresserList ->
                                Country.countHairDressers(id, term).map(total ->
                                        hairDresserList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(hairDresserList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/stuntmen")
    public Uni<Response> getStuntmen(
            @RestPath Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Person.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                Country.getStuntmen(id, page, size, sort, sortDirection, term)
                        .flatMap(stuntmanList ->
                                Country.countStuntmen(id, term).map(total ->
                                        stuntmanList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(stuntmanList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, CountryDTO countryDTO) {
        if (Objects.isNull(countryDTO) || Objects.isNull(countryDTO.getNomFrFr())) {
            throw new WebApplicationException("Country name was not set on request.", 422);
        }

        return
                Country.update(id, countryDTO)
                        .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                        .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build)
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    private Sort.Direction validateSortDirection(String direction) {
        return Arrays.stream(Sort.Direction.values())
                .filter(d -> d.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(Sort.Direction.Ascending); // Valeur par défaut si invalide
    }

    private Uni<Response> validateSortField(String sort, List<String> allowedSortFields) {
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
