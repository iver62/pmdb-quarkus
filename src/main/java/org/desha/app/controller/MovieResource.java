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
import org.apache.commons.lang3.StringUtils;
import org.desha.app.config.CustomHttpHeaders;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.*;
import org.desha.app.domain.enums.PersonType;
import org.desha.app.service.MovieService;
import org.desha.app.service.PersonService;
import org.desha.app.utils.Messages;
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
@Tag(name = "Films", description = "Opérations liées aux films")
public class MovieResource {

    private final MovieService movieService;
    private final PersonService personService;

    @Inject
    public MovieResource(MovieService movieService, PersonService personService) {
        this.movieService = movieService;
        this.personService = personService;
    }

    /**
     * Récupère le nombre total de films correspondant aux critères de recherche spécifiés.
     * <p>
     * Cette méthode effectue une requête pour compter le nombre de films qui correspondent aux critères fournis dans l'objet
     * {@link MovieQueryParamsDTO}. Si des critères sont spécifiés, elle renvoie une réponse HTTP avec le statut 200 (OK)
     * contenant le nombre total de films correspondants. Si aucun film ne correspond aux critères, la méthode renverra également
     * une réponse HTTP 200 avec la valeur 0.
     *
     * @param queryParams Les paramètres de requête encapsulés dans un objet {@link MovieQueryParamsDTO}, qui contiennent
     *                    les critères de recherche pour filtrer les films.
     * @return Un {@link Uni} contenant une réponse HTTP 200 (OK) avec le nombre total de films correspondant aux critères.
     * Si aucun film ne correspond, la réponse contiendra 0.
     */
    @GET
    @Path("/count")
    public Uni<Response> count(@BeanParam MovieQueryParamsDTO queryParams) {
        return
                movieService.count(CriteriaDTO.build(queryParams))
                        .map(aLong -> Response.ok(aLong).build());
    }

    /**
     * Récupère un film par son identifiant.
     * <p>
     * Cette méthode permet de récupérer les détails d'un film en fonction de son identifiant unique. Si le film existe,
     * elle renvoie une réponse HTTP avec le statut 200 (OK) contenant les informations du film. Si une erreur se produit
     * lors de la récupération du film (par exemple, film non trouvé ou problème interne), la méthode renvoie une réponse
     * HTTP avec le statut 500 (Internal Server Error).
     *
     * @param id L'identifiant du film à récupérer.
     * @return Un {@link Uni} contenant une réponse HTTP. Si le film est trouvé, la réponse contient le film avec le statut 200.
     * Si le film n'est pas trouvé ou si une erreur se produit, la réponse contient un statut 500 (Internal Server Error).
     */
    @GET
    @Path("/{id}")
    public Uni<Response> getMovie(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getById(id)
                        .onItem().ifNotNull().transform(movie -> Response.ok(movie).build())
                ;
    }

