package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "pays")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Country extends PanacheEntity {

    @Column(name = "code", nullable = false, unique = true)
    private int code;

    @Column(name = "alpha2")
    private String alpha2;

    @Column(name = "alpha3")
    private String alpha3;

    @Column(name = "nom_en_gb")
    private String nomEnGb;

    @Column(name = "nom_fr_fr")
    private String nomFrFr;

    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Person> persons;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies = new HashSet<>();

    public Uni<Movie> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieSet -> {
                                    movies.add(movie);
                                    return movie;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addPerson(Person person) {
        return
                Mutiny.fetch(persons)
                        .map(
                                people -> {
                                    people.add(person);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovie(Long id) {
        return
                Mutiny.fetch(movies)
                        .map(
                                fetchMovies -> {
                                    fetchMovies.removeIf(movie -> Objects.equals(movie.id, id));
                                    return fetchMovies;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removePerson(Long id) {
        return
                Mutiny.fetch(persons)
                        .map(
                                fetchPersons -> {
                                    fetchPersons.removeIf(person -> Objects.equals(person.id, id));
                                    return fetchPersons;
                                }
                        )
                ;
    }
}
