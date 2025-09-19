package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.enums.PersonType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Table(name = "personne")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person extends PanacheEntityBase {

    public static final String DEFAULT_PHOTO = "default-photo.jpg";
    public static final String DEFAULT_SORT = "name";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "dateOfBirth", "dateOfDeath", "moviesCount", "awardsCount", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "photo")
    private String photoFileName;

    @Column(name = "date_naissance")
    private LocalDate dateOfBirth;

    @Column(name = "date_deces")
    private LocalDate dateOfDeath;

    @Column(name = "date_creation")
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @Embedded
    private PersonFilmography personFilmography;

    @ManyToMany
    @JoinTable(
            name = "lnk_pays_personne",
            joinColumns = @JoinColumn(name = "fk_personne"),
            inverseJoinColumns = @JoinColumn(name = "fk_pays")
    )
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(mappedBy = "personSet")
    private List<Award> awards = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "personne_type", joinColumns = @JoinColumn(name = "fk_personne"))
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Set<PersonType> types;

    @PrePersist
    private void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public static Person build(String name, String photoFileName) {
        return
                Person.builder()
                        .name(StringUtils.defaultString(name).trim())
                        .photoFileName(Optional.ofNullable(photoFileName).orElse(DEFAULT_PHOTO))
                        .types(new HashSet<>())
                        .build()
                ;
    }

    public void updatePerson(PersonDTO personDTO) {
        setName(personDTO.getName());
        setDateOfBirth(personDTO.getDateOfBirth());
        setDateOfDeath(personDTO.getDateOfDeath());
    }

    public void addCountries(Set<Country> countrySet) {
        countries.addAll(countrySet);
    }

    public void addType(PersonType type) {
        types.add(type);
    }

    public void removeCountry(Long id) {
        countries.removeIf(country -> Objects.equals(country.getId(), id));
    }

    public void clearCountries() {
        countries.clear();
    }

}
