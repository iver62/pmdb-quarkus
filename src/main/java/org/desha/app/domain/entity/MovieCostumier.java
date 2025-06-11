
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_costumier")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieCostumier extends MovieTechnician {

    public static MovieCostumier of(Long id, Movie movie, Person person, String role) {
        return MovieCostumier.builder()
                .id(id)
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

    public static MovieCostumier of(Movie movie, Person person, String role) {
        return MovieCostumier.builder()
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

}
