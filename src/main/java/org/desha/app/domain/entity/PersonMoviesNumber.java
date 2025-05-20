package org.desha.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Builder
@Immutable
@Table(name = "personne_nombre_films")
@NoArgsConstructor
@AllArgsConstructor
public class PersonMoviesNumber {

    @Id
    @Column(name = "fk_personne")
    Long personId;

    @Column(name = "nombre_films")
    Long moviesNumber;
}
