
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

    private static MovieSfxSupervisor build(Long id, Movie movie, Person person, String role) {
        return
                MovieSfxSupervisor.builder()
                        .id(id)
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

    public static MovieSfxSupervisor of(Long id, Movie movie, Person person, String role) {
        return build(id, movie, person, role);
    }

    public static MovieSfxSupervisor of(Movie movie, Person person, String role) {
        return build(null, movie, person, role);
    }

}
