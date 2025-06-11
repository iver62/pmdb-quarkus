
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_realisateur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDirector extends MovieTechnician {

    public static MovieDirector of(Long id, Movie movie, Person person, String role) {
        return MovieDirector.builder()
                .id(id)
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

    public static MovieDirector of(Movie movie, Person person, String role) {
        return MovieDirector.builder()
                .movie(movie)
                .person(person)
                .role(role)
                .build();
    }

}
