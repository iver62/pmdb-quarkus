package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "scenariste")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Screenwriter extends Person {

    @JsonIgnore
    @ManyToMany(mappedBy = "screenwriters")
    @Fetch(FetchMode.SELECT)
    private List<Movie> movies = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_pays_scenariste", joinColumns = @JoinColumn(name = "fk_scenariste"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @Builder
    public Screenwriter(Long id, String name, String photoFileName) {
        super();
        this.id = id;
        this.name = name;
        this.photoFileName = photoFileName;
    }

    public static Screenwriter fromDTO(PersonDTO personDTO) {
        return
                Screenwriter.builder()
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

}
