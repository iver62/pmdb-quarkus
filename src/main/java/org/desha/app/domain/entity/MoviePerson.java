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
import org.hibernate.annotations.Subselect;

@Entity
@Getter
@Builder
@Immutable
@Table(name = "film_personne_vue")
@Subselect("SELECT * FROM film_personne_vue")
@NoArgsConstructor
@AllArgsConstructor
public class MoviePerson {

    @Id
    @Column(name = "fk_film")
    Long movieId;

    @Column(name = "fk_personne")
    Long personId;
}
