package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
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
import java.util.List;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "pays")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Country extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "nomFrFr";
    public static final List<String> ALLOWED_SORT_FIELDS = List.of("code", "alpha2", "alpha3", "nomEnGb", DEFAULT_SORT, "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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
    private Set<Movie> movies = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Person> persons = new HashSet<>();

    @PrePersist
    public void onCreate() {
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public Uni<Movie> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .invoke(
                                fetchedMovies -> {
                                    fetchedMovies.add(movie);
                                    log.info("Added movie {} to country {}", movie, this);
                                }
                        )
                        .replaceWith(movie)
                ;
    }

}
