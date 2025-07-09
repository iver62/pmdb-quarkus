
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

    public static MovieMusician build(Movie movie, Person person, String role) {
        return
                MovieMusician.builder()
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

}
