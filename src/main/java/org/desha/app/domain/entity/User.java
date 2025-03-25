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

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@Immutable
@Table(name = "utilisateurs")
@NoArgsConstructor
@AllArgsConstructor
public class User extends PanacheEntityBase {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "username", "email", "emailVerified", "name");

    @Id
    private UUID id;

    @Column(name = "pseudo", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "email_verifie")
    private Boolean emailVerified;

    @Column(name = "nom")
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Movie> movies;

    public static User fromDTO(UserDTO userDTO) {
        return
                User.builder()
                        .id(userDTO.getId())
                        .username(userDTO.getUsername())
                        .email(userDTO.getEmail())
                        .emailVerified(userDTO.getEmailVerified())
                        .name(userDTO.getName())
                        .build();
    }
}
