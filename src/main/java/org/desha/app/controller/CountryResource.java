package org.desha.app.controller;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.service.CountryService;
import org.jboss.resteasy.reactive.RestPath;

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
    public Uni<Response> getMoviesByCountry(
            @RestPath Long id,
            @QueryParam("sort") @DefaultValue("title") String sort,
            @QueryParam("direction") @DefaultValue("Ascending") String direction,
            @QueryParam("title") @DefaultValue("") String title
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getMovies(id, sort, sortDirection, title)
                        .flatMap(movieList ->
                                Country.countMovies(id, title).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(movieList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("title") @DefaultValue("") String title
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getMovies(id, page, size, sort, sortDirection, title)
                        .flatMap(movieList ->
                                Country.countMovies(id, title).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(movieList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getActors(id, page, size, sort, sortDirection, name)
                        .flatMap(actorList ->
                                Country.countActors(id, name).map(total ->
                                        actorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(actorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getProducers(id, page, size, sort, sortDirection, name)
                        .flatMap(producerList ->
                                Country.countProducers(id, name).map(total ->
                                        producerList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(producerList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getDirectors(id, page, size, sort, sortDirection, name)
                        .flatMap(directorList ->
                                Country.countDirectors(id, name).map(total ->
                                        directorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(directorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getScreenwriters(id, page, size, sort, sortDirection, name)
                        .flatMap(screenwriterList ->
                                Country.countScreenwriters(id, name).map(total ->
                                        screenwriterList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(screenwriterList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getMusicians(id, page, size, sort, sortDirection, name)
                        .flatMap(musicianList ->
                                Country.countMusicians(id, name).map(total ->
                                        musicianList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(musicianList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getDecorators(id, page, size, sort, sortDirection, name)
                        .flatMap(decoratorList ->
                                Country.countDecorators(id, name).map(total ->
                                        decoratorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(decoratorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getCostumiers(id, page, size, sort, sortDirection, name)
                        .flatMap(costumierList ->
                                Country.countCostumiers(id, name).map(total ->
                                        costumierList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(costumierList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getPhotographers(id, page, size, sort, sortDirection, name)
                        .flatMap(photographerList ->
                                Country.countPhotographers(id, name).map(total ->
                                        photographerList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(photographerList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getEditors(id, page, size, sort, sortDirection, name)
                        .flatMap(editorList ->
                                Country.countEditors(id, name).map(total ->
                                        editorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(editorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getCasters(id, page, size, sort, sortDirection, name)
                        .flatMap(casterList ->
                                Country.countCasters(id, name).map(total ->
                                        casterList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(casterList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getArtDirectors(id, page, size, sort, sortDirection, name)
                        .flatMap(artDirectorList ->
                                Country.countArtDirectors(id, name).map(total ->
                                        artDirectorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(artDirectorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getSoundEditors(id, page, size, sort, sortDirection, name)
                        .flatMap(soundEditorList ->
                                Country.countSoundEditors(id, name).map(total ->
                                        soundEditorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(soundEditorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getVisualEffectsSupervisors(id, page, size, sort, sortDirection, name)
                        .flatMap(visualEffectsSupervisorList ->
                                Country.countVisualEffectsSupervisors(id, name).map(total ->
                                        visualEffectsSupervisorList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(visualEffectsSupervisorList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getMakeupArtists(id, page, size, sort, sortDirection, name)
                        .flatMap(makeupArtistList ->
                                Country.countMakeupArtists(id, name).map(total ->
                                        makeupArtistList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(makeupArtistList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getHairDressers(id, page, size, sort, sortDirection, name)
                        .flatMap(hairDresserList ->
                                Country.countHairDressers(id, name).map(total ->
                                        hairDresserList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(hairDresserList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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
            @QueryParam("name") @DefaultValue("") String name
    ) {
        // Vérifier si la direction est valide
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.valueOf(direction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valeur invalide pour 'direction'. Valeurs autorisées: Ascending, Descending")
                            .build()
            );
        }

        return
                Country.getStuntmen(id, page, size, sort, sortDirection, name)
                        .flatMap(stuntmanList ->
                                Country.countStuntmen(id, name).map(total ->
                                        stuntmanList.isEmpty()
                                                ? Response.noContent().header("X-Total-Count", total).build()
                                                : Response.ok(stuntmanList).header("X-Total-Count", total).build()
                                )
                        )
                        .onFailure().recoverWithItem(err ->
                                Response.serverError().entity("Erreur serveur : " + err.getMessage()).build()
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

}
