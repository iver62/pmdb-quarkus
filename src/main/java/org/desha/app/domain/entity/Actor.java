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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "acteur")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Actor extends Person {

    @JsonIgnore
    @OneToMany(mappedBy = "actor")
    @Fetch(FetchMode.SELECT)
    private Set<MovieActor> movieActors = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_pays_acteur", joinColumns = @JoinColumn(name = "fk_acteur"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @Builder
    public Actor(Long id, String name, String photoFileName) {
        super();
        this.id = id;
        this.name = name;
        this.photoFileName = photoFileName;
    }

    public static Actor fromDTO(PersonDTO personDTO) {
        return
                Actor.builder()
                        .name(personDTO.getName())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonServiceImpl.DEFAULT_PHOTO)
                        .build()
                ;
    }

    public Set<Movie> getMovies() {
        return Collections.emptySet();
    }

    public Uni<Set<Movie>> addMovie(Movie movie) {
        return Uni.createFrom().item(Collections.emptySet());
    }

    public Uni<Set<Movie>> removeMovie(Long id) {
        return Uni.createFrom().item(Collections.emptySet());
    }

}
