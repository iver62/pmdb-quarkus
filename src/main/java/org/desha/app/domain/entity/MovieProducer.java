package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "lnk_film_producteur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieProducer extends MovieTechnician {

    private static MovieProducer build(Long id, Movie movie, Person person, String role) {
        return
                MovieProducer.builder()
                        .id(id)
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

    public static MovieProducer of(Long id, Movie movie, Person person, String role) {
        return build(id, movie, person, role);
    }

    public static MovieProducer of(Movie movie, Person person, String role) {
        return build(null, movie, person, role);
    }

}
