package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "lnk_film_acteur")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActor extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_acteur")
    private Actor actor;

    @NotEmpty(message = "Le rôle ne peut pas être vide")
    @Column(name = "role", nullable = false)
    private String role;

    @Builder
    public MovieActor(Long id, Movie movie, Actor actor, String role) {
        super();
        this.id = id;
        this.movie = movie;
        this.actor = actor;
        this.role = role;
    }

    public static MovieActor build(Long id, Movie movie, Actor actor, String role) {
        return
                MovieActor.builder()
                        .id(id)
                        .movie(movie)
                        .actor(actor)
                        .role(role)
                        .build()
                ;
    }

    public static MovieActor build(Movie movie, Actor actor, String role) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .role(role)
                        .build()
                ;
    }

}
