
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
public class MovieSetDesigner extends MovieTechnician {

    public static MovieSetDesigner build(Movie movie, Person person) {
        return
                MovieSetDesigner.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
