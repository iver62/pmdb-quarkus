package org.desha.app.controller;

import io.quarkus.test.InjectMock;
import io.smallrye.mutiny.Uni;
import org.desha.app.data.Factory;
import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.service.MovieTechnicianService;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MovieTechnicianEndPointTest {

    @InjectMock
    MovieTechnicianService movieTechnicianService;

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void shouldReturnTechniciansByMovieSuccessfully(String endpoint) {
        List<MovieTechnicianDTO> mockMovieTechnicianDTOList = Factory.mockMovieTechnicianDTOList(5);

        when(movieTechnicianService.getMovieTechniciansByMovie(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieTechnicianDTOList));

        given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieTechnicianDTOList.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(MovieTechnicianDTO::getRole)
                                .toArray(String[]::new))
                );
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void shouldReturnNoContentWhenNoTechniciansByMovie(String endpoint) {
        when(movieTechnicianService.getMovieTechniciansByMovie(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void testSaveTechniciansByMovie(String endpoint) {
        List<MovieTechnicianDTO> mockMovieTechnicianDTOList = Factory.mockMovieTechnicianDTOList(5);

        when(movieTechnicianService.saveTechnicians(any(), any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieTechnicianDTOList));

        given()
                .contentType("application/json")
                .body(mockMovieTechnicianDTOList)
                .when()
                .put(endpoint)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieTechnicianDTOList.size()))
                .body(
                        "id", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.containsInAnyOrder(mockMovieTechnicianDTOList.stream()
                                .map(MovieTechnicianDTO::getRole)
                                .toArray(String[]::new))
                )
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void shouldReturnNoContentWhenSavingEmptyTechnicians(String endpoint) {
        when(movieTechnicianService.saveTechnicians(any(), any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .contentType("application/json")
                .body(Collections.emptyList())
                .when()
                .put(endpoint)
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void testAddTechniciansByMovie(String endpoint) {
        List<MovieTechnicianDTO> mockMovieTechnicianDTOList = Factory.mockMovieTechnicianDTOList(6);

        when(movieTechnicianService.addTechnicians(any(), any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieTechnicianDTOList));

        given()
                .contentType("application/json")
                .body(mockMovieTechnicianDTOList.subList(0, 3))
                .when()
                .patch(endpoint)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieTechnicianDTOList.size()))
                .body(
                        "id", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(MovieTechnicianDTO::getRole)
                                .toArray(String[]::new))
                );
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void testAddTechniciansByMovieEmpty(String endpoint) {
        when(movieTechnicianService.addTechnicians(any(), any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .contentType("application/json")
                .body(Collections.emptySet())
                .when()
                .patch(endpoint)
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers/1", "/1/directors/1", "/1/assistant-directors/1", "/1/screenwriters/1",
                    "/1/composers/1", "/1/musicians/1", "/1/photographers/1", "/1/costume-designers/1",
                    "/1/set-designers/1", "/1/editors/1", "/1/casters/1", "/1/artists/1", "/1/sound-editors/1",
                    "/1/vfx-supervisors/1", "/1/sfx-supervisors/1", "/1/makeup-artists/1", "/1/hair-dressers/1",
                    "/1/stuntmen/1"
            }
    )
    void testRemoveTechnicianFromMovie(String endpoint) {
        List<MovieTechnicianDTO> mockMovieTechnicianDTOList = Factory.mockMovieTechnicianDTOList(6);

        when(movieTechnicianService.removeTechnician(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(mockMovieTechnicianDTOList));

        given()
                .when()
                .patch(endpoint)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(mockMovieTechnicianDTOList.size()))
                .body(
                        "id", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.id", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getId().intValue())
                                .toArray(Integer[]::new)),
                        "person.name", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getName())
                                .toArray(String[]::new)),
                        "person.photoFileName", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getPhotoFileName())
                                .toArray(String[]::new)),
                        "person.dateOfBirth", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfBirth().toString())
                                .toArray(String[]::new)),
                        "person.dateOfDeath", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(dto -> dto.getPerson().getDateOfDeath().toString())
                                .toArray(String[]::new)),
                        "role", Matchers.contains(mockMovieTechnicianDTOList.stream()
                                .map(MovieTechnicianDTO::getRole)
                                .toArray(String[]::new))
                )
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers/1", "/1/directors/1", "/1/assistant-directors/1", "/1/screenwriters/1",
                    "/1/composers/1", "/1/musicians/1", "/1/photographers/1", "/1/costume-designers/1",
                    "/1/set-designers/1", "/1/editors/1", "/1/casters/1", "/1/artists/1", "/1/sound-editors/1",
                    "/1/vfx-supervisors/1", "/1/sfx-supervisors/1", "/1/makeup-artists/1", "/1/hair-dressers/1",
                    "/1/stuntmen/1"
            }
    )
    void shouldReturn204IfTechniciansListIsEmpty(String endpoint) {
        when(movieTechnicianService.removeTechnician(any(), any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(Collections.emptyList()));

        given()
                .when()
                .patch(endpoint)
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/1/producers", "/1/directors", "/1/assistant-directors", "/1/screenwriters",
                    "/1/composers", "/1/musicians", "/1/photographers", "/1/costume-designers",
                    "/1/set-designers", "/1/editors", "/1/casters", "/1/artists", "/1/sound-editors",
                    "/1/vfx-supervisors", "/1/sfx-supervisors", "/1/makeup-artists", "/1/hair-dressers",
                    "/1/stuntmen"
            }
    )
    void shouldReturnNoContentWhenTechniciansAreRemoved(String endpoint) {
        when(movieTechnicianService.clearTechnicians(any(), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete(endpoint)
                .then()
                .statusCode(204)
                .body(Matchers.emptyOrNullString())
        ;
    }
}
