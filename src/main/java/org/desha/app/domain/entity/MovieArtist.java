
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_artiste")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieArtist extends MovieTechnician {

    private static MovieArtist build(Long id, Movie movie, Person person, String role) {
        return
                MovieArtist.builder()
                        .id(id)
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

    public static MovieArtist of(Long id, Movie movie, Person person, String role) {
        return build(id, movie, person, role);
    }

    public static MovieArtist of(Movie movie, Person person, String role) {
        return build(null, movie, person, role);
    }

}
