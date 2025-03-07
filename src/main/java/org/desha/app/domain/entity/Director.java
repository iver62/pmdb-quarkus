package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.service.PersonService;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

@Slf4j
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "realisateur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Director extends Person {

    @JsonIgnore
    @ManyToMany(mappedBy = "directors")
    @Fetch(FetchMode.SELECT)
    private List<Movie> movies = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_pays_realisateur", joinColumns = @JoinColumn(name = "fk_realisateur"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @Builder
    public Director(Long id, String name, String photoFileName) {
        super();
        this.id = id;
        this.name = name;
        this.photoFileName = photoFileName;
    }

    public static Director fromDTO(PersonDTO personDTO) {
        return
                Director.builder()
                        .name(personDTO.getName())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonService.DEFAULT_PHOTO)
                        .build()
                ;
    }

    public Uni<List<Movie>> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieList -> {
                                    movieList.add(movie);
                                    return movieList;
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
    public Uni<List<Movie>> removeMovie(Long id) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieList -> {
                                    movieList.removeIf(movie -> Objects.equals(movie.id, id));
                                    return movieList;
                                }
                        )
                ;
    }

    /*public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
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
    }*/
}
