package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@Table(name = "lnk_film_acteur")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActor extends PanacheEntityBase implements Comparable<MovieActor> {

    public static final String DEFAULT_SORT = "movie.title";
    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", DEFAULT_SORT, "movie.releaseDate", "role");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_acteur")
    private Person actor;

    @NotEmpty(message = "Le rôle ne peut pas être vide")
    @Column(name = "role", nullable = false)
    private String role;

    @NotNull(message = "Le rang ne peut pas être nul")
    @Column(name = "rang", nullable = false)
    private Integer rank;

    public static MovieActor of(Long id, Movie movie, Person actor, String role, Integer rank) {
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

    public static MovieActor of(Movie movie, Person actor, String role, Integer rank) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .role(role)
                        .rank(rank)
                        .build()
                ;
    }

    public String toString() {
        return id + " / " + actor.getId() + ": " + actor.getName() + " -> " + role + " (" + rank + ")";
    }

    @Override
    public int compareTo(MovieActor o) {
        return rank.compareTo(o.getRank());
    }
}
