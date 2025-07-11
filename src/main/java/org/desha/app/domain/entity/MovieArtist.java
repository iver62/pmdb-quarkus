
package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@NoArgsConstructor
@Table(name = "lnk_film_artiste")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieArtist extends MovieTechnician {

    public static MovieArtist build(Movie movie, Person person, String role) {
        return
                MovieArtist.builder()
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .build()
                ;
    }

}
