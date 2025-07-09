package org.desha.app.domain.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.util.*;

@Entity
@Getter
@Builder
@Immutable
@Table(name = "utilisateurs_vue")
@Subselect("SELECT * FROM utilisateurs_vue")
@NoArgsConstructor
@AllArgsConstructor
public class User extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "username";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "lastname", "email", "emailVerified", "moviesCount");

    @Id
    private UUID id;

    @Column(name = "pseudo", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "email_verifie")
    private Boolean emailVerified;

    @Column(name = "nom")
    private String lastname;

    @Column(name = "prenom")
    private String firstname;

    @OneToMany(mappedBy = "user")
    private List<Movie> movies = new ArrayList<>();

    public static User build(UUID id, String username, String email, Boolean emailVerified, String lastname, String firstname) {
        return
                User.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .emailVerified(emailVerified)
                        .lastname(lastname)
                        .firstname(firstname)
                        .build()
                ;
    }

    public int getNumberOfMovies() {
        return Objects.nonNull(movies) ? movies.size() : 0;
    }
}
