
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_specialiste_effets_speciaux")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieSfxSupervisor extends MovieTechnician {

    public static MovieSfxSupervisor build(Movie movie, Person person) {
        return
                MovieSfxSupervisor.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

}
