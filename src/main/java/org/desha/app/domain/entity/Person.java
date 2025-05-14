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

    public static final String DEFAULT_SORT = "name";
    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", DEFAULT_SORT, "dateOfBirth", "dateOfDeath", "moviesCount", "creationDate", "lastUpdate");

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

    /**
     * Ajoute un ensemble de pays à la collection existante des pays.
     * <p>
     * Cette méthode récupère l'ensemble actuel des pays via Mutiny, puis ajoute les pays
     * spécifiés dans {@code countrySet} s'ils ne sont pas nuls.
     * Si la collection initiale de pays est nulle, une exception {@link IllegalStateException}
     * est levée.
     * </p>
     *
     * @param countrySet l'ensemble des pays à ajouter ; peut être nul (aucune action dans ce cas)
     * @return un {@link Uni} contenant l'ensemble mis à jour des pays
     * @throws IllegalStateException si la collection de pays n'est pas initialisée
     */
    public abstract Uni<Set<Country>> addCountries(Set<Country> countrySet);

    /**
     * Supprime un pays de la collection en fonction de son identifiant.
     * <p>
     * Cette méthode récupère l'ensemble actuel des pays via Mutiny, puis supprime
     * celui dont l'identifiant correspond à {@code id}. Si l'ensemble des pays n'est pas
     * initialisé (null), une exception {@link IllegalStateException} est levée.
     * </p>
     *
     * @param id l'identifiant du pays à supprimer
     * @return un {@link Uni} contenant l'ensemble mis à jour des pays
     * @throws IllegalStateException si l'ensemble des pays n'est pas initialisé
     */
    public abstract Uni<Set<Country>> removeCountry(Long id);

    /**
     * Supprime tous les pays de la collection.
     * <p>
     * Cette méthode vide l'ensemble des pays récupéré via {@code Mutiny.fetch(countries)}.
     * Si l'ensemble des pays n'est pas initialisé (null), une exception {@link IllegalStateException} est levée.
     * </p>
     *
     * @return un {@link Uni} contenant l'ensemble désormais vide des pays
     * @throws IllegalStateException si l'ensemble des pays n'est pas initialisé
     */
    public abstract Uni<Set<Country>> clearCountries();

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
