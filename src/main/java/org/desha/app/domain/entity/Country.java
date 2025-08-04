package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "pays")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Entité représentant un pays dans le système")
public class Country extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "nomFrFr";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "alpha2", "alpha3", "nomEnGb", DEFAULT_SORT);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Schema(description = "Identifiant unique du pays", example = "1")
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    @Schema(description = "Code numérique unique du pays selon la norme ISO 3166-1", example = "250", required = true)
    private int code;

    @Column(name = "alpha2")
    @Schema(description = "Code alpha-2 du pays selon la norme ISO 3166-1", example = "FR")
    private String alpha2;

    @Column(name = "alpha3")
    @Schema(description = "Code alpha-3 du pays selon la norme ISO 3166-1", example = "FRA")
    private String alpha3;

    @Column(name = "nom_en_gb")
    @Schema(description = "Nom du pays en anglais (GB)", example = "France")
    private String nomEnGb;

    @Column(name = "nom_fr_fr")
    @Schema(description = "Nom du pays en français (FR)", example = "France")
    private String nomFrFr;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    private Set<Movie> movies = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    private Set<Person> persons = new HashSet<>();

}
