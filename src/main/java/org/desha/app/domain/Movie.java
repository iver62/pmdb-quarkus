package org.desha.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Cacheable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "film", uniqueConstraints = {@UniqueConstraint(columnNames = {"titre", "titre_original"})})
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@NamedQueries({
        @NamedQuery(name = "Movie.searchByTitle", query = "from Movie where lower(title) LIKE lower(?1)")
})
public class Movie extends PanacheEntity {

    @NotEmpty(message = "Le titre ne peut pas être vide")
    @Column(name = "titre", nullable = false)
    private String title;

    @Column(name = "titre_original")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "date_sortie")
    @Temporal(TemporalType.DATE)
    private LocalDate releaseDate;

    @Column(name = "duree")
    private Long runningTime;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "box_office")
    private Long boxOffice;

    @Column(name = "chemin_affiche", unique = true)
    private String posterPath;

    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_producteur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_producteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> producers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_realisateur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_realisateur"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> directors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_scenariste", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_scenariste"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> screenwriters = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_musicien", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_musicien"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> musicians = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_photographes", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_photographe"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> photographers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_costumiers", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_costumier"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> costumiers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_decorateurs", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_decorateur"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> decorators = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_monteurs", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_monteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> editors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_casteurs", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_casteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Person> casting = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_pays", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    @Fetch(FetchMode.SELECT)
    private Set<Country> countries = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_genre", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_genre"))
    @Fetch(FetchMode.SELECT)
    private Set<Genre> genres = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    private Set<Award> awards = new HashSet<>();

    public static Movie build(MovieDTO movieDTO) {
        return Movie.builder()
                .title(movieDTO.getTitle())
                .originalTitle(movieDTO.getOriginalTitle())
                .releaseDate(movieDTO.getReleaseDate())
                .synopsis(movieDTO.getSynopsis())
                .runningTime(movieDTO.getRunningTime())
                .budget(movieDTO.getBudget())
                .boxOffice(movieDTO.getBoxOffice())
                .posterPath(movieDTO.getPosterPath())
                .countries(movieDTO.getCountries())
                .genres(movieDTO.getGenres())
                .creationDate(LocalDateTime.now())
                .build();
    }

    public static Uni<Set<PanacheEntityBase>> getByTitle(String title) {
        return
                Panache.withTransaction(() -> list("title", title).map(HashSet::new));
    }

    public static Uni<Set<PanacheEntityBase>> searchByTitle(String title) {
        return
                Panache.withTransaction(() -> list("#Movie.searchByTitle", "%" + title + "%").map(HashSet::new));
    }

    public Uni<Set<Person>> addProducers(Set<Person> personSet) {
        return
                Mutiny.fetch(producers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addDirectors(Set<Person> personSet) {
        return
                Mutiny.fetch(directors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addScreenwriters(Set<Person> personSet) {
        return
                Mutiny.fetch(screenwriters)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addMusicians(Set<Person> personSet) {
        return
                Mutiny.fetch(musicians)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addPhotographers(Set<Person> personSet) {
        return
                Mutiny.fetch(photographers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addCostumiers(Set<Person> personSet) {
        return
                Mutiny.fetch(costumiers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addDecorators(Set<Person> personSet) {
        return
                Mutiny.fetch(decorators)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> addEditors(Set<Person> personSet) {
        return
                Mutiny.fetch(editors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> saveCasting(Set<Person> personSet) {
        return
                Mutiny.fetch(casting)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(personSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Role>> addRole(Role role) {
        return
                Mutiny.fetch(roles)
                        .map(
                                fetchRoles -> {
                                    fetchRoles.add(role);
                                    return fetchRoles;
                                }
                        )
                ;
    }

    public Uni<Set<Role>> addRoles(Set<Role> roles) {
        return
                Mutiny.fetch(roles)
                        .map(
                                fetchRoles -> {
                                    fetchRoles.clear();
                                    fetchRoles.addAll(roles);
                                    return fetchRoles;
                                }
                        )
                ;
    }

    public Uni<Set<Genre>> addGenres(Set<Genre> genreSet) {
        return
                Mutiny.fetch(genres)
                        .map(
                                fetchGenres -> {
                                    fetchGenres.clear();
                                    fetchGenres.addAll(genreSet);
                                    return fetchGenres;
                                }
                        )
                ;
    }

    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .map(
                                fetchCountries -> {
                                    fetchCountries.clear();
                                    fetchCountries.addAll(countrySet);
                                    return fetchCountries;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> addAwards(Set<Award> awardSet) {
        return
                Mutiny.fetch(awards)
                        .map(
                                fetchAwards -> {
                                    fetchAwards.clear();
                                    fetchAwards.addAll(awardSet);
                                    return fetchAwards;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeProducer(Long id) {
        return
                Mutiny.fetch(producers)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeDirector(Long id) {
        return
                Mutiny.fetch(directors)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeScreenwriter(Long id) {
        return
                Mutiny.fetch(screenwriters)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeMusician(Long id) {
        return
                Mutiny.fetch(musicians)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removePhotographer(Long id) {
        return
                Mutiny.fetch(photographers)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeCostumier(Long id) {
        return
                Mutiny.fetch(costumiers)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeDecorator(Long id) {
        return
                Mutiny.fetch(decorators)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Person>> removeEditor(Long id) {
        return
                Mutiny.fetch(editors)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public void removeRole(Long id) {
        this.roles.removeIf(role -> Objects.equals(role.id, id));
    }

    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .map(
                                fetchCountries -> {
                                    fetchCountries.removeIf(country -> Objects.equals(country.id, id));
                                    return fetchCountries;
                                }
                        )
                ;
    }

    public Uni<Set<Genre>> removeGenre(Long id) {
        return
                Mutiny.fetch(genres)
                        .map(
                                fetchGenres -> {
                                    fetchGenres.removeIf(genre -> Objects.equals(genre.id, id));
                                    return fetchGenres;
                                }
                        )
                ;
    }

    public Uni<Set<Award>> removeAward(Long id) {
        return
                Mutiny.fetch(awards)
                        .map(
                                fetchAwards -> {
                                    fetchAwards.removeIf(award -> Objects.equals(award.id, id));
                                    return fetchAwards;
                                }
                        )
                ;
    }
}
