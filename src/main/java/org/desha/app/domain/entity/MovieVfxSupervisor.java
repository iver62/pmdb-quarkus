
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_specialiste_effets_visuels")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieVfxSupervisor extends MovieTechnician {

    public static MovieVfxSupervisor build(Movie movie, Person person) {
        return MovieVfxSupervisor.builder()
                .movie(movie)
                .person(person)
                .build();
    }

}
