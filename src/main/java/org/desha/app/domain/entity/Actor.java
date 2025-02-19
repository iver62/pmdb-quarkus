package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "acteur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Actor extends Person {

    @JsonIgnore
    @OneToMany(mappedBy = "actor")
    @Fetch(FetchMode.SELECT)
    private Set<MovieActor> movieActors = new HashSet<>();

    /*@JsonIgnore
    @ManyToMany(mappedBy = "directors")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies = new HashSet<>();*/

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_pays_acteur", joinColumns = @JoinColumn(name = "fk_acteur"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    @Fetch(FetchMode.SELECT)
    private Set<Country> countries = new HashSet<>();

    @Builder
    public Actor(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Set<Movie> getMovies() {
        return Collections.emptySet();
    }

    public Uni<Set<Movie>> addMovie(Movie movie) {
        return Uni.createFrom().item(Collections.emptySet());
    }

    public Uni<Set<Movie>> removeMovie(Long id) {
        return Uni.createFrom().item(Collections.emptySet());
    }

}
