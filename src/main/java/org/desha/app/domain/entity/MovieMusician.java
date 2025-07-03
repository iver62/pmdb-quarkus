
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_musicien")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieMusician extends MovieTechnician {

    private static MovieMusician build(Long id, Movie movie, Person person, String role) {
        return
                MovieMusician.builder()
                        .id(id)
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

    public static MovieMusician of(Long id, Movie movie, Person person, String role) {
        return build(id, movie, person, role);
    }

    public static MovieMusician of(Movie movie, Person person, String role) {
        return build(null, movie, person, role);
    }

}
