
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_casteur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieCaster extends MovieTechnician {

    public static MovieCaster build(Movie movie, Person person) {
        return
                MovieCaster.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
