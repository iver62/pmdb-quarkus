package org.desha.app.controller;

import io.quarkus.panache.common.Page;
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
                countryService.getById(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build());
    }

    @GET
    public Uni<Response> getCountries(
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("sort") @DefaultValue("nomFrFr") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Country.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                countryService.getCountries(Page.of(pageIndex, size), sort, sortDirection, term)
                        .flatMap(countryList ->
                                countryService.countCountries(term).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("all")
    public Uni<Response> getCountries(
            @QueryParam("sort") @DefaultValue("nomFrFr") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("term") @DefaultValue("") String term
    ) {
        Uni<Response> sortValidation = validateSortField(sort, Country.ALLOWED_SORT_FIELDS);
        if (Objects.nonNull(sortValidation)) {
            return sortValidation;
        }

        Sort.Direction sortDirection = validateSortDirection(direction);

        return
                countryService.getCountries(sort, sortDirection, term)
                        .flatMap(countryList ->
                                countryService.countCountries(term).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("{id}/full")
    public Uni<Response> getFullCountry(Long id) {
        return
                countryService.getFull(id)
                        .onItem().ifNotNull().transform(country -> Response.ok(country).build())
                        .onItem().ifNull().continueWith(Response.noContent().build())
                ;
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
                countryService.getAllMovies(id, sort, sortDirection, term)
                        .flatMap(movieList ->
                                countryService.countMovies(id, term).map(total ->
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
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("50") int size,
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
                countryService.getMovies(id, Page.of(pageIndex, size), sort, sortDirection, term)
                        .flatMap(movieList ->
                                countryService.countMovies(id, term).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
                        )
                ;
    }

    /*@GET
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
                countryService.getActorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(actorList ->
                                countryService.countActorsByCountry(id, term).map(total ->
                                        actorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(actorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getProducersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(producerList ->
                                countryService.countProducersByCountry(id, term).map(total ->
                                        producerList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(producerList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getDirectorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(directorList ->
                                countryService.countDirectorsByCountry(id, term).map(total ->
                                        directorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(directorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getScreenwritersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(screenwriterList ->
                                countryService.countScreenwritersByCountry(id, term).map(total ->
                                        screenwriterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(screenwriterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getMusiciansByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(musicianList ->
                                countryService.countMusiciansByCountry(id, term).map(total ->
                                        musicianList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(musicianList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

   /* @GET
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
                countryService.getDecoratorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(decoratorList ->
                                countryService.countDecoratorsByCountry(id, term).map(total ->
                                        decoratorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(decoratorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getCostumiersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(costumierList ->
                                countryService.countCostumiersByCountry(id, term).map(total ->
                                        costumierList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(costumierList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getPhotographersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(photographerList ->
                                countryService.countPhotographersByCountry(id, term).map(total ->
                                        photographerList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(photographerList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getEditorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(editorList ->
                                countryService.countEditorsByCountry(id, term).map(total ->
                                        editorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(editorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getCastersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(casterList ->
                                countryService.countCastersByCountry(id, term).map(total ->
                                        casterList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(casterList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

   /* @GET
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
                countryService.getArtDirectorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(artDirectorList ->
                                countryService.countArtDirectorsByCountry(id, term).map(total ->
                                        artDirectorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(artDirectorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getSoundEditorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(soundEditorList ->
                                countryService.countSoundEditorsByCountry(id, term).map(total ->
                                        soundEditorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(soundEditorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getVisualEffectsSupervisorsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(visualEffectsSupervisorList ->
                                countryService.countVisualEffectsSupervisorsByCountry(id, term).map(total ->
                                        visualEffectsSupervisorList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(visualEffectsSupervisorList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getMakeupArtistsByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(makeupArtistList ->
                                countryService.countMakeupArtistsByCountry(id, term).map(total ->
                                        makeupArtistList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(makeupArtistList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getHairDressersByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(hairDresserList ->
                                countryService.countHairDressersByCountry(id, term).map(total ->
                                        hairDresserList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(hairDresserList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    /*@GET
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
                countryService.getStuntmenByCountry(id, page, size, sort, sortDirection, term)
                        .flatMap(stuntmanList ->
                                countryService.countStuntmenByCountry(id, term).map(total ->
                                        stuntmanList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(stuntmanList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }*/

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, CountryDTO countryDTO) {
        if (Objects.isNull(countryDTO) || Objects.isNull(countryDTO.getNomFrFr())) {
            throw new WebApplicationException("Country name was not set on request.", 422);
        }

        return
                countryService.update(id, countryDTO)
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
