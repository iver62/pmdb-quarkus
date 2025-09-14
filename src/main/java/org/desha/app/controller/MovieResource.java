package org.desha.app.controller;

import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.domain.record.Repartition;
import org.desha.app.service.MovieService;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Path("/movies")
@ApplicationScoped
@Slf4j
@APIResponses(value = {
        @APIResponse(
                responseCode = "401",
                description = "Utilisateur non authentifié"
        ),
        @APIResponse(
                responseCode = "403",
                description = "Accès interdit"
        ),
        @APIResponse(
                responseCode = "500",
                description = "Erreur interne du serveur"
        )
})
@Tag(name = "Films", description = "Opérations liées aux films")
public class MovieResource {

    private final MovieService movieService;
    private final PersonService personService;

    @Inject
    public MovieResource(MovieService movieService, PersonService personService) {
        this.movieService = movieService;
        this.personService = personService;
    }

    @GET
    @Path("/count")
    @Operation(
            summary = "Compter les films",
            description = "Retourne le nombre de films correspondant aux critères fournis en paramètres de requête."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Nombre total de films correspondant aux critères",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Long.class, examples = "42")
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides"
            )
    })
    public Uni<Response> count(@BeanParam MovieQueryParamsDTO queryParams) {
        return
                movieService.count(CriteriaDTO.build(queryParams))
                        .map(aLong -> Response.ok(aLong).build());
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Récupérer un film par son identifiant",
            description = "Retourne les détails d’un film correspondant à l’identifiant fourni."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Film trouvé",
                    content = @Content(
                            mediaType = "application/json"
//                            schema = @Schema(implementation = MovieDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Identifiant de film invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getMovie(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                ;
    }

    @GET
    @Operation(
            summary = "Récupère une liste de films",
            description = """
                    Retourne une liste paginée de films en fonction des critères de recherche et de tri.
                    Si aucun film ne correspond, une réponse 204 (No Content) est renvoyée."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des films trouvés",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON
//                            schema = @Schema(implementation = MovieDTO.class, type = SchemaType.ARRAY)
                    ),
                    headers = {
                            @Header(
                                    name = CustomHttpHeaders.X_TOTAL_COUNT,
                                    description = "Nombre total d’éléments correspondant à la recherche",
                                    schema = @Schema(type = SchemaType.INTEGER, examples = "124")
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun film trouvé"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de recherche invalides"
            )
    })
    public Uni<Response> getMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                movieService.getMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(movieList ->
                                movieService.count(criteriaDTO)
                                        .map(total ->
                                                movieList.isEmpty()
                                                        ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                        : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                        )
                        )
                ;
    }

    @GET
    @Path("/all")
    public Uni<Response> getAllMovies(@BeanParam MovieQueryParamsDTO queryParams) {
        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Movie.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Movie.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                movieService.getMovies(finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .flatMap(movieList ->
                                movieService.count(criteriaDTO).map(total ->
                                        movieList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(movieList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/countries")
    @Operation(
            summary = "Récupère les pays présents dans les films",
            description = """
                    Retourne une liste paginée des pays associés à au moins un film.
                    Permet le tri, la recherche textuelle et la sélection par langue."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des pays trouvés",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CountryDTO.class, type = SchemaType.ARRAY)
                    ),
                    headers = {
                            @Header(
                                    name = CustomHttpHeaders.X_TOTAL_COUNT,
                                    description = "Nombre total de pays correspondant à la recherche",
                                    schema = @Schema(type = SchemaType.INTEGER, examples = "57")
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucun pays trouvé"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de recherche invalides (tri, langue, etc.)"
            )
    })
    public Uni<Response> getCountriesInMovies(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Country.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Country.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();
        String finalLang = queryParams.validateLang();

        return
                movieService.getCountriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term, finalLang)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(countryList ->
                                movieService.countCountriesInMovies(term, finalLang).map(total ->
                                        countryList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(countryList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/categories")
    @Operation(
            summary = "Récupère les catégories présentes dans les films",
            description = """
                    Retourne une liste paginée des catégories associées à au moins un film.
                    Permet le tri et la recherche textuelle."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des catégories trouvées",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CategoryDTO.class, type = SchemaType.ARRAY)
                    ),
                    headers = {
                            @Header(
                                    name = CustomHttpHeaders.X_TOTAL_COUNT,
                                    description = "Nombre total de catégories correspondant à la recherche",
                                    schema = @Schema(type = SchemaType.INTEGER, examples = "15")
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune catégorie trouvée"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de recherche invalides (tri, etc.)"
            )
    })
    public Uni<Response> getCategoriesInMovies(@BeanParam QueryParamsDTO queryParams) {
        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Category.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Category.ALLOWED_SORT_FIELDS);
        String term = queryParams.getTerm();

        return
                movieService.getCategoriesInMovies(Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), term)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(categoryDTOList ->
                                movieService.countCategoriesInMovies(term).map(total ->
                                        categoryDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(categoryDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/title/{title}")
    public Uni<Response> getMoviesByTitle(@RestPath String title) {
        if (Objects.isNull(title)) {
            throw new BadRequestException("Le titre ne peut pas être nul");
        }

        return
                movieService.getByTitle(title)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieList ->
                                movieList.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieList).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/persons")
    @Operation(
            summary = "Récupère les personnes associées à un film",
            description = """
                    Retourne une liste paginée des personnes (acteurs, réalisateurs, etc.) associées à un film donné.
                    Permet le tri et la recherche avancée selon les critères fournis."""
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des personnes trouvées",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON
//                            schema = @Schema(implementation = PersonDTO.class, type = SchemaType.ARRAY)
                    ),
                    headers = {
                            @Header(
                                    name = CustomHttpHeaders.X_TOTAL_COUNT,
                                    description = "Nombre total de personnes correspondant à la recherche",
                                    schema = @Schema(type = SchemaType.INTEGER, examples = "42")
                            )
                    }
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune personne trouvée pour ce film"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Paramètres de recherche invalides (tri, dates, etc.)"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getPersonsByMovie(@RestPath @NotNull Long id, @BeanParam PersonQueryParamsDTO queryParams) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        queryParams.isInvalidDateRange(); // Vérification de la cohérence des dates

        String finalSort = Optional.ofNullable(queryParams.getSort()).orElse(Person.DEFAULT_SORT);
        queryParams.validateSortField(finalSort, Person.ALLOWED_SORT_FIELDS);

        CriteriaDTO criteriaDTO = CriteriaDTO.build(queryParams);

        return
                movieService.getPersonsByMovie(id, Page.of(queryParams.getPageIndex(), queryParams.getSize()), finalSort, queryParams.validateSortDirection(), criteriaDTO)
                        .onItem().ifNull().continueWith(List::of)
                        .flatMap(personDTOList ->
                                movieService.countPersonsByMovie(id, criteriaDTO).map(total ->
                                        personDTOList.isEmpty()
                                                ? Response.noContent().header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                                : Response.ok(personDTOList).header(CustomHttpHeaders.X_TOTAL_COUNT, total).build()
                                )
                        )
                ;
    }

    @GET
    @Path("/{id}/technical-team")
    @Operation(
            summary = "Récupère l'équipe technique d'un film",
            description = "Retourne les techniciens associés à un film donné (réalisateurs, producteurs, assistants, etc.)"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Équipe technique trouvée",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TechnicalTeamDTO.class)
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "Aucune donnée technique trouvée pour ce film"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "ID de film invalide"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Film introuvable"
            )
    })
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getTechnicalTeam(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getTechnicalTeam(id)
                        .map(technicalTeam -> Response.ok(technicalTeam).build())
                ;
    }

    @GET
    @Path("/{id}/actors")
    @Operation(
            summary = "Récupère les acteurs d'un film",
            description = "Retourne la liste des acteurs associés à un film donné."
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des acteurs du film",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PersonDTO.class)
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun acteur trouvé pour ce film"
    )
    @APIResponse(
            responseCode = "400",
            description = "ID de film invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getActorsByMovie(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getActorsByMovie(id)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActors ->
                                movieActors.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActors).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/categories")
    @Operation(
            summary = "Récupère les catégories d'un film",
            description = """
                    Renvoie l'ensemble des catégories associées à un film spécifique.
                    Si aucune catégorie n'est trouvée, retourne un code HTTP 204 No Content."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des catégories récupérée avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CategoryDTO.class, type = SchemaType.ARRAY))
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune catégorie trouvée pour ce film"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film non trouvé"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getCategories(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCategoriesByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/countries")
    @Operation(
            summary = "Récupère les pays associés à un film",
            description = """
                    Renvoie l'ensemble des pays liés à un film spécifique.
                    Si aucun pays n'est trouvé, retourne un code HTTP 204 No Content."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des pays récupérée avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CountryDTO.class, type = SchemaType.ARRAY))
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun pays trouvé pour ce film"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film non trouvé"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getCountries(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCountriesByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOS ->
                                countryDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOS).build()
                        )
                ;
    }

    @GET
    @Path("/{id}/ceremonies-awards")
    @Operation(
            summary = "Récupère les récompenses associées à un film",
            description = """
                    Renvoie l'ensemble des récompenses liées à un film spécifique.
                    Si aucune cérémonie ou récompense n'est trouvée, retourne un code HTTP 204 No Content."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Liste des récompenses récupérées avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CeremonyAwardsDTO.class, type = SchemaType.ARRAY))
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune récompense trouvée pour ce film"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film non trouvé"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> getCeremoniesAwards(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getCeremoniesAwardsByMovie(id)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(ceremonyAwardsDTOS ->
                                ceremonyAwardsDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTOS).build()
                        )
                ;
    }

    @GET
    @Path("/creation-date-evolution")
    @Operation(
            summary = "Évolution du nombre de films créés dans le temps",
            description = """
                    Renvoie une liste représentant l'évolution du nombre de films créés par date ou période.
                    Chaque élément contient une date/période et le nombre de films créés à cette date."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Répartition récupérée avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Repartition.class, type = SchemaType.ARRAY))
    )
    public Uni<Response> getMoviesCreationDateEvolution() {
        return
                movieService.getMoviesCreationDateEvolution()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/creation-date-repartition")
    @Operation(
            summary = "Répartition des films par date de création",
            description = """
                    Renvoie une liste représentant la répartition du nombre de films selon leur date de création.
                    Chaque élément contient une date et le nombre de films créés à cette date."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Répartition récupérée avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Repartition.class, type = SchemaType.ARRAY))
    )
    public Uni<Response> getMoviesRepartitionByCreationDate() {
        return
                movieService.getMoviesCreationDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/decade-repartition")
    @Operation(
            summary = "Répartition des films par décennie de sortie",
            description = """
                    Renvoie une liste représentant la répartition du nombre de films selon leur décennie de sortie.
                    Chaque élément contient une décennie et le nombre de films sortis durant cette période."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Répartition récupérée avec succès",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Repartition.class, type = SchemaType.ARRAY))
    )
    public Uni<Response> getMoviesRepartitionByDecade() {
        return
                movieService.getMoviesReleaseDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/posters/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
    @Operation(
            summary = "Récupère l'affiche d'un film",
            description = """
                    Renvoie l'image de l'affiche du film correspondant au nom de fichier fourni.
                    Le type MIME est détecté automatiquement."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Affiche récupérée avec succès",
            content = @Content(
                    mediaType = "image/*",
                    schema = @Schema(type = SchemaType.STRING, format = "binary")
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Nom de fichier invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Affiche introuvable"
    )
    public Uni<Response> getPoster(String fileName) {
        if (Objects.isNull(fileName) || fileName.isEmpty() || Objects.equals("undefined", fileName)) {
            log.warn("Invalid file request: {}", fileName);
            throw new BadRequestException("Invalid file name");
        }

        return
                movieService.getPoster(fileName)
                        .onItem().ifNotNull().transform(
                                file -> {
                                    try {
                                        byte[] fileBytes = Files.readAllBytes(file.toPath());
                                        String mimeType = Files.probeContentType(file.toPath()); // Détecte automatiquement le type MIME

                                        log.info("Serving poster: {}", fileName);
                                        return Response.ok(fileBytes).type(mimeType).build();
                                    } catch (IOException e) {
                                        log.error("Error loading poster {}: {}", fileName, e.getMessage());
                                        return Response.serverError().entity("Erreur lors du chargement de l'affiche").build();
                                    }
                                }
                        )
                        .onItem().ifNull().continueWith(() -> {
                            log.warn("Poster not found: {}", fileName);
                            return Response.status(Response.Status.NOT_FOUND).entity("Affiche introuvable").build();
                        })
                ;
    }

    @POST
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Crée un nouveau film",
            description = "Permet de créer un film en envoyant un fichier pour l'affiche et les informations du film en JSON."
    )
    @APIResponse(
            responseCode = "201",
            description = "Film créé avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = MovieDTO.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple informations manquantes ou ID défini dans MovieDTO"
    )
    public Uni<Response> create(
            @RequestBody(
                    description = "Fichier de l'affiche du film à uploader",
                    required = false,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA,
                            schema = @Schema(type = SchemaType.STRING, format = "binary")
                    )
            )
            @RestForm("file") FileUpload file,

            @RequestBody(
                    description = "Informations du film au format JSON",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieDTO.class)
                    )
            )
            @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO
    ) {
        if (Objects.isNull(movieDTO)) {
            throw new BadRequestException("Aucune information sur le film n’a été fournie dans la requête");
        }

        if (Objects.nonNull(movieDTO.getId())) {
            throw new BadRequestException("L’identifiant a été défini de manière incorrecte dans la requête");
        }

        if (StringUtils.isBlank(movieDTO.getTitle())) {
            throw new BadRequestException("Le titre du film n’a pas été fourni dans la requête");
        }

        return
                movieService.saveMovie(file, movieDTO)
                        .map(movie -> Response.status(CREATED).entity(movie).build())
                ;
    }

    @PUT
    @Path("/{id}")
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour un film existant",
            description = """
                    Permet de mettre à jour les informations d'un film en envoyant un fichier pour l'affiche et les informations du film en JSON.
                    L'identifiant du film dans l'URL doit correspondre à celui dans le DTO."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Film mis à jour avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = MovieDTO.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple informations manquantes, titre manquant ou DTO nul"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @APIResponse(
            responseCode = "422",
            description = "L'identifiant du film dans le DTO ne correspond pas à celui de l'URL"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> update(
            @RestPath @NotNull Long id,

            @RequestBody(
                    description = "Fichier de l'affiche du film à uploader",
                    required = false,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA,
                            schema = @Schema(type = SchemaType.STRING, format = "binary")
                    )
            )
            @RestForm("file") FileUpload file,

            @RequestBody(
                    description = "Informations du film au format JSON",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MovieDTO.class)
                    )
            )
            @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO
    ) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieDTO)) {
            throw new BadRequestException("Aucune information sur le film n’a été fournie dans la requête");
        }

        if (StringUtils.isBlank(movieDTO.getTitle())) {
            throw new BadRequestException("Le titre du film n’a pas été fourni dans la requête");
        }

        if (!Objects.equals(id, movieDTO.getId())) {
            throw new WebApplicationException("L'identifiant du film ne correspond pas à celui de la requête", 422);
        }

        return movieService.updateMovie(id, file, movieDTO)
                .map(entity -> Response.ok(entity).build());
    }

    @PUT
    @Path("/{id}/cast")
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour le casting d'un film",
            description = """
                    Permet de sauvegarder ou mettre à jour la liste des acteurs d'un film.
                    Chaque acteur est représenté par un MovieActorDTO contenant son rôle et son rang.
                    Si aucun acteur n'est fourni, la réponse sera vide."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Casting mis à jour avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = MovieActorDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun acteur n'a été ajouté ou mis à jour"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple liste des acteurs nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> saveCast(@RestPath @NotNull Long id, List<MovieActorDTO> movieActorsList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieActorsList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle");
        }

        return
                movieService.saveCast(
                                id,
                                movieActorsList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, StringUtils.defaultString(dto.getRole()).trim(), dto.getRank()))
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOS ->
                                movieActorDTOS.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOS).build()
                        )
                ;
    }

    @PUT
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour les catégories d'un film",
            description = """
                    Permet de sauvegarder ou mettre à jour la liste des catégories d'un film.
                    Chaque catégorie est représentée par un CategoryDTO.
                    Si aucune catégorie n'est fournie, la réponse sera vide."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Catégories mises à jour avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune catégorie n'a été ajoutée ou mise à jour"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple liste des catégories nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> saveCategories(@RestPath @NotNull Long id, Set<CategoryDTO> categoryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle");
        }

        return
                movieService.saveCategories(id, categoryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    @PUT
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Met à jour les pays d'un film",
            description = """
                    Permet de sauvegarder ou mettre à jour la liste des pays associés à un film. Chaque pays est représenté par un CountryDTO.
                    Si aucune donnée n'est fournie, la réponse sera vide."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Pays mis à jour avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun pays n'a été ajouté ou mis à jour"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple liste des pays nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> saveCountries(@RestPath @NotNull Long id, Set<CountryDTO> countryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle");
        }

        return
                movieService.saveCountries(id, countryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                ;
    }

    @PUT
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajoute ou met à jour les récompenses d'un film",
            description = """
                    Permet de sauvegarder ou mettre à jour une cérémonie de récompenses associée à un film.
                    La cérémonie est représentée par un objet CeremonyAwardsDTO."""
    )
    @APIResponse(
            responseCode = "200",
            description = "Récompense du film ajoutée ou mise à jour avec succès",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CeremonyAwardsDTO.class)
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple CeremonyAwardsDTO null"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> saveCeremonyAwards(@RestPath @NotNull Long id, CeremonyAwardsDTO ceremonyAwardsDTO) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(ceremonyAwardsDTO)) {
            throw new BadRequestException("La liste des récompenses ne peut pas être nulle");
        }

        return
                movieService.saveCeremonyAwards(id, ceremonyAwardsDTO)
                        .map(dto -> Response.ok(dto).build())
                ;
    }

    @PATCH
    @Path("/{id}/roles")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajoute des acteurs à un film",
            description = "Permet d'ajouter une liste d'acteurs à un film existant. Chaque acteur est représenté par un objet MovieActorDTO."
    )
    @APIResponse(
            responseCode = "200",
            description = "Acteurs ajoutés avec succès au film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = MovieActorDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun acteur n'a été ajouté"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple la liste des acteurs est nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> addMovieActors(@RestPath @NotNull Long id, List<MovieActorDTO> movieActorDTOList) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(movieActorDTOList)) {
            throw new BadRequestException("La liste des acteurs ne peut pas être nulle");
        }

        return
                movieService.addMovieActors(
                                id,
                                movieActorDTOList,
                                (movie, dto) -> personService.prepareAndPersistPerson(dto.getPerson(), PersonType.ACTOR)
                                        .map(person -> MovieActor.build(movie, person, dto.getRole(), dto.getRank()))
                        )
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                ;
    }

    @PATCH
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajoute des catégories à un film",
            description = "Permet d'ajouter une liste de catégories à un film existant. Chaque catégorie est représentée par un objet CategoryDTO."
    )
    @APIResponse(
            responseCode = "200",
            description = "Catégories ajoutées avec succès au film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune catégorie n'a été ajoutée"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple la liste des catégories est nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> addCategories(@RestPath @NotNull Long id, Set<CategoryDTO> categoryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(categoryDTOS)) {
            throw new BadRequestException("La liste des catégories ne peut pas être nulle");
        }

        return
                movieService.addCategories(id, categoryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    @PATCH
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Ajoute des pays à un film",
            description = "Permet d'ajouter une liste de pays à un film existant. Chaque pays est représenté par un objet CountryDTO."
    )
    @APIResponse(
            responseCode = "200",
            description = "Pays ajoutés avec succès au film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun pays n'a été ajouté"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple la liste des pays est nulle"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> addCountries(@RestPath @NotNull Long id, Set<CountryDTO> countryDTOS) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        if (Objects.isNull(countryDTOS)) {
            throw new BadRequestException("La liste des pays ne peut pas être nulle");
        }

        return
                movieService.addCountries(id, countryDTOS)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                ;
    }

    @PATCH
    @Path("/{movieId}/roles/{personId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime un acteur d'un film",
            description = "Permet de retirer un acteur d'un film existant en utilisant l'identifiant du film et l'identifiant de l'acteur."
    )
    @APIResponse(
            responseCode = "200",
            description = "Acteur supprimé avec succès du film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = MovieActorDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun acteur n'a été supprimé"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple identifiants invalides"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film ou acteur introuvable pour les identifiants fournis"
    )
    @Parameter(name = "movieId", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    @Parameter(name = "personId", description = "Identifiant unique de la personne", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> removeMovieActor(@RestPath @NotNull Long movieId, @RestPath @NotNull Long personId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(personId, Messages.INVALID_PERSON_ID);

        return
                movieService.removeMovieActor(movieId, personId)
                        .onItem().ifNull().continueWith(List::of)
                        .map(movieActorDTOs ->
                                movieActorDTOs.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(movieActorDTOs).build()
                        )
                ;
    }

    @PATCH
    @Path("/{movieId}/categories/{categoryId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime une catégorie d'un film",
            description = "Permet de retirer une catégorie d'un film existant en utilisant l'identifiant du film et l'identifiant de la catégorie."
    )
    @APIResponse(
            responseCode = "200",
            description = "Catégorie supprimée avec succès du film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune catégorie n'a été supprimée"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple identifiants invalides"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film ou catégorie introuvable pour les identifiants fournis"
    )
    @Parameter(name = "movieId", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    @Parameter(name = "categoryId", description = "Identifiant unique de la catégorie", required = true, example = "1", in = ParameterIn.PATH)
    public Uni<Response> removeCategory(@RestPath @NotNull Long movieId, @RestPath @NotNull Long categoryId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(categoryId, Messages.INVALID_CATEGORY_ID);

        return
                movieService.removeCategory(movieId, categoryId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(categoryDTOSet ->
                                categoryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(categoryDTOSet).build()
                        )
                ;
    }

    @PATCH
    @Path("/{movieId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime un pays d'un film",
            description = "Permet de retirer un pays associé à un film existant en utilisant l'identifiant du film et l'identifiant du pays."
    )
    @APIResponse(
            responseCode = "200",
            description = "Pays supprimé avec succès du film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucun pays n'a été supprimé"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple identifiants invalides"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film ou pays introuvable pour les identifiants fournis"
    )
    @Parameter(name = "movieId", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    @Parameter(name = "countryId", description = "Identifiant unique du pays", required = true, example = "1", in = ParameterIn.PATH)
    public Uni<Response> removeCountry(@RestPath @NotNull Long movieId, @RestPath @NotNull Long countryId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(countryId, Messages.INVALID_COUNTRY_ID);

        return
                movieService.removeCountry(movieId, countryId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(countryDTOSet ->
                                countryDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(countryDTOSet).build()
                        )
                ;
    }

    @PATCH
    @Path("/{movieId}/ceremonies-awards/{ceremonyAwardsId}")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime une récompense d'une cérémonie d'un film",
            description = "Permet de retirer une récompense associée à un film pour une cérémonie donnée en utilisant l'identifiant du film et l'identifiant de la récompense."
    )
    @APIResponse(
            responseCode = "200",
            description = "Récompense supprimée avec succès du film",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON
//                    array = @ArraySchema(schema = @Schema(implementation = CeremonyAwardsDTO.class))
            )
    )
    @APIResponse(
            responseCode = "204",
            description = "Aucune récompense n'a été supprimée"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, par exemple identifiants invalides"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film ou récompense introuvable pour les identifiants fournis"
    )
    @Parameter(name = "movieId", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    @Parameter(name = "ceremonyAwardsId", description = "Identifiant unique de la cérémonie", required = true, example = "1", in = ParameterIn.PATH)
    public Uni<Response> removeCeremonyAwards(@RestPath @NotNull Long movieId, @RestPath @NotNull Long ceremonyAwardsId) {
        ValidationUtils.validateIdOrThrow(movieId, Messages.INVALID_MOVIE_ID);
        ValidationUtils.validateIdOrThrow(ceremonyAwardsId, Messages.INVALID_CEREMONY_AWARDS_ID);

        return
                movieService.removeCeremonyAwards(movieId, ceremonyAwardsId)
                        .onItem().ifNull().continueWith(Set::of)
                        .map(ceremonyAwardsDTOSet ->
                                ceremonyAwardsDTOSet.isEmpty()
                                        ? Response.noContent().build()
                                        : Response.ok(ceremonyAwardsDTOSet).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Operation(
            summary = "Supprime un film",
            description = "Permet de supprimer un film existant en utilisant son identifiant unique. Supprime également l'affiche associée et met à jour les statistiques liées aux films."
    )
    @APIResponse(
            responseCode = "204",
            description = "Film supprimé avec succès, aucun contenu retourné"
    )
    @APIResponse(
            responseCode = "400",
            description = "Requête invalide, identifiant du film manquant ou invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> delete(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.deleteMovie(id)
                        .map(deleted ->
                                Boolean.TRUE.equals(deleted)
                                        ? Response.noContent().build()
                                        : Response.status(Response.Status.NOT_FOUND).build()
                        )
                ;
    }

    @DELETE
    @Path("/{id}/actors")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime tous les acteurs d'un film",
            description = "Permet de supprimer l'ensemble des acteurs associés à un film donné en utilisant son identifiant unique."
    )
    @APIResponse(
            responseCode = "204",
            description = "Acteurs supprimés avec succès, aucun contenu retourné"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film manquant ou invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> deleteActors(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearActors(id)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    @DELETE
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime toutes les catégories d'un film",
            description = "Permet de supprimer l'ensemble des catégories associées à un film donné en utilisant son identifiant unique."
    )
    @APIResponse(
            responseCode = "204",
            description = "Catégories supprimées avec succès, aucun contenu retourné"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film manquant ou invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> deleteCategories(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCategories(id)
                .map(deleted -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime tous les pays d'un film",
            description = "Permet de supprimer l'ensemble des pays associés à un film donné en utilisant son identifiant unique."
    )
    @APIResponse(
            responseCode = "204",
            description = "Pays supprimés avec succès, aucun contenu retourné"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant du film manquant ou invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable pour l'identifiant fourni"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> deleteCountries(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCountries(id).map(deleted -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    @Operation(
            summary = "Supprime toutes les récompenses associées à un film",
            description = "Vide la collection des récompenses pour le film donné. Renvoie HTTP 204 si l'opération réussit."
    )
    @APIResponse(
            responseCode = "204",
            description = "Récompenses supprimés avec succès"
    )
    @APIResponse(
            responseCode = "400",
            description = "Identifiant de film invalide"
    )
    @APIResponse(
            responseCode = "404",
            description = "Film introuvable"
    )
    @Parameter(name = "id", description = "Identifiant unique du film", required = true, example = "123", in = ParameterIn.PATH)
    public Uni<Response> deleteCeremoniesAwards(@RestPath @NotNull Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCeremoniesAwards(id).map(deleted -> Response.noContent().build());
    }

}