
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_decorateur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDecorator extends MovieTechnician {

    public static MovieDecorator of(Long id, Movie movie, Person person, String role) {
        return MovieDecorator.builder()
                .id(id)
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

    public static MovieDecorator of(Movie movie, Person person, String role) {
        return MovieDecorator.builder()
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

}