    @GET
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
    public Uni<Response> getPersonsByMovie(@RestPath Long id, @BeanParam PersonQueryParamsDTO queryParams) {
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
    public Uni<Response> getTechnicalTeam(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.getTechnicalTeam(id)
                        .map(technicalTeam -> Response.ok(technicalTeam).build())
                ;
    }

    @GET
    @Path("/{id}/actors")
    public Uni<Response> getActorsByMovie(@RestPath Long id) {
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

    /**
     * Récupère les catégories associées à un film donné.
     *
     * @param id L'ID du film.
     * @return Une réponse HTTP :
     * - 200 (OK) avec la liste des catégories si elle n'est pas vide.
     * - 204 si la liste des catégories est vide.
     */
    @GET
    @Path("/{id}/categories")
    public Uni<Response> getCategories(@RestPath Long id) {
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

    /**
     * Récupère les pays associés à un film donné.
     *
     * @param id L'ID du film.
     * @return Une réponse HTTP :
     * - 200 (OK) avec la liste des pays si elle contient des données.
     * - 204 si la liste des pays est vide.
     */
    @GET
    @Path("/{id}/countries")
    public Uni<Response> getCountries(@RestPath Long id) {
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

    /**
     * Récupère les récompenses associées à un film par son identifiant.
     * <p>
     * Cette méthode récupère la liste des récompenses associées à un film donné, en fonction de l'identifiant du film
     * fourni dans l'URL. Si la liste des récompenses est vide, une réponse avec le statut HTTP 204 (No Content) est renvoyée.
     * Si des récompenses sont trouvées, une réponse avec le statut HTTP 200 (OK) contenant la liste des récompenses est renvoyée.
     *
     * @param id L'identifiant du film pour lequel les récompenses doivent être récupérées.
     * @return Un {@link Uni} contenant une réponse HTTP. Si aucune récompense n'est trouvée, une réponse avec le statut HTTP 204 est renvoyée.
     * Sinon, une réponse avec le statut HTTP 200 et la liste des récompenses est renvoyée.
     */
    @GET
    @Path("/{id}/ceremonies-awards")
    public Uni<Response> getCeremoniesAwards(@RestPath Long id) {
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
    public Uni<Response> getMoviesCreationDateEvolution() {
        return
                movieService.getMoviesCreationDateEvolution()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/creation-date-repartition")
    public Uni<Response> getMoviesRepartitionByCreationDate() {
        return
                movieService.getMoviesCreationDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/decade-repartition")
    public Uni<Response> getMoviesRepartitionByDecade() {
        return
                movieService.getMoviesReleaseDateRepartition()
                        .map(countDTOS -> Response.ok(countDTOS).build())
                ;
    }

    @GET
    @Path("/posters/{fileName}")
    @Produces({"image/jpg", "image/jpeg", "image/png"})
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
    public Uni<Response> create(@RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO) {
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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> update(@RestPath Long id, @RestForm("file") FileUpload file, @RestForm @PartType(MediaType.APPLICATION_JSON) @Valid MovieDTO movieDTO) {
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
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCast(@RestPath Long id, List<MovieActorDTO> movieActorsList) {
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

    /**
     * Met à jour les catégories associées à un film donné.
     * <p>
     * Cette méthode permet d'ajouter ou de mettre à jour les catégories d'un film
     * en fonction des identifiants fournis.
     *
     * @param id           L'identifiant du film dont les catégories doivent être mises à jour.
     * @param categoryDTOS Un ensemble de {@link CategoryDTO} représentant les catégories à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des catégories mises à jour.
     * - `204 No Content` si aucune catégorie n'est associée.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des catégories est null.
     */
    @PUT
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCategories(@RestPath Long id, Set<CategoryDTO> categoryDTOS) {
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

    /**
     * Met à jour les pays associés à un film donné.
     * <p>
     * Cette méthode permet de mettre à jour les pays d'un film en fonction des identifiants fournis.
     *
     * @param id          L'identifiant du film dont les pays doivent être mis à jour.
     * @param countryDTOS Un ensemble de {@link CountryDTO} représentant les pays à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des pays mise à jour.
     * - `204 No Content` si aucun pays n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des pays est null.
     */
    @PUT
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCountries(@RestPath Long id, Set<CountryDTO> countryDTOS) {
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

    /**
     * Met à jour les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de mettre à jour les récompenses d'un film en fonction des identifiants fournis.
     *
     * @param id                L'identifiant du film dont les récompenses doivent être mis à jour.
     * @param ceremonyAwardsDTO Un {@link CeremonyAwardsDTO} représentant les récompenses à associer.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - `200 OK` avec la liste des récompenses mise à jour.
     * - `204 No Content` si aucune récompense n'est associé.
     * - `500 Server Error` si la mise à jour échoue.
     * @throws BadRequestException si la liste des récompenses est null.
     */
    @PUT
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> saveCeremonyAwards(@RestPath Long id, CeremonyAwardsDTO ceremonyAwardsDTO) {
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
    public Uni<Response> addMovieActors(@RestPath Long id, List<MovieActorDTO> movieActorDTOList) {
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

    /**
     * Ajoute un ensemble de catégories à un film spécifique.
     *
     * @param id           L'identifiant du film auquel les catégories doivent être ajoutées.
     * @param categoryDTOS L'ensemble des catégories à ajouter, représentées sous forme de DTO.
     * @return Une réponse HTTP contenant le film mis à jour avec ses nouvelles catégories :
     * - 200 OK si l'opération réussit et retourne l'entité mise à jour.
     * - 500 Server Error si l'ajout échoue.
     */
    @PATCH
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCategories(@RestPath Long id, Set<CategoryDTO> categoryDTOS) {
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

    /**
     * Ajoute une liste de pays associés à un film.
     *
     * @param id          L'identifiant du film auquel les pays doivent être ajoutés.
     * @param countryDTOS Un ensemble d'objets {@link CountryDTO} représentant les pays à associer au film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si l'ajout est réussi.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> addCountries(@RestPath Long id, Set<CountryDTO> countryDTOS) {
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

    /**
     * Supprime un acteur associé à un film donné.
     *
     * @param movieId  L'identifiant du film dont l'acteur doit être supprimé.
     * @param personId L'identifiant de l'association acteur-film à supprimer.
     * @return Une {@link Uni} contenant une {@link Response} :
     * - 200 OK avec la liste mise à jour des acteurs si la suppression est réussie.
     * - 500 Server Error si la suppression échoue.
     */
    @PATCH
    @Path("/{movieId}/roles/{personId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeMovieActor(@RestPath Long movieId, @RestPath Long personId) {
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

    /**
     * Supprime une catégorie spécifique d'un film donné.
     *
     * @param movieId    L'identifiant du film dont la catégorie doit être supprimée.
     * @param categoryId L'identifiant de la catégorie à supprimer.
     * @return Une réponse HTTP contenant le film mis à jour après la suppression de la catégorie :
     * - 200 OK si la suppression est réussie et retourne l'entité mise à jour.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{movieId}/categories/{categoryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCategory(@RestPath Long movieId, @RestPath Long categoryId) {
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

    /**
     * Supprime l'association d'un pays avec un film donné.
     *
     * @param movieId   L'identifiant du film concerné.
     * @param countryId L'identifiant du pays à dissocier du film.
     * @return Un {@link Uni} contenant une réponse HTTP :
     * - 200 OK avec l'entité mise à jour si la suppression est réussie.
     * - 500 Internal Server Error en cas d'erreur interne.
     */
    @PATCH
    @Path("/{movieId}/countries/{countryId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCountry(@RestPath Long movieId, @RestPath Long countryId) {
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

    /**
     * Supprime toutes les récompenses associées à un film donné.
     * <p>
     * Cette méthode permet de supprimer toutes les récompenses associées à un film en appelant la méthode
     * {@link MovieService#removeCeremonyAwards(Long, Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param movieId L'identifiant du film dont les récompenses doivent être supprimées.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les récompenses ont été supprimées avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des récompenses.
     */
    @PATCH
    @Path("/{movieId}/ceremonies-awards/{ceremonyAwardsId}")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> removeCeremonyAwards(@RestPath Long movieId, @RestPath Long ceremonyAwardsId) {
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
    public Uni<Response> delete(@RestPath Long id) {
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
    public Uni<Response> deleteActors(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return
                movieService.clearActors(id)
                        .map(deleted -> Response.noContent().build())
                ;
    }

    /**
     * Supprime toutes les catégories associées à un film donné.
     * <p>
     * Cette méthode permet de supprimer toutes les catégories associées à un film en appelant la méthode
     * {@link MovieService#clearCategories(Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les catégories doivent être supprimées.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les catégories ont été supprimées avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des catégories.
     */
    @DELETE
    @Path("/{id}/categories")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCategories(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCategories(id)
                .map(deleted -> Response.noContent().build());
    }

    /**
     * Supprime tous les pays associés à un film donné.
     * <p>
     * Cette méthode permet de supprimer tous les pays associés à un film en appelant la méthode
     * {@link MovieService#clearCountries(Long)}. Elle répond avec un code HTTP 200 si la suppression a réussi.
     *
     * @param id L'identifiant du film dont les pays doivent être supprimés.
     * @return Un {@link Uni} contenant la réponse HTTP avec un code 200 si les pays ont été supprimés avec succès.
     * @throws WebApplicationException Si une erreur survient lors de la suppression des pays.
     */
    @DELETE
    @Path("/{id}/countries")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCountries(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCountries(id).map(deleted -> Response.noContent().build());
    }

    @DELETE
    @Path("/{id}/ceremonies-awards")
    @RolesAllowed({"user", "admin"})
    public Uni<Response> deleteCeremoniesAwards(@RestPath Long id) {
        ValidationUtils.validateIdOrThrow(id, Messages.INVALID_MOVIE_ID);

        return movieService.clearCeremoniesAwards(id).map(deleted -> Response.noContent().build());
    }

}