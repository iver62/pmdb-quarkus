
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_cascadeur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieStuntman extends MovieTechnician {

    public static MovieStuntman build(Movie movie, Person person) {
        return
                MovieStuntman.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
