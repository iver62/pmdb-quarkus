package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.desha.app.data.Factory;
import org.desha.app.data.Utils;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.record.Repartition;
import org.desha.app.service.MovieService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(MovieResource.class)
@TestSecurity(user = "pierrickd", roles = {"admin", "user"})
class MovieEndPointTest {

    @InjectMock
    MovieService movieService;

    @Test
    void shouldReturnNumberSuccessfully() {
        when(movieService.count(any()))
                .thenReturn(Uni.createFrom().item(55L));

        given()
                .when()
                .get("/count")
                .then()
                .statusCode(200)
                .body(is("55"))
        ;
    }

    @Test
    void shouldReturnMovieSuccessfully() {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();

        when(movieService.getById(any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTO));

        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockMovieDTO.getId().intValue()),
                        "title", equalTo(mockMovieDTO.getTitle()),
                        "originalTitle", equalTo(mockMovieDTO.getOriginalTitle()),
                        "synopsis", equalTo(mockMovieDTO.getSynopsis()),
                        "releaseDate", equalTo(mockMovieDTO.getReleaseDate().toString()),
                        "runningTime", equalTo(mockMovieDTO.getRunningTime()),
                        "budget.value", equalTo(mockMovieDTO.getBudget().value()),
                        "budget.currency", equalTo(mockMovieDTO.getBudget().currency()),
                        "boxOffice.value", equalTo(mockMovieDTO.getBoxOffice().value()),
                        "boxOffice.currency", equalTo(mockMovieDTO.getBoxOffice().currency()),
                        "posterFileName", equalTo(mockMovieDTO.getPosterFileName()),
                        "creationDate", equalTo(mockMovieDTO.getCreationDate().format(Utils.dateTimeFormatter)),
                        "lastUpdate", equalTo(mockMovieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                )
        ;
    }

    @Test
    void shouldReturnMoviesByTitleSuccessfully() {
        List<MovieDTO> mockMovieDTOList = Factory.mockMovieDTOList(5);

        when(movieService.getByTitle(any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTOList));

        given()
                .when()
                .get("/title/test")
                .then()
                .statusCode(200)
                .body(
                        "size()", Matchers.is(mockMovieDTOList.size()),
                        "id", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "title", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getTitle)
                                .toArray(String[]::new)),
                        "originalTitle", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getOriginalTitle)
                                .toArray(String[]::new)),
                        "synopsis", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getSynopsis)
                                .toArray(String[]::new)),
                        "releaseDate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getReleaseDate().toString())
                                .toArray(String[]::new)),
                        "runningTime", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getRunningTime)
                                .toArray(Integer[]::new)),
                        "budget.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().value())
                                .toArray(Long[]::new)),
                        "budget.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().currency())
                                .toArray(String[]::new)),
                        "boxOffice.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().value())
                                .toArray(Long[]::new)),
                        "boxOffice.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().currency())
                                .toArray(String[]::new)),
                        "posterFileName", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getPosterFileName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenEmptyMoviesByTitle() {
        when(movieService.getByTitle(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get("/title/test")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnPaginatedMoviesSuccessfully() {
        final int size = 50;
        List<MovieDTO> mockMovieDTOList = Factory.mockMovieDTOList(60);

        List<MovieDTO> mockMovieDTOSubList = mockMovieDTOList.subList(0, size);

        when(movieService.getMovies(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTOSubList));

        when(movieService.count(any()))
                .thenReturn(Uni.createFrom().item((long) mockMovieDTOList.size()));

        given()
                .when()
                .get("?size=50")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "60")
                .body(
                        "size()", Matchers.is(size),
                        "id", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "title", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(MovieDTO::getTitle)
                                .toArray(String[]::new)),
                        "originalTitle", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(MovieDTO::getOriginalTitle)
                                .toArray(String[]::new)),
                        "synopsis", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(MovieDTO::getSynopsis)
                                .toArray(String[]::new)),
                        "releaseDate", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getReleaseDate().toString())
                                .toArray(String[]::new)),
                        "runningTime", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(MovieDTO::getRunningTime)
                                .toArray(Integer[]::new)),
                        "budget.value", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getBudget().value())
                                .toArray(Long[]::new)),
                        "budget.currency", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getBudget().currency())
                                .toArray(String[]::new)),
                        "boxOffice.value", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().value())
                                .toArray(Long[]::new)),
                        "boxOffice.currency", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().currency())
                                .toArray(String[]::new)),
                        "posterFileName", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(MovieDTO::getPosterFileName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.contains(mockMovieDTOSubList.stream()
                                .map(movieDTO -> movieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoMoviesFound() {
        when(movieService.getMovies(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        when(movieService.count(any()))
                .thenReturn(Uni.createFrom().item(0L));

        given()
                .when()
                .get()
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnAllMoviesSuccessfully() {
        List<MovieDTO> mockMovieDTOList = Factory.mockMovieDTOList(60);

        when(movieService.getMovies(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTOList));

        when(movieService.count(any()))
                .thenReturn(Uni.createFrom().item((long) mockMovieDTOList.size()));

        given()
                .when()
                .get("/all")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "60")
                .body(
                        "size()", Matchers.is(mockMovieDTOList.size()),
                        "id", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "title", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getTitle)
                                .toArray(String[]::new)),
                        "originalTitle", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getOriginalTitle)
                                .toArray(String[]::new)),
                        "synopsis", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getSynopsis)
                                .toArray(String[]::new)),
                        "releaseDate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getReleaseDate().toString())
                                .toArray(String[]::new)),
                        "runningTime", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getRunningTime)
                                .toArray(Integer[]::new)),
                        "budget.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().value())
                                .toArray(Long[]::new)),
                        "budget.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().currency())
                                .toArray(String[]::new)),
                        "boxOffice.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().value())
                                .toArray(Long[]::new)),
                        "boxOffice.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().currency())
                                .toArray(String[]::new)),
                        "posterFileName", Matchers.contains(mockMovieDTOList.stream()
                                .map(MovieDTO::getPosterFileName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoMovies() {
        when(movieService.getMovies(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get("/all")
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnCountriesInMoviesSuccessfully() {
        List<CountryDTO> mockCountryDTOList = Factory.mockCountryDTOList(20);

        when(movieService.getCountriesInMovies(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOList));

        when(movieService.countCountriesInMovies(any(), any()))
                .thenReturn(Uni.createFrom().item((long) mockCountryDTOList.size()));

        given()
                .when()
                .get("/countries?lang=fr")
                .then()
                .statusCode(200)
                .header("X-Total-Count", String.valueOf(mockCountryDTOList.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(countryDTO -> countryDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.containsInAnyOrder(mockCountryDTOList.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCountriesInMovies() {
        when(movieService.getCountriesInMovies(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        when(movieService.countCountriesInMovies(any(), any()))
                .thenReturn(Uni.createFrom().item(0L));

        given()
                .when()
                .get("/countries?lang=fr")
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnCategoriesInMoviesSuccessfully() {
        List<CategoryDTO> mockCategoryDTOList = Factory.mockCategoryDTOList(5);

        when(movieService.getCategoriesInMovies(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOList));

        when(movieService.countCategoriesInMovies(any()))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/categories")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "5")
                .body(
                        "id", Matchers.containsInAnyOrder(mockCategoryDTOList.stream()
                                .map(categoryDTO -> categoryDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockCategoryDTOList.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.containsInAnyOrder(mockCategoryDTOList.stream()
                                .map(categoryDTO -> categoryDTO.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.containsInAnyOrder(mockCategoryDTOList.stream()
                                .map(categoryDTO -> categoryDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCategoriesInMovies() {
        when(movieService.getCategoriesInMovies(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        when(movieService.countCategoriesInMovies(any()))
                .thenReturn(Uni.createFrom().item(0L));

        given()
                .when()
                .get("/categories")
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnPersonsByMovieSuccessfully() {
        List<LitePersonDTO> mockLitePersonDTOList = Factory.mockLitePersonDTOList(5);

        when(movieService.getPersonsByMovie(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockLitePersonDTOList));

        when(movieService.countPersonsByMovie(any(), any()))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/1/persons")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "5")
                .body(
                        "id", Matchers.containsInAnyOrder(mockLitePersonDTOList.stream()
                                .map(litePersonDTO -> litePersonDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockLitePersonDTOList.stream()
                                .map(LitePersonDTO::getName)
                                .toArray(String[]::new)),
                        "photoFileName", Matchers.containsInAnyOrder(mockLitePersonDTOList.stream()
                                .map(LitePersonDTO::getPhotoFileName)
                                .toArray(String[]::new)),
                        "dateOfBirth", Matchers.containsInAnyOrder(mockLitePersonDTOList.stream()
                                .map(litePersonDTO -> litePersonDTO.getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "dateOfDeath", Matchers.containsInAnyOrder(mockLitePersonDTOList.stream()
                                .map(litePersonDTO -> litePersonDTO.getDateOfDeath().toString())
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoPersonsByMovie() {
        when(movieService.getPersonsByMovie(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        when(movieService.countPersonsByMovie(any(), any()))
                .thenReturn(Uni.createFrom().item(0L));

        given()
                .when()
                .get("/1/persons")
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnTechnicalTeamSuccessfully() {
        TechnicalTeamDTO mockTechnicalTeamDTO = Factory.mockTechnicalTeamDTO();

        when(movieService.getTechnicalTeam(any()))
                .thenReturn(Uni.createFrom().item(mockTechnicalTeamDTO));

        given()
                .when()
                .get("/1/technical-team")
                .then()
                .statusCode(200)
                .body(
                        "directors.id", Matchers.contains(mockTechnicalTeamDTO.getDirectors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "assistantDirectors.id", Matchers.contains(mockTechnicalTeamDTO.getAssistantDirectors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "screenwriters.id", Matchers.contains(mockTechnicalTeamDTO.getScreenwriters().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "producers.id", Matchers.contains(mockTechnicalTeamDTO.getProducers().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "composers.id", Matchers.contains(mockTechnicalTeamDTO.getComposers().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "musicians.id", Matchers.contains(mockTechnicalTeamDTO.getMusicians().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "photographers.id", Matchers.contains(mockTechnicalTeamDTO.getPhotographers().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "costumeDesigners.id", Matchers.contains(mockTechnicalTeamDTO.getCostumeDesigners().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "stageDesigners.id", Matchers.contains(mockTechnicalTeamDTO.getStageDesigners().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "editors.id", Matchers.contains(mockTechnicalTeamDTO.getEditors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "casters.id", Matchers.contains(mockTechnicalTeamDTO.getCasters().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "artists.id", Matchers.contains(mockTechnicalTeamDTO.getArtists().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "soundEditors.id", Matchers.contains(mockTechnicalTeamDTO.getSoundEditors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "vfxSupervisors.id", Matchers.contains(mockTechnicalTeamDTO.getVfxSupervisors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "sfxSupervisors.id", Matchers.contains(mockTechnicalTeamDTO.getSfxSupervisors().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "makeupArtists.id", Matchers.contains(mockTechnicalTeamDTO.getMakeupArtists().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "hairDressers.id", Matchers.contains(mockTechnicalTeamDTO.getHairDressers().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "stuntmen.id", Matchers.contains(mockTechnicalTeamDTO.getStuntmen().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnActorsByMovieSuccessfully() {
        List<MovieActorDTO> mockMovieActorDTOList = Factory.mockMovieActorDTOList(5);

        when(movieService.getActorsByMovie(any()))
                .thenReturn(Uni.createFrom().item(mockMovieActorDTOList));

        given()
                .when()
                .get("/1/actors")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieActorDTOList.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(movieActorDTO -> movieActorDTO.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRole)
                                .toArray(String[]::new)),
                        "rank", Matchers.containsInAnyOrder(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRank)
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoActorsByMovie() {
        when(movieService.getActorsByMovie(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get("/1/actors")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnCategoriesByMovieSuccessfully() {
        Set<CategoryDTO> mockCategoryDTOSet = Factory.mockCategoryDTOSet(5);

        when(movieService.getCategoriesByMovie(any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOSet));

        given()
                .when()
                .get("/1/categories")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCategoryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(categoryDTO -> categoryDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(categoryDTO -> categoryDTO.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(categoryDTO -> categoryDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCategoriesByMovie() {
        when(movieService.getCategoriesByMovie(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .get("/1/categories")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnCountriesByMovieSuccessfully() {
        Set<CountryDTO> mockCountryDTOSet = Factory.mockCountryDTOSet(5);

        when(movieService.getCountriesByMovie(any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOSet));

        given()
                .when()
                .get("/1/countries?lang=fr")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCountryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(countryDTO -> countryDTO.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCountriesByMovie() {
        when(movieService.getCountriesByMovie(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .get("/1/countries?lang=fr")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testGetCeremoniesAwardsByMovie() {
        Set<CeremonyAwardsDTO> mockCeremonyAwardsDTOSet = Factory.mockCeremonyAwardsDTOSet(5);

        when(movieService.getCeremoniesAwardsByMovie(any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyAwardsDTOSet));

        given()
                .when()
                .get("/1/ceremonies-awards")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCeremonyAwardsDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "ceremony.id", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getCeremony().getId().intValue())
                                .toArray(Integer[]::new)),
                        "ceremony.name", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getCeremony().getName())
                                .toArray(String[]::new)),
                        "awards.id.flatten()", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .flatMap(dto -> dto.getAwards().stream())
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "awards.name.flatten()", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .flatMap(dto -> dto.getAwards().stream())
                                .map(AwardDTO::getName)
                                .toArray(String[]::new)),
                        "awards.year.flatten()", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .flatMap(dto -> dto.getAwards().stream())
                                .map(dto -> dto.getYear().toString())
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void testGetCeremoniesAwardsByMovieEmpty() {
        when(movieService.getCeremoniesAwardsByMovie(any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .get("/1/ceremonies-awards")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnMovieCreationDateEvolution() {
        List<Repartition> mockRepartitionList = Factory.mockRepartitionList(10);

        when(movieService.getMoviesCreationDateEvolution())
                .thenReturn(Uni.createFrom().item(mockRepartitionList));

        given()
                .when()
                .get("/creation-date-evolution")
                .then()
                .statusCode(200)
                .body(
                        "size()", is(10),
                        "label", Matchers.contains(mockRepartitionList.stream()
                                .map(Repartition::label)
                                .toArray(String[]::new)),
                        "total", Matchers.contains(mockRepartitionList.stream()
                                .map(dto -> dto.total().intValue())
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnMovieRepartitionByCreationDate() {
        List<Repartition> mockRepartitionList = Factory.mockRepartitionList(10);

        when(movieService.getMoviesCreationDateRepartition())
                .thenReturn(Uni.createFrom().item(mockRepartitionList));

        given()
                .when()
                .get("/creation-date-repartition")
                .then()
                .statusCode(200)
                .body(
                        "size()", is(10),
                        "label", Matchers.contains(mockRepartitionList.stream()
                                .map(Repartition::label)
                                .toArray(String[]::new)),
                        "total", Matchers.contains(mockRepartitionList.stream()
                                .map(dto -> dto.total().intValue())
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnMovieRepartitionByDecade() {
        List<Repartition> mockRepartitionList = Factory.mockRepartitionList(10);

        when(movieService.getMoviesReleaseDateRepartition())
                .thenReturn(Uni.createFrom().item(mockRepartitionList));

        given()
                .when()
                .get("/decade-repartition")
                .then()
                .statusCode(200)
                .body(
                        "size()", is(10),
                        "label", Matchers.contains(mockRepartitionList.stream()
                                .map(Repartition::label)
                                .toArray(String[]::new)),
                        "total", Matchers.contains(mockRepartitionList.stream()
                                .map(dto -> dto.total().intValue())
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void testCreateMovie() throws IOException {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();
        mockMovieDTO.setId(null);

        File tempFile = Factory.mockFile();

        // Simule un enregistrement avec retour du MovieDTO (avec un id maintenant défini)
        MovieDTO savedMovieDTO = Factory.mockMovieDTO(); // Celui retourné par le service avec un id non nul

        when(movieService.saveMovie(any(), any()))
                .thenReturn(Uni.createFrom().item(savedMovieDTO));

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .post()
                .then()
                .statusCode(201)
                .body(
                        "id", equalTo(savedMovieDTO.getId().intValue()),
                        "title", equalTo(savedMovieDTO.getTitle()),
                        "originalTitle", equalTo(savedMovieDTO.getOriginalTitle()),
                        "synopsis", equalTo(savedMovieDTO.getSynopsis()),
                        "releaseDate", equalTo(savedMovieDTO.getReleaseDate().toString()),
                        "runningTime", equalTo(savedMovieDTO.getRunningTime()),
                        "budget.value", equalTo(savedMovieDTO.getBudget().value()),
                        "budget.currency", equalTo(savedMovieDTO.getBudget().currency()),
                        "boxOffice.value", equalTo(savedMovieDTO.getBoxOffice().value()),
                        "boxOffice.currency", equalTo(savedMovieDTO.getBoxOffice().currency()),
                        "posterFileName", equalTo(savedMovieDTO.getPosterFileName()),
                        "creationDate", equalTo(savedMovieDTO.getCreationDate().format(Utils.dateTimeFormatter)),
                        "lastUpdate", equalTo(savedMovieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                )
        ;

        tempFile.delete();
    }

    @Test
    void shouldReturnBadRequestWhenMovieIdIsInvalid() throws IOException {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();

        File tempFile = File.createTempFile("test-upload", ".jpg");
        Files.writeString(tempFile.toPath(), "fake image data");

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(equalTo("L’identifiant a été défini de manière incorrecte dans la requête"))
        ;
    }

    @Test
    void shouldReturnBadRequestWhenMovieTitleIsMissing() throws IOException {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();
        mockMovieDTO.setId(null);
        mockMovieDTO.setTitle(null);

        File tempFile = Factory.mockFile();

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(
                        "details", Matchers.is("Le titre du film est obligatoire"),
                        "message", Matchers.is("Erreur de validation")
                )
        ;

        tempFile.delete();
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws IOException {
        File tempFile = Factory.mockFile();

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", (Object) null, "application/json")
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(equalTo("Aucune information sur le film n’a été fournie dans la requête"))
        ;
    }

    @Test
    void testUpdateMovie() throws IOException {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();

        File tempFile = Factory.mockFile();

        when(movieService.updateMovie(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTO));

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .put("/" + mockMovieDTO.getId())
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockMovieDTO.getId().intValue()),
                        "title", equalTo(mockMovieDTO.getTitle()),
                        "originalTitle", equalTo(mockMovieDTO.getOriginalTitle()),
                        "synopsis", equalTo(mockMovieDTO.getSynopsis()),
                        "releaseDate", equalTo(mockMovieDTO.getReleaseDate().toString()),
                        "runningTime", equalTo(mockMovieDTO.getRunningTime()),
                        "budget.value", equalTo(mockMovieDTO.getBudget().value()),
                        "budget.currency", equalTo(mockMovieDTO.getBudget().currency()),
                        "boxOffice.value", equalTo(mockMovieDTO.getBoxOffice().value()),
                        "boxOffice.currency", equalTo(mockMovieDTO.getBoxOffice().currency()),
                        "posterFileName", equalTo(mockMovieDTO.getPosterFileName()),
                        "creationDate", equalTo(mockMovieDTO.getCreationDate().format(Utils.dateTimeFormatter)),
                        "lastUpdate", equalTo(mockMovieDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                )
        ;

        tempFile.delete();
    }

    @Test
    void testUpdateMovieWithInvalidPayload() throws IOException {
        File tempFile = Factory.mockFile();

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .when()
                .put("/1")
                .then()
                .statusCode(400)
                .body(equalTo("Aucune information sur le film n’a été fournie dans la requête"));

        tempFile.delete();
    }

    @Test
    void shouldReturnBadRequestIfTitleIsMissing() throws IOException {
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();
        mockMovieDTO.setTitle(null);

        File tempFile = Factory.mockFile();

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .put("/1")
                .then()
                .statusCode(400)
                .body(
                        "details", Matchers.is("Le titre du film est obligatoire"),
                        "message", Matchers.is("Erreur de validation")
                )
        ;

        tempFile.delete();
    }

    @Test
    void testUpdateMovieWithInconsistentId() throws IOException {
        // ID dans le corps ≠ ID dans l'URL
        MovieDTO mockMovieDTO = Factory.mockMovieDTO();
        mockMovieDTO.setId(2L); // L'URL contient /1 mais le corps a id = 2

        File tempFile = Factory.mockFile();

        given()
                .multiPart("file", tempFile, "image/jpeg")
                .multiPart("movieDTO", mockMovieDTO, "application/json")
                .when()
                .put("/1")
                .then()
                .statusCode(422)
                .body(equalTo("L'identifiant du film ne correspond pas à celui de la requête"));

        tempFile.delete();
    }

    @Test
    void testSaveCast() {
        List<MovieActorDTO> mockMovieActorDTOList = Factory.mockMovieActorDTOList(10);

        when(movieService.saveCast(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieActorDTOList));

        given()
                .contentType("application/json")
                .body(mockMovieActorDTOList)
                .when()
                .put("/1/cast")
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRole)
                                .toArray(String[]::new)),
                        "rank", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRank)
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenSavingEmptyCast() {
        when(movieService.saveCast(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .contentType("application/json")
                .body(Collections.emptyList())
                .when()
                .put("/1/cast")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testSaveCastWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .put("/1/cast")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des acteurs ne peut pas être nulle"))
        ;
    }

    @ParameterizedTest
    @MethodSource("provideTechnicianEndpointsAndErrorMessages")
    void testSaveTechniciansByMovieWithInvalidPayload(String endpoint, String expectedMessage) {
        given()
                .contentType("application/json")
                .when()
                .put(endpoint)
                .then()
                .statusCode(400)
                .body(equalTo(expectedMessage));
    }

    @Test
    void testSaveCategories() {
        Set<CategoryDTO> mockCategoryDTOSet = Factory.mockCategoryDTOSet(5);

        when(movieService.saveCategories(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOSet));

        given()
                .contentType("application/json")
                .body(mockCategoryDTOSet)
                .when()
                .put("/1/categories")
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void testSaveCategoriesEmpty() {
        when(movieService.saveCategories(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .contentType("application/json")
                .body(Collections.emptySet())
                .when()
                .put("/1/categories")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testSaveCategoriesWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .put("/1/categories")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des catégories ne peut pas être nulle"))
        ;
    }

    @Test
    void testSaveCountries() {
        Set<CountryDTO> mockCountryDTOSet = Factory.mockCountryDTOSet(5);

        when(movieService.saveCountries(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOSet));

        given()
                .contentType("application/json")
                .body(mockCountryDTOSet)
                .when()
                .put("/1/countries")
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void testSaveCountriesEmpty() {
        when(movieService.saveCountries(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .contentType("application/json")
                .body(Collections.emptySet())
                .when()
                .put("/1/countries")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testSaveCountriesWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .put("/1/countries")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des pays ne peut pas être nulle"))
        ;
    }

    @Test
    void shouldReturn200WhenSavingCeremonyAwards() {
        CeremonyAwardsDTO mockCeremonyAwardsDTO = Factory.mockCeremonyAwardsDTO();

        when(movieService.saveCeremonyAwards(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyAwardsDTO));

        given()
                .contentType("application/json")
                .body(mockCeremonyAwardsDTO)
                .when()
                .put("/1/ceremonies-awards")
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.is(mockCeremonyAwardsDTO.getId().intValue()),
                        "ceremony.id", Matchers.is(mockCeremonyAwardsDTO.getCeremony().getId().intValue()),
                        "ceremony.name", Matchers.is(mockCeremonyAwardsDTO.getCeremony().getName()),
                        "awards.id", Matchers.containsInAnyOrder(mockCeremonyAwardsDTO.getAwards().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "awards.name", Matchers.containsInAnyOrder(mockCeremonyAwardsDTO.getAwards().stream()
                                .map(AwardDTO::getName)
                                .toArray(String[]::new)),
                        "awards.year", Matchers.containsInAnyOrder(mockCeremonyAwardsDTO.getAwards().stream()
                                .map(dto -> dto.getYear().toString())
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnBadRequestWhenSavingNullCeremonyAwards() {
        given()
                .contentType("application/json")
                .when()
                .put("/1/ceremonies-awards")
                .then()
                .statusCode(400)
                .body(Matchers.equalTo("La liste des récompenses ne peut pas être nulle"))
        ;
    }


    @ParameterizedTest
    @MethodSource("provideTechnicianEndpointsAndErrorMessages")
    void testAddTechniciansByMovieWithInvalidPayload(String endpoint, String expectedMessage) {
        given()
                .contentType("application/json")
                .when()
                .patch(endpoint)
                .then()
                .statusCode(400)
                .body(equalTo(expectedMessage));
    }

    @Test
    void testAddActorsByMovie() {
        List<MovieActorDTO> mockMovieActorDTOList = Factory.mockMovieActorDTOList(6);

        when(movieService.addMovieActors(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieActorDTOList));

        given()
                .contentType("application/json")
                .body(mockMovieActorDTOList.subList(0, 3))
                .when()
                .patch("/1/roles")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieActorDTOList.size()))
                .body(
                        "id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRole)
                                .toArray(String[]::new)),
                        "rank", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRank)
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void testAddActorsByMovieEmpty() {
        when(movieService.addMovieActors(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .contentType("application/json")
                .body(Collections.emptyList())
                .when()
                .patch("/1/roles")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testAddActorsByMovieWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .patch("/1/roles")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des acteurs ne peut pas être nulle"));
    }

    @Test
    void testAddCategoriesByMovie() {
        Set<CategoryDTO> mockCategoryDTOSet = Factory.mockCategoryDTOSet(5);

        when(movieService.addCategories(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOSet));

        given()
                .contentType("application/json")
                .body(mockCategoryDTOSet.stream()
                        .limit(3)
                        .collect(Collectors.toSet())
                )
                .when()
                .patch("/1/categories")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCategoryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void testAddACategoriesByMovieEmpty() {
        when(movieService.addCategories(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .contentType("application/json")
                .body(Collections.emptySet())
                .when()
                .patch("/1/categories")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testAddCategoriesByMovieWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .patch("/1/categories")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des catégories ne peut pas être nulle"));
    }

    @Test
    void testAddCountriesByMovie() {
        Set<CountryDTO> mockCountryDTOSet = Factory.mockCountryDTOSet(5);

        when(movieService.addCountries(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOSet));

        given()
                .contentType("application/json")
                .body(mockCountryDTOSet.stream()
                        .limit(3)
                        .collect(Collectors.toSet())
                )
                .when()
                .patch("/1/countries")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCountryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void testAddACountriesByMovieEmpty() {
        when(movieService.addCountries(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .contentType("application/json")
                .body(Collections.emptySet())
                .when()
                .patch("/1/countries")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void testAddCountriesByMovieWithInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .patch("/1/countries")
                .then()
                .statusCode(400)
                .body(equalTo("La liste des pays ne peut pas être nulle"));
    }

    @Test
    void shouldReturn200WhenRemovingActor() {
        List<MovieActorDTO> mockMovieActorDTOList = Factory.mockMovieActorDTOList(6);

        when(movieService.removeMovieActor(any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieActorDTOList));

        given()
                .when()
                .patch("/1/roles/1")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieActorDTOList.size()))
                .body(
                        "id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRole)
                                .toArray(String[]::new)),
                        "rank", Matchers.contains(mockMovieActorDTOList.stream()
                                .map(MovieActorDTO::getRank)
                                .toArray(Integer[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenRemovingActorAndActorListIsEmpty() {
        when(movieService.removeMovieActor(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .patch("/1/roles/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturn200WhenRemovingCategory() {
        Set<CategoryDTO> mockCategoryDTOSet = Factory.mockCategoryDTOSet(5);

        when(movieService.removeCategory(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOSet));

        given()
                .when()
                .patch("/1/categories/1")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCategoryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.containsInAnyOrder(mockCategoryDTOSet.stream()
                                .map(dto -> dto.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenRemovingCategoryAndCategorySetIsEmpty() {
        when(movieService.removeCategory(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .patch("/1/categories/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturn200WhenRemovingCountry() {
        Set<CountryDTO> mockCountryDTOSet = Factory.mockCountryDTOSet(5);

        when(movieService.removeCountry(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOSet));

        given()
                .when()
                .patch("/1/countries/1")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCountryDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.containsInAnyOrder(mockCountryDTOSet.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenRemovingCountryAndCountrySetIsEmpty() {
        when(movieService.removeCountry(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .patch("/1/countries/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturn200WhenRemovingCeremonyAwards() {
        Set<CeremonyAwardsDTO> mockCeremonyAwardsDTOSet = Factory.mockCeremonyAwardsDTOSet(5);

        when(movieService.removeCeremonyAwards(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyAwardsDTOSet));

        given()
                .when()
                .patch("/1/ceremonies-awards/1")
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockCeremonyAwardsDTOSet.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "ceremony.id", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getCeremony().getId().intValue())
                                .toArray(Integer[]::new)),
                        "ceremony.name", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .map(dto -> dto.getCeremony().getName())
                                .toArray(String[]::new)),
                        "awards.id.flatten()", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .flatMap(dto -> dto.getAwards().stream())
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "awards.name.flatten()", Matchers.containsInAnyOrder(mockCeremonyAwardsDTOSet.stream()
                                .flatMap(dto -> dto.getAwards().stream())
                                .map(AwardDTO::getName)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenRemovingCeremonyAwardsAndCeremonyAwardsSetIsEmpty() {
        when(movieService.removeCeremonyAwards(any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .patch("/1/ceremonies-awards/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNoContentWhenMovieIsDeleted() {
        when(movieService.deleteMovie(1L)).thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNotFoundWhenMovieIsNotDeleted() {
        when(movieService.deleteMovie(any()))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/1")
                .then()
                .statusCode(404)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNoContentWhenActorsAreRemoved() {
        when(movieService.clearActors(any()))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1/actors")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNoContentWhenCategoriesAreRemoved() {
        when(movieService.clearCategories(any()))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1/categories")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNoContentWhenCountriesAreRemoved() {
        when(movieService.clearCountries(any()))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1/countries")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNoContentWhenCeremoniesAwardsAreRemoved() {
        when(movieService.clearCeremoniesAwards(any()))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1/ceremonies-awards")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    private static Stream<Arguments> provideTechnicianEndpointsAndErrorMessages() {
        return Stream.of(
                Arguments.of("/1/producers", "La liste des producteurs ne peut pas être nulle"),
                Arguments.of("/1/directors", "La liste des réalisateurs ne peut pas être nulle"),
                Arguments.of("/1/assistant-directors", "La liste des assistants réalisateurs ne peut pas être nulle"),
                Arguments.of("/1/screenwriters", "La liste des scénaristes ne peut pas être nulle"),
                Arguments.of("/1/composers", "La liste des compositeurs ne peut pas être nulle"),
                Arguments.of("/1/musicians", "La liste des musiciens ne peut pas être nulle"),
                Arguments.of("/1/photographers", "La liste des photographes ne peut pas être nulle"),
                Arguments.of("/1/costume-designers", "La liste des costumiers ne peut pas être nulle"),
                Arguments.of("/1/set-designers", "La liste des décorateurs ne peut pas être nulle"),
                Arguments.of("/1/editors", "La liste des monteurs ne peut pas être nulle"),
                Arguments.of("/1/casters", "La liste des casteurs ne peut pas être nulle"),
                Arguments.of("/1/artists", "La liste des artistes ne peut pas être nulle"),
                Arguments.of("/1/sound-editors", "La liste des ingénieurs son ne peut pas être nulle"),
                Arguments.of("/1/vfx-supervisors", "La liste des spécialistes des effets visuels ne peut pas être nulle"),
                Arguments.of("/1/sfx-supervisors", "La liste des spécialistes des effets spéciaux ne peut pas être nulle"),
                Arguments.of("/1/makeup-artists", "La liste des maquilleurs ne peut pas être nulle"),
                Arguments.of("/1/hair-dressers", "La liste des coiffeurs ne peut pas être nulle"),
                Arguments.of("/1/stuntmen", "La liste des cascadeurs ne peut pas être nulle")
        );
    }

    /*private static Stream<Arguments> provideInvalidTechnicianFailures() {
        return Stream.of(
                Arguments.of("/1/producers", "Erreur lors de la mise à jour des producteurs"),
                Arguments.of("/1/directors", "Erreur lors de la mise à jour des réalisateurs"),
                Arguments.of("/1/assistant-directors", "Erreur lors de la mise à jour des assistants réalisateurs"),
                Arguments.of("/1/screenwriters", "Erreur lors de la mise à jour des scénaristes"),
                Arguments.of("/1/composers", "Erreur lors de la mise à jour des compositeurs"),
                Arguments.of("/1/musicians", "Erreur lors de la mise à jour des musiciens"),
                Arguments.of("/1/photographers", "Erreur lors de la mise à jour des photographes"),
                Arguments.of("/1/costume-designers", "Erreur lors de la mise à jour des costumiers"),
                Arguments.of("/1/set-designers", "Erreur lors de la mise à jour des décorateurs"),
                Arguments.of("/1/editors", "Erreur lors de la mise à jour des monteurs"),
                Arguments.of("/1/casters", "Erreur lors de la mise à jour des casteurs"),
                Arguments.of("/1/artists", "Erreur lors de la mise à jour des artistes"),
                Arguments.of("/1/sound-editors", "Erreur lors de la mise à jour des ingénieurs son"),
                Arguments.of("/1/vfx-supervisors", "Erreur lors de la mise à jour des spécialistes des effets visuels"),
                Arguments.of("/1/sfx-supervisors", "Erreur lors de la mise à jour des spécialistes des effets spéciaux"),
                Arguments.of("/1/makeup-artists", "Erreur lors de la mise à jour des maquilleurs"),
                Arguments.of("/1/hair-dressers", "Erreur lors de la mise à jour des coiffeurs"),
                Arguments.of("/1/stuntmen", "Erreur lors de la mise à jour des cascadeurs")
        );
    }*/

    /*private static Stream<Arguments> provideTechnicianEndpointsAndInvalidIdsMessages() {
        return Stream.of(
                Arguments.of("/1/producers/0", Messages.INVALID_PRODUCER_ID),
                Arguments.of("/1/directors/0", Messages.INVALID_DIRECTOR_ID),
                Arguments.of("/1/assistant-directors/0", Messages.INVALID_ASSISTANT_DIRECTOR_ID),
                Arguments.of("/1/screenwriters/0", Messages.INVALID_SCREENWRITER_ID),
                Arguments.of("/1/composers/0", Messages.INVALID_COMPOSER_ID),
                Arguments.of("/1/musicians/0", Messages.INVALID_MUSICIAN_ID),
                Arguments.of("/1/photographers/0", Messages.INVALID_PHOTOGRAPHER_ID),
                Arguments.of("/1/costume-designers/0", Messages.INVALID_COSTUME_DESIGNER_ID),
                Arguments.of("/1/set-designers/0", Messages.INVALID_SET_DESIGNER_ID),
                Arguments.of("/1/editors/0", Messages.INVALID_EDITOR_ID),
                Arguments.of("/1/casters/0", Messages.INVALID_CASTER_ID),
                Arguments.of("/1/artists/0", Messages.INVALID_ARTIST_ID),
                Arguments.of("/1/sound-editors/0", Messages.INVALID_SOUND_EDITOR_ID),
                Arguments.of("/1/vfx-supervisors/0", Messages.INVALID_VFX_SUPERVISOR_ID),
                Arguments.of("/1/sfx-supervisors/0", Messages.INVALID_SFX_SUPERVISOR_ID),
                Arguments.of("/1/makeup-artists/0", Messages.INVALID_MAKEUP_ARTIST_ID),
                Arguments.of("/1/hair-dressers/0", Messages.INVALID_HAIRDRESSER_ID),
                Arguments.of("/1/stuntmen/0", Messages.INVALID_STUNTMAN_ID)
        );
    }*/

    /*private static Stream<Arguments> provideTechnicianEndpointsAndAddingErrorMessages() {
        return Stream.of(
                Arguments.of("/1/producers", Messages.ERROR_WHILE_ADDING_PRODUCERS),
                Arguments.of("/1/directors", Messages.ERROR_WHILE_ADDING_DIRECTORS),
                Arguments.of("/1/assistant-directors", "Erreur lors de l'ajout des assistants réalisateurs"),
                Arguments.of("/1/screenwriters", "Erreur lors de l'ajout des scénaristes"),
                Arguments.of("/1/composers", "Erreur lors de l'ajout des compositeurs"),
                Arguments.of("/1/musicians", "Erreur lors de l'ajout des musiciens"),
                Arguments.of("/1/photographers", "Erreur lors de l'ajout des photographes"),
                Arguments.of("/1/costume-designers", "Erreur lors de l'ajout des costumiers"),
                Arguments.of("/1/set-designers", "Erreur lors de l'ajout des décorateurs"),
                Arguments.of("/1/editors", "Erreur lors de l'ajout des monteurs"),
                Arguments.of("/1/casters", "Erreur lors de l'ajout des casteurs"),
                Arguments.of("/1/artists", "Erreur lors de l'ajout des artistes"),
                Arguments.of("/1/sound-editors", "Erreur lors de l'ajout des ingénieurs son"),
                Arguments.of("/1/vfx-supervisors", "Erreur lors de l'ajout des spécialistes des effets visuels"),
                Arguments.of("/1/sfx-supervisors", "Erreur lors de l'ajout des spécialistes des effets spéciaux"),
                Arguments.of("/1/makeup-artists", "Erreur lors de l'ajout des maquilleurs"),
                Arguments.of("/1/hair-dressers", "Erreur lors de l'ajout des coiffeurs"),
                Arguments.of("/1/stuntmen", "Erreur lors de l'ajout des cascadeurs")
        );
    }*/
}
