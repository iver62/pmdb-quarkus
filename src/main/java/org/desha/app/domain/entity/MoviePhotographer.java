
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_photographe")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MoviePhotographer extends MovieTechnician {

    public static MoviePhotographer build(Movie movie, Person person, String role) {
        return
                MoviePhotographer.builder()
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

}
