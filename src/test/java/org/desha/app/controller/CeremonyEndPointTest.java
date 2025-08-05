package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.desha.app.data.Factory;
import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.service.CeremonyService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(CeremonyResource.class)
@TestSecurity(user = "pierrickd", roles = {"admin", "user"})
class CeremonyEndPointTest {

    @InjectMock
    private CeremonyService ceremonyService;

    @Test
    void shouldReturnCategory() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();

        when(ceremonyService.getCeremony(any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyDTO));

        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCeremonyDTO.getId().intValue()),
                        "name", equalTo(mockCeremonyDTO.getName())
                )
        ;
    }

    @Test
    void shouldReturnCeremonySetSuccessfully() {
        Set<CeremonyDTO> mockCeremonyDTOSet = Factory.mockCeremonyDTOSet(10);

        when(ceremonyService.getCeremonies(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyDTOSet));

        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "id", Matchers.containsInAnyOrder(mockCeremonyDTOSet.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "name", Matchers.contains(mockCeremonyDTOSet.stream()
                                .map(CeremonyDTO::getName)
                                .toArray(String[]::new))
                )
        ;
    }

    @Test
    void shouldReturnNoContentWhenNoCeremoniesFound() {
        when(ceremonyService.getCeremonies(any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptySet()));

        given()
                .when()
                .get()
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @Test
    void shouldCreateCeremonySuccessfully() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();
        mockCeremonyDTO.setId(null);

        CeremonyDTO savedCeremonyDTO = Factory.mockCeremonyDTO();

        when(ceremonyService.create(any()))
                .thenReturn(Uni.createFrom().item(savedCeremonyDTO));

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body(
                        "id", equalTo(savedCeremonyDTO.getId().intValue()),
                        "name", equalTo(savedCeremonyDTO.getName())
                )
        ;
    }

    @Test
    void shouldReturnBadRequestWhenCeremonyIdIsProvided() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(equalTo("L’identifiant a été défini de manière incorrecte dans la requête"))
        ;
    }

    @Test
    void shouldReturnBadRequestWhenCeremonyNameIsMissing() {
        CeremonyDTO mockCeremonyDTO = new CeremonyDTO();

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body(
                        "details", Matchers.is("Le nom de la cérémonie est obligatoire"),
                        "message", Matchers.is("Erreur de validation")
                )
        ;
    }

    @Test
    void shouldUpdateCeremonySuccessfully() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();

        when(ceremonyService.update(any(), any()))
                .thenReturn(Uni.createFrom().item(mockCeremonyDTO));

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .put("/" + mockCeremonyDTO.getId())
                .then()
                .statusCode(200)
                .body(
                        "id", equalTo(mockCeremonyDTO.getId().intValue()),
                        "name", equalTo(mockCeremonyDTO.getName())
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
                .body(equalTo("Aucune information sur la cérémonie n’a été fournie dans la requête"));
    }

    @Test
    void shouldReturnBadRequestIfNameIsMissing() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();
        mockCeremonyDTO.setName(null);

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .put("/" + mockCeremonyDTO.getId())
                .then()
                .statusCode(400)
                .body(
                        "details", Matchers.is("Le nom de la cérémonie est obligatoire"),
                        "message", Matchers.is("Erreur de validation")
                )
        ;
    }

    @Test
    void shouldReturnUnprocessableEntityWhenInconsistentId() {
        CeremonyDTO mockCeremonyDTO = Factory.mockCeremonyDTO();
        mockCeremonyDTO.setId(2L);

        given()
                .contentType("application/json")
                .body(mockCeremonyDTO)
                .when()
                .put("/1")
                .then()
                .statusCode(422)
                .body(equalTo("L'identifiant de la cérémonie ne correspond pas à celui de la requête"));
    }

    @Test
    void shouldReturnNoContentWhenCeremonyIsDeleted() {
        when(ceremonyService.deleteCeremony(1L))
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
        when(ceremonyService.deleteCeremony(any()))
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
