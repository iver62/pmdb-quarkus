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

    public static MovieProducer build(Movie movie, Person person, String role) {
        return
                MovieProducer.builder()
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

}
