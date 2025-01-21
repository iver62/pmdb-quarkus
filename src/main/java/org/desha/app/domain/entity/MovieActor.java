package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "lnk_film_acteur")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActor extends PanacheEntity {

//    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_acteur")
    private Person actor;

    @Column(name = "nom")
    private String name;

    public static MovieActor build(Movie movie, Person person, String role) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .actor(person)
                        .name(role)
                        .build()
                ;
    }
}
