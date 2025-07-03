
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_assistant_realisateur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieAssistantDirector extends MovieTechnician {

    private static MovieAssistantDirector build(Long id, Movie movie, Person person, String role) {
        return
                MovieAssistantDirector.builder()
                        .id(id)
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

    public static MovieAssistantDirector of(Long id, Movie movie, Person person, String role) {
        return build(id, movie, person, role);
    }

    public static MovieAssistantDirector of(Movie movie, Person person, String role) {
        return build(null, movie, person, role);
    }

}
