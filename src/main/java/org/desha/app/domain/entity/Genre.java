package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.AuditGenreListener;
import org.desha.app.domain.dto.GenreDTO;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "genre")
@EntityListeners(AuditGenreListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Genre extends PanacheEntityBase {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @NotEmpty(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false, unique = true)
    private String name;

    @Column(name = "date_creation")
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(mappedBy = "genres")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies;

    public static Genre fromDTO(GenreDTO genreDTO) {
        return
                Genre.builder()
                        .name(StringUtils.capitalize(genreDTO.getName()))
                        .build()
                ;
    }

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public static Uni<Long> count(String name) {
        return count("LOWER(name) LIKE LOWER(?1)", "%" + name + "%");
    }

    public static Uni<Genre> getById(Long id) {
        return findById(id);
    }

    public static Uni<List<Genre>> getAll() {
        return listAll();
    }

    public static Uni<List<Movie>> getAllMovies(Long id, String sort, Sort.Direction direction, String title) {
        return
                Movie.find(
                                "SELECT m FROM Movie m JOIN m.genres g WHERE g.id = ?1 AND LOWER(m.title) LIKE LOWER(?2)",
                                Sort.by(sort, direction),
                                id, MessageFormat.format("%{0}%", title)
                        )
                        .list();
    }

    public Uni<Set<Movie>> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .map(
                                movieSet -> {
                                    movies.add(movie);
                                    return movieSet;
                                }
                        )
                ;
    }

    public Uni<Set<Movie>> removeMovie(Long id) {
        return
                Mutiny.fetch(movies)
                        .map(
                                fetchMovies -> {
                                    fetchMovies.removeIf(movie -> Objects.equals(movie.id, id));
                                    return fetchMovies;
                                }
                        )
                ;
    }

    public static Uni<Genre> create(GenreDTO genreDTO) {
        return Panache.withTransaction(() -> Genre.fromDTO(genreDTO).persist());
    }

    public static Uni<Genre> update(Long id, GenreDTO genreDTO) {
        return
                Panache
                        .withTransaction(() ->
                                getById(id)
                                        .onItem().ifNotNull()
                                        .invoke(entity -> entity.setName(genreDTO.getName()))
                        )
                ;
    }

    public static Uni<Boolean> deleteGenre(Long id) {
        return Panache.withTransaction(() -> deleteById(id));
    }

}
