package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@SuperBuilder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({
        @NamedQuery(name = "AbstractPerson.searchByName", query = "from AbstractPerson where lower(name) LIKE lower(?1)")
})
public abstract class Person extends PanacheEntity implements Comparable<Person> {

    @Column(name = "nom")
    private String name;

    @Column(name = "chemin_photo", unique = true)
    private String photoPath;

    @Column(name = "date_naissance")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;

    @Column(name = "date_deces")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfDeath;

    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_personne_pays", joinColumns = @JoinColumn(name = "fk_personne"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    @Fetch(FetchMode.SELECT)
    private Set<Country> countries = new HashSet<>();

        @JsonIgnore
    @OneToMany(mappedBy = "person", orphanRemoval = true)
    private Set<Award> awards = new HashSet<>();

    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .map(
                                fetchedCountries -> {
                                    fetchedCountries.addAll(countrySet);
                                    return fetchedCountries;
                                }
                        )
                ;
    }

    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .map(
                                countrySet -> {
                                    countrySet.removeIf(country -> Objects.equals(country.id, id));
                                    return countrySet;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> addAwards(Set<Award> awardSet) {
        return
                Mutiny.fetch(awards)
                        .map(
                                fetchedAwards -> {
                                    fetchedAwards.addAll(awardSet);
                                    return fetchedAwards;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> removeAward(Long id) {
        return
                Mutiny.fetch(awards)
                        .map(
                                awardSet -> {
                                    awardSet.removeIf(award -> Objects.equals(award.id, id));
                                    return awardSet;
                                }
                        )
                ;
    }

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
