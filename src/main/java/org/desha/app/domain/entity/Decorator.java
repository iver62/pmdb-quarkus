package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.service.PersonServiceImpl;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "decorateur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Decorator extends Person {

    @JsonIgnore
    @ManyToMany(mappedBy = "decorators")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_pays_decorateur", joinColumns = @JoinColumn(name = "fk_decorateur"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

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

    @Builder
    public Decorator(Long id, String name, String photoFileName) {
        super();
        this.id = id;
        this.name = name;
        this.photoFileName = photoFileName;
    }

    public static Decorator fromDTO(PersonDTO personDTO) {
        return
                Decorator.builder()
                        .name(personDTO.getName())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonServiceImpl.DEFAULT_PHOTO)
                        .build()
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
