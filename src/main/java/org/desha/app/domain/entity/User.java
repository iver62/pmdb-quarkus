package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.desha.app.domain.dto.UserDTO;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Movie> movies = new ArrayList<>();

    public static User fromDTO(UserDTO userDTO) {
        return
                User.builder()
                        .id(userDTO.getId())
                        .username(userDTO.getUsername())
                        .email(userDTO.getEmail())
                        .emailVerified(userDTO.getEmailVerified())
                        .lastname(userDTO.getLastname())
                        .firstname(userDTO.getFirstname())
                        .build();
    }
}
