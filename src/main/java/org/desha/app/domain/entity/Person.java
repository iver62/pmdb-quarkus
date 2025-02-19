package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({
        @NamedQuery(name = "Person.searchByName", query = "from Person where lower(name) LIKE lower(?1)")
})
public abstract class Person extends PanacheEntity implements Comparable<Person> {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "dateOfBirth", "dateOfDeath", "creationDate", "lastUpdate");

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

    @JsonIgnore
    @OneToMany(mappedBy = "person", orphanRemoval = true)
    private Set<Award> awards = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
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

    public abstract Set<Movie> getMovies();

    public abstract Uni<Set<Movie>> addMovie(Movie movie);

    public abstract Uni<Set<Movie>> removeMovie(Long id);

    public abstract Set<Country> getCountries();

    public abstract void setCountries(Set<Country> countrySet);

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
