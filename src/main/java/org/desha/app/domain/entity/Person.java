package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Getter
@Setter
@MappedSuperclass
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Person extends PanacheEntityBase implements Comparable<Person> {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "dateOfBirth", "dateOfDeath", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @Column(name = "nom")
    protected String name;

    @Column(name = "photo")
    protected String photoFileName;

    @Column(name = "date_naissance")
    protected LocalDate dateOfBirth;

    @Column(name = "date_deces")
    protected LocalDate dateOfDeath;

    @Column(name = "date_creation")
    protected LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    protected LocalDateTime lastUpdate;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public abstract List<Movie> getMovies();

    public abstract Uni<List<Movie>> addMovie(Movie movie);

    public abstract Uni<List<Movie>> removeMovie(Long id);

    public abstract Set<Country> getCountries();

    public abstract void setCountries(Set<Country> countrySet);

    public abstract void setMovies(List<Movie> movieList);

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
