package org.desha.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.time.Year;

@Entity
@Getter
@Setter
@Table(name = "recompense")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Award extends PanacheEntity {

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "annee")
    private Year year;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fk_film")
    private Movie movie;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fk_person")
    private Person person;

}
