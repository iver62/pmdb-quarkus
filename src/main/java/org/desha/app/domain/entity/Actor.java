package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.service.PersonService;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

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
    private List<MovieActor> movieActors = new ArrayList<>();

    @ManyToMany
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
                        .id(personDTO.getId())
                        .name(personDTO.getName())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonService.DEFAULT_PHOTO)
                        .build()
                ;
    }

    public List<Movie> getMovies() {
        return Collections.emptyList();
    }

    public Uni<List<Movie>> addMovie(Movie movie) {
        return Uni.createFrom().item(Collections.emptyList());
    }

    public Uni<List<Movie>> removeMovie(Long id) {
        return Uni.createFrom().item(Collections.emptyList());
    }

    @Override
    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés"))
                        .invoke(fetchCountries -> {
                            if (Objects.nonNull(countrySet)) {
                                fetchCountries.addAll(countrySet);
                            }
                        })
                ;
    }

    @Override
    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(fetchCountries -> fetchCountries.removeIf(country -> Objects.equals(country.getId(), id)))
                ;
    }

    @Override
    public Uni<Set<Country>> clearCountries() {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(Set::clear)
                ;
    }

}
