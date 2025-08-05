package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.desha.app.data.Factory;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.service.AwardService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(AwardResource.class)
@TestSecurity(user = "pierrickd", roles = {"admin", "user"})
class AwardEndPointTest {

    @InjectMock
    private AwardService awardService;

    @Test
    void shouldReturnAward() {
        AwardDTO mockAwardDTO = Factory.mockAwardDTO();

        when(awardService.getAward(any()))
                .thenReturn(Uni.createFrom().item(mockAwardDTO));

        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockAwardDTO.getId().intValue()),
                        "name", equalTo(mockAwardDTO.getName()),
                        "persons.id", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "persons.name", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(LitePersonDTO::getName)
                                .toArray(String[]::new)),
                        "persons.photoFileName", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(LitePersonDTO::getPhotoFileName)
                                .toArray(String[]::new)),
                        "persons.dateOfBirth", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "persons.dateOfDeath", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "year", equalTo(mockAwardDTO.getYear().toString())
                )
        ;
    }

    @Test
    void shouldUpdateAwardSuccessfully() {
        AwardDTO mockAwardDTO = Factory.mockAwardDTO();

        when(awardService.updateAward(any(), any()))
                .thenReturn(Uni.createFrom().item(mockAwardDTO));

        given()
                .contentType("application/json")
                .body(mockAwardDTO)
                .when()
                .put("/" + mockAwardDTO.getId())
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockAwardDTO.getId().intValue()),
                        "name", equalTo(mockAwardDTO.getName()),
                        "persons.id", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "persons.name", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(LitePersonDTO::getName)
                                .toArray(String[]::new)),
                        "persons.photoFileName", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(LitePersonDTO::getPhotoFileName)
                                .toArray(String[]::new)),
                        "persons.dateOfBirth", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "persons.dateOfDeath", Matchers.containsInAnyOrder(mockAwardDTO.getPersons().stream()
                                .map(dto -> dto.getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "year", equalTo(mockAwardDTO.getYear().toString())
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
                .body(equalTo("Aucune information sur la récompense n’a été fournie dans la requête"));
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
                        "details", Matchers.is("Le nom de la récompense est obligatoire"),
                        "message", Matchers.is("Erreur de validation")
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
                .body(equalTo("L'identifiant de la récompense ne correspond pas à celui de la requête"));
    }

    @Test
    void shouldReturnNoContentWhenAwardIsDeleted() {
        when(awardService.deleteAward(1L))
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
        when(awardService.deleteAward(any()))
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
