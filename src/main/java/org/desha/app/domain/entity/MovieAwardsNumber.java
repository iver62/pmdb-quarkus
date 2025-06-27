package org.desha.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Entity
@Immutable
@Subselect("SELECT * FROM films_nombre_recompenses_vue")
public class MovieAwardsNumber {

    @Id
    @Column(name = "fk_film")
    private Long movieId;

    @Column(name = "nombre_recompenses")
    private Long awardsNumber;
}
