package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "lnk_film_acteur")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActor extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_acteur")
    private Actor actor;

    @NotEmpty(message = "Le rôle ne peut pas être vide")
    @Column(name = "role", nullable = false)
    private String role;

    @NotNull(message = "Le rang ne peut pas être nul")
    @Column(name = "rang", nullable = false)
    private Integer rank;

    public static MovieActor build(Long id, Movie movie, Actor actor, String role, Integer rank) {
        return
                MovieActor.builder()
                        .id(id)
                        .movie(movie)
                        .actor(actor)
                        .role(role)
                        .rank(rank)
                        .build()
                ;
    }

    public static MovieActor build(Movie movie, Actor actor, String role, Integer rank) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .role(role)
                        .rank(rank)
                        .build()
                ;
    }
}
