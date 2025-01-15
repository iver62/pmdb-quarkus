package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@SuperBuilder
@Getter
@Setter
@Table(name = "maquilleur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakeupArtist extends Person {

    @JsonIgnore
    @ManyToMany(mappedBy = "makeupArtists")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies = new HashSet<>();

    public Uni<Set<Movie>> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    /**
     * Retire un film de la liste des films
     *
     * @param id l'identifiant du film
     * @return la liste des films
     */
    public Uni<Set<Movie>> removeMovie(Long id) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }


}
