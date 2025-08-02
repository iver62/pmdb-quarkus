package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.desha.app.data.Factory;
import org.desha.app.data.Utils;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.service.CategoryService;
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
@TestHTTPEndpoint(CategoryResource.class)
@TestSecurity(user = "pierrickd", roles = {"admin", "user"})
class CategoryEndPointTest {

    @InjectMock
    private CategoryService categoryService;

    @Test
    void shouldReturnNumberWhenCount() {
        when(categoryService.count(any()))
                .thenReturn(Uni.createFrom().item(10L));

        given()
                .when()
                .get("/count")
                .then()
                .statusCode(200)
                .body(is("10"))
        ;
    }

    @Test
    void shouldReturnCategory() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();

        when(categoryService.getById(any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTO));

        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCategoryDTO.getId().intValue()),
                        "name", equalTo(mockCategoryDTO.getName()),
                        "creationDate", equalTo(mockCategoryDTO.getCreationDate().format(Utils.dateTimeFormatter)),
                        "lastUpdate", equalTo(mockCategoryDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                )
        ;
    }

    @Test
    void shouldReturnCategoryListSuccessfully() {
        List<CategoryDTO> mockCategoryDTOList = Factory.mockCategoryDTOList(10);

        when(categoryService.getCategories(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTOList));

        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.contains(mockCategoryDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.contains(mockCategoryDTOList.stream()
                                .map(CategoryDTO::getName)
                                .toArray(String[]::new)),
                        "creationDate", Matchers.contains(mockCategoryDTOList.stream()
                                .map(dto -> dto.getCreationDate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new)),
                        "lastUpdate", Matchers.contains(mockCategoryDTOList.stream()
                                .map(dto -> dto.getLastUpdate().format(Utils.dateTimeFormatter))
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCategoriesFound() {
        when(categoryService.getCategories(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get()
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnMovieListByCategorySuccessfully() {
        List<MovieDTO> mockMovieDTOList = Factory.mockMovieDTOList(60);

        when(categoryService.getMoviesByCategory(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieDTOList));

        when(categoryService.countMoviesByCategory(any(), any()))
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
        when(categoryService.getMoviesByCategory(any(), any(), any(), any(), any()))
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
    void shouldCreateCategorySuccessfully() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();
        mockCategoryDTO.setId(null);
        Category mockCategory = Factory.mockCategory();


        when(categoryService.create(any()))
                .thenReturn(Uni.createFrom().item(mockCategory));

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body(
                        "id", equalTo(mockCategory.getId().intValue()),
                        "name", equalTo(mockCategory.getName())
                )
        ;
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIdIsProvided() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(equalTo("L’identifiant a été défini de manière incorrecte dans la requête"))
        ;
    }

    @Test
    void shouldReturnBadRequestWhenCategoryNameIsMissing() {
        CategoryDTO mockCategoryDTO = new CategoryDTO();

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(
                        "message", Matchers.is("Erreur de validation"),
                        "details", Matchers.is("Le nom de la catégorie est obligatoire")
                )
        ;
    }

    @Test
    void shouldReturnConflictWhenCategoryAlreadyExists() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();
        mockCategoryDTO.setId(null);

        when(categoryService.create(any()))
                .thenReturn(Uni.createFrom().failure(
                                new WebApplicationException("Erreur, La catégorie existe déjà ou ne respecte pas les contraintes de validation", Response.Status.CONFLICT)
                        )
                )
        ;

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockCategoryDTO)
                .when()
                .post()
                .then()
                .statusCode(409)
                .body(equalTo("Erreur, La catégorie existe déjà ou ne respecte pas les contraintes de validation"))
        ;
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();

        when(categoryService.update(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCategoryDTO));

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .put("/" + mockCategoryDTO.getId())
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCategoryDTO.getId().intValue()),
                        "name", equalTo(mockCategoryDTO.getName()),
                        "creationDate", equalTo(mockCategoryDTO.getCreationDate().format(Utils.dateTimeFormatter)),
                        "lastUpdate", equalTo(mockCategoryDTO.getLastUpdate().format(Utils.dateTimeFormatter))
                )
        ;
    }

    @Test
    void shouldReturnBadRequestIfNameIsMissing() {
        CategoryDTO mockCategoryDTO = Factory.mockCategoryDTO();
        mockCategoryDTO.setName(null);

        given()
                .contentType("application/json")
                .body(mockCategoryDTO)
                .when()
                .put("/1")
                .then()
                .statusCode(400)
                .body(
                        "message", Matchers.is("Erreur de validation"),
                        "details", Matchers.is("Le nom de la catégorie est obligatoire")
                )
        ;
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
                .body(equalTo("L'identifiant de la catégorie ne correspond pas à celui de la requête"));
    }

    @Test
    void shouldReturnNoContentWhenCategoryIsDeleted() {
        when(categoryService.deleteCategory(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/1")
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldReturnNotFoundWhenCategoryIsNotDeleted() {
        when(categoryService.deleteCategory(any()))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/1")
                .then()
                .statusCode(404)
                .body(Matchers.emptyOrNullString())
        ;
    }
}
