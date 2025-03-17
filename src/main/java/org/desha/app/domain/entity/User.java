package org.desha.app.domain.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "username", "email", "firstName", "lastName");

    @Id
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "prenom")
    private String firstName;

    @Column(name = "nom")
    private String lastName;
}
