
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_maquilleur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieMakeupArtist extends MovieTechnician {

    public static MovieMakeupArtist of(Long id, Movie movie, Person person, String role) {
        return MovieMakeupArtist.builder()
                .id(id)
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

    public static MovieMakeupArtist of(Movie movie, Person person, String role) {
        return MovieMakeupArtist.builder()
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

}
