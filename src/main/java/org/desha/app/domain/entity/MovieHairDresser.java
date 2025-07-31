
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_coiffeur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieHairDresser extends MovieTechnician {

    public static MovieHairDresser build(Movie movie, Person person) {
        return
                MovieHairDresser.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
