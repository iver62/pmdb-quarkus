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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "personne")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NamedQueries({
        @NamedQuery(name = "Person.searchByName", query = "from Person where lower(name) LIKE lower(?1)")
})
public class Person extends PanacheEntity implements Comparable<Person> {

    @Column(name = "nom")
    private String name;

//    @Column(name = "nom de famille")
//    private String lastName;
//
//    @Column(name = "prenom")
//    private String firstName;
//
//    @Column(name = "deuxieme_prenom")
//    private String secondName;
//
//    @Column(name = "troisieme_prenom")
//    private String thirdName;
//
//    @Column(name = "pseudo")
//    private String pseudo;

    @Column(name = "chemin_photo", unique = true)
    private String photoPath;

    @Column(name = "date_naissance")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;

    @Column(name = "date_deces")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfDeath;

    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_personne_pays", joinColumns = @JoinColumn(name = "fk_personne"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    @Fetch(FetchMode.SELECT)
    private Set<Country> countries;

    @JsonIgnore
    @ManyToMany(mappedBy = "producers")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsProducer = new HashSet<>();

    /*@JsonIgnore
    @ManyToMany(mappedBy = "directors")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsDirector = new HashSet<>();*/

    @JsonIgnore
    @ManyToMany(mappedBy = "screenwriters")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsScreenWriter = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "musicians")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsMusician = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "photographers")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsPhotographer = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "costumiers")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsCostumier = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "decorators")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsDecorator = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "editors")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsEditor = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "casters")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> moviesAsCaster = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "actor", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Fetch(FetchMode.SELECT)
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "person", orphanRemoval = true)
    private Set<Award> awards = new HashSet<>();

    public Uni<Set<Movie>> addMovieAsProducer(Movie movie) {
        return
                Mutiny.fetch(moviesAsProducer)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    /*public Uni<Set<Movie>> addMovieAsDirector(Movie movie) {
        return
                Mutiny.fetch(moviesAsDirector)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }*/

    public Uni<Set<Movie>> addMovieAsScreenwriter(Movie movie) {
        return
                Mutiny.fetch(moviesAsScreenWriter)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> addMovieAsMusician(Movie movie) {
        return
                Mutiny.fetch(moviesAsMusician)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> addMovieAsPhotographer(Movie movie) {
        return
                Mutiny.fetch(moviesAsPhotographer)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> addMovieAsCostumier(Movie movie) {
        return
                Mutiny.fetch(moviesAsCostumier)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> addMovieAsDecorator(Movie movie) {
        return
                Mutiny.fetch(moviesAsDecorator)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> addMovieAsEditor(Movie movie) {
        return
                Mutiny.fetch(moviesAsEditor)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> saveMovieAsCaster(Movie movie) {
        return
                Mutiny.fetch(moviesAsCaster)
                        .map(
                                movieSet -> {
                                    movieSet.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Role>> addRole(Role role) {
        return
                Mutiny.fetch(roles)
                        .map(
                                roleSet -> {
                                    roleSet.add(role);
                                    return roleSet;
                                }
                        )
                ;
    }

    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .map(
                                fetchedCountries -> {
                                    fetchedCountries.addAll(countrySet);
                                    return fetchedCountries;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> addAwards(Set<Award> awardSet) {
        return
                Mutiny.fetch(awards)
                        .map(
                                fetchedAwards -> {
                                    fetchedAwards.addAll(awardSet);
                                    return fetchedAwards;
                                }
                        )
                ;
    }

    /**
     * Retire un film de la liste des films en tant que producteur
     *
     * @param id l'identifiant du film
     * @return la liste des films en tant que producteur
     */
    public Uni<Set<Movie>> removeMovieAsProducer(Long id) {
        return
                Mutiny.fetch(moviesAsProducer)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    /*public Uni<Set<Movie>> removeMovieAsDirector(Long id) {
        return
                Mutiny.fetch(moviesAsDirector)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }*/

    public Uni<Set<Movie>> removeMovieAsScreenwriter(Long id) {
        return
                Mutiny.fetch(moviesAsScreenWriter)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsMusician(Long id) {
        return
                Mutiny.fetch(moviesAsMusician)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsPhotographer(Long id) {
        return
                Mutiny.fetch(moviesAsPhotographer)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsCostumier(Long id) {
        return
                Mutiny.fetch(moviesAsCostumier)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsDecorator(Long id) {
        return
                Mutiny.fetch(moviesAsDecorator)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovieAsEditor(Long id) {
        return
                Mutiny.fetch(moviesAsEditor)
                        .map(
                                movieSet -> {
                                    movieSet.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .map(
                                countrySet -> {
                                    countrySet.removeIf(country -> Objects.equals(country.id, id));
                                    return countrySet;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> removeAward(Long id) {
        return
                Mutiny.fetch(awards)
                        .map(
                                awardSet -> {
                                    awardSet.removeIf(award -> Objects.equals(award.id, id));
                                    return awardSet;
                                }
                        )
                ;
    }

    public void removeRole(Long id) {
        this.roles.removeIf(role -> Objects.equals(role.id, id));
    }

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
