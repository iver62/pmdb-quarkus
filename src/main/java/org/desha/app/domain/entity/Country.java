package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "pays")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Country extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "nomFrFr";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "alpha2", "alpha3", "nomEnGb", DEFAULT_SORT);

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

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    private Set<Movie> movies = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    private Set<Person> persons = new HashSet<>();

    public void updateMovie(CountryDTO countryDTO) {
        setCode(countryDTO.getCode());
        setAlpha2(countryDTO.getAlpha2());
        setAlpha3(countryDTO.getAlpha3());
        setNomEnGb(countryDTO.getNomEnGb());
        setNomFrFr(countryDTO.getNomFrFr());
    }

    /*public Uni<Movie> addMovie(Movie movie) {
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
    }*/

}
