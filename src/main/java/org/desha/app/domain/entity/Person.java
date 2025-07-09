package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person extends PanacheEntityBase implements Comparable<Person> {

    public static final String DEFAULT_PHOTO = "default-photo.jpg";
    public static final String DEFAULT_SORT = "name";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "dateOfBirth", "dateOfDeath", "moviesCount", "awardsCount", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "nom")
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

    @OneToMany(mappedBy = "person")
    private List<MovieActor> playedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieProducer> producedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieDirector> directedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieAssistantDirector> assistantDirectedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieScreenwriter> writtenMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieComposer> composedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieMusician> musicalMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MoviePhotographer> photographedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieCostumeDesigner> costumeDesignedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieSetDesigner> setDesignedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieEditor> editedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieCaster> castedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieArtist> artistMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieSoundEditor> soundEditedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieVfxSupervisor> vfxSupervisedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieSfxSupervisor> sfxSupervisedMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieMakeupArtist> makeupMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieHairDresser> hairStyledMovies = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<MovieStuntman> stuntMovies = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "lnk_pays_personne", joinColumns = @JoinColumn(name = "fk_personne"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(mappedBy = "personSet")
    private List<Award> awards = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "personne_type", joinColumns = @JoinColumn(name = "fk_personne"))
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Set<PersonType> types = new HashSet<>();

    @PrePersist
    private void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public static Person build(Long id, String name, String photoFileName, LocalDate dateOfBirth, LocalDate dateOfDeath, Set<PersonType> types, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                Person.builder()
                        .id(id)
                        .name(name.trim())
                        .photoFileName(Optional.ofNullable(photoFileName).orElse(DEFAULT_PHOTO))
                        .dateOfBirth(dateOfBirth)
                        .dateOfDeath(dateOfDeath)
                        .types(types)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
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
        this.types.add(type);
    }

    public void removeCountry(Long id) {
        countries.removeIf(country -> Objects.equals(country.getId(), id));
    }

    public void clearCountries() {
        countries.clear();
    }

    @Override
    public int compareTo(Person p) {
        return Comparator
                .comparing(Person::getName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(Person::getId, Comparator.nullsLast(Long::compareTo))
                .compare(this, p);
    }
}
