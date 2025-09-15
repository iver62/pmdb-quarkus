package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "lnk_film_acteur")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieActor extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "movie.title";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "movie.originalTitle", "movie.releaseDate", "role");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_acteur")
    private Person person;

    @NotEmpty(message = "Le rôle ne peut pas être vide")
    @Column(name = "role", nullable = false)
    private String role;

    @NotNull(message = "Le rang ne peut pas être nul")
    @Column(name = "rang", nullable = false)
    private Integer rank;

    public static MovieActor build(Movie movie, Person person) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .person(person)
                        .build()
                ;
    }

    public static MovieActor build(Movie movie, Person person, String role, Integer rank) {
        return
                MovieActor.builder()
                        .movie(movie)
                        .person(person)
                        .role(role)
                        .rank(rank)
                        .build()
                ;
    }

    public String toString() {
        return id + " / " + person.getId() + ": " + person.getName() + " -> " + role + " (" + rank + ")";
    }
}
