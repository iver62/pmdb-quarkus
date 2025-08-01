package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.desha.app.data.Factory;
import org.desha.app.data.Utils;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.service.CountryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CountryResource.class)
@TestSecurity(user = "pierrickd", roles = {"admin", "user"})
class CountryEndPointTest {

    @InjectMock
    private CountryService countryService;

    @Test
    void shouldReturnNumberWhenCount() {
        when(countryService.countCountries(any(), any()))
                .thenReturn(Uni.createFrom().item(100L));

        given()
                .when()
                .get("/count?lang=fr")
                .then()
                .statusCode(200)
                .body(is("100"))
        ;
    }

    @Test
    void shouldReturnCountry() {
        CountryDTO mockCountryDTO = Factory.mockCountryDTO();

        when(countryService.getById(any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTO));

        given()
                .when()
                .get("/1?lang=fr")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCountryDTO.getId().intValue()),
                        "code", equalTo(mockCountryDTO.getCode()),
                        "alpha2", equalTo(mockCountryDTO.getAlpha2()),
                        "alpha3", equalTo(mockCountryDTO.getAlpha3()),
                        "nomEnGb", equalTo(mockCountryDTO.getNomEnGb()),
                        "nomFrFr", equalTo(mockCountryDTO.getNomFrFr())
                )
        ;
    }

    @Test
    void shouldReturnCountryListSuccessfully() {
        List<CountryDTO> mockCountryDTOList = Factory.mockCountryDTOList(100);

        when(countryService.getCountries(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTOList));

        given()
                .when()
                .get("?lang=fr")
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.contains(mockCountryDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "code", Matchers.contains(mockCountryDTOList.stream()
                                .map(CountryDTO::getCode)
                                .toArray(Integer[]::new)),
                        "alpha2", Matchers.contains(mockCountryDTOList.stream()
                                .map(CountryDTO::getAlpha2)
                                .toArray(String[]::new)),
                        "alpha3", Matchers.contains(mockCountryDTOList.stream()
                                .map(CountryDTO::getAlpha3)
                                .toArray(String[]::new)),
                        "nomEnGb", Matchers.contains(mockCountryDTOList.stream()
                                .map(CountryDTO::getNomEnGb)
                                .toArray(String[]::new)),
                        "nomFrFr", Matchers.contains(mockCountryDTOList.stream()
                                .map(CountryDTO::getNomFrFr)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCountriesFound() {
        when(countryService.getCountries(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get("?lang=fr")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnMovieListByCountrySuccessfully() {
        List<MovieDTO> mockMovieDTOList = Factory.mockMovieDTOList(60);

        when(countryService.getMoviesByCountry(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTOList));

        when(countryService.countMoviesByCountry(any(), any()))
                .thenReturn(Uni.createFrom().item((long) mockMovieDTOList.size()));

        given()
                .when()
                .get("/1/movies")
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
                                .map(movieDTO -> movieDTO.getRunningTime().intValue())
                                .toArray(Integer[]::new)),
                        "budget.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().getValue().intValue())
                                .toArray(Integer[]::new)),
                        "budget.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBudget().getCurrency())
                                .toArray(String[]::new)),
                        "boxOffice.value", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().getValue().intValue())
                                .toArray(Integer[]::new)),
                        "boxOffice.currency", Matchers.contains(mockMovieDTOList.stream()
                                .map(movieDTO -> movieDTO.getBoxOffice().getCurrency())
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
    void shouldReturnNoContentWhenNoMoviesFound() {
        when(countryService.getMoviesByCountry(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get("/1/movies")
                .then()
                .statusCode(204)
                .header("X-Total-Count", "0")
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnPersonListByCountrySuccessfully() {
        List<LitePersonDTO> mockLitePersonDTOList = Factory.mockLitePersonDTOList(20);

        when(countryService.getPersonsByCountry(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockLitePersonDTOList));

        when(countryService.countPersonsByCountry(any(), any()))
                .thenReturn(Uni.createFrom().item((long) mockLitePersonDTOList.size()));

        given()
                .when()
                .get("/1/persons")
                .then()
                .statusCode(200)
                .header("X-Total-Count", "20")
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
    void shouldReturnNoContentWhenNoPersonsFound() {
        when(countryService.getPersonsByCountry(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

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
    void shouldUpdateCountrySuccessfully() {
        CountryDTO mockCountryDTO = Factory.mockCountryDTO();

        when(countryService.update(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCountryDTO));

        given()
                .contentType("application/json")
                .body(mockCountryDTO)
                .when()
                .put("/" + mockCountryDTO.getId())
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCountryDTO.getId().intValue()),
                        "code", equalTo(mockCountryDTO.getCode()),
                        "alpha2", equalTo(mockCountryDTO.getAlpha2()),
                        "alpha3", equalTo(mockCountryDTO.getAlpha3()),
                        "nomEnGb", equalTo(mockCountryDTO.getNomEnGb()),
                        "nomFrFr", equalTo(mockCountryDTO.getNomFrFr())
                )
        ;
    }

    @Test
    void shouldReturnBadRequestWhenInvalidPayload() {
        given()
                .contentType("application/json")
                .when()
                .put("/1")
                .then()
                .statusCode(400)
                .body(equalTo("Aucune information sur le pays n’a été fournie dans la requête"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenInconsistentId() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();
        mockCategoryDTO.setId(2L);

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .put("/1")
                .then()
                .statusCode(422)
                .body(equalTo("L'identifiant du pays ne correspond pas à celui de la requête"));
    }
}
