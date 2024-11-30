package org.desha.app;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class MovieEndPointTest {

    @Test
    public void testCreateMovie() {
        given()
                .when()
                .body("{\n" +
                        "    \"title\": \"Seven\",\n" +
                        "    \"originalTitle\": \"Se7en\",\n" +
                        "    \"synopsis\": \"Deux policiers, William Somerset et David Mills, sont chargés d'une enquête criminelle concernant un tueur en série psychopathe, lequel planifie méthodiquement ses meurtres en fonction des sept péchés capitaux qui sont : la gourmandise, l'avarice, la paresse, la luxure, l'orgueil, l'envie et la colère.\",\n" +
                        "    \"releaseDate\": \"1995-09-22\",\n" +
                        "    \"duration\": 127,\n" +
                        "    \"budget\": 30000000,\n" +
                        "    \"boxOffice\": 327311000,\n" +
                        "    \"posterPath\": \"seven-path.png\"\n" +
                        "}")
                .contentType("application/json")
                .post("/movies")
                .then()
                .statusCode(201)
                .body(
                        containsString("\"id\":"),
                        containsString("\"title\":\"Seven\""),
                        containsString("\"originalTitle\":\"Se7en\""),
                        containsString("\"synopsis\":\"Deux policiers, William Somerset et David Mills, sont chargés d'une enquête criminelle concernant un tueur en série psychopathe, lequel planifie méthodiquement ses meurtres en fonction des sept péchés capitaux qui sont : la gourmandise, l'avarice, la paresse, la luxure, l'orgueil, l'envie et la colère.\""),
                        containsString("\"releaseDate\":\"1995-09-22\""),
                        containsString("\"duration\":127"),
                        containsString("\"budget\":30000000"),
                        containsString("\"boxOffice\":327311000"),
                        containsString("\"posterPath\":\"seven-path.png\"")
                )
        ;
    }

    @Test
    public void givenMovies_whenGetMovies_thenReturnAllMovies() {
        given().when()
                .get("/movies")
                .then()
                .statusCode(200)
        ;
    }
}
