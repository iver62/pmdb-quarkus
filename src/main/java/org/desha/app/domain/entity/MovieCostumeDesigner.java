
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
public class MovieCostumeDesigner extends MovieTechnician {

    public static MovieCostumeDesigner build(Movie movie, Person person) {
        return
                MovieCostumeDesigner.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
