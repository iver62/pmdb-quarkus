package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.PersonType;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.service.PersonService;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public static final String DEFAULT_SORT = "name";
    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", DEFAULT_SORT, "dateOfBirth", "dateOfDeath", "moviesCount", "awardsCount", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @Column(name = "nom")
    protected String name;

    @Column(name = "photo")
    protected String photoFileName;

    @Column(name = "date_naissance")
    private LocalDate dateOfBirth;

    @Column(name = "date_deces")
    private LocalDate dateOfDeath;

    @Column(name = "date_creation")
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @OneToMany(mappedBy = "actor")
    private List<MovieActor> playedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "producers")
    private List<Movie> producedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "directors")
    private List<Movie> directedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "screenwriters")
    private List<Movie> writtenMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "dialogueWriters")
    private List<Movie> dialogueWrittenMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "musicians")
    private List<Movie> musicalMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "photographers")
    private List<Movie> photographedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "costumiers")
    private List<Movie> costumeMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "decorators")
    private List<Movie> decoratedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "editors")
    private List<Movie> editedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "casters")
    private List<Movie> castedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "artDirectors")
    private List<Movie> artDirectedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "soundEditors")
    private List<Movie> soundEditedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "visualEffectsSupervisors")
    private List<Movie> vfxSupervisedMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "makeupArtists")
    private List<Movie> makeupMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "hairDressers")
    private List<Movie> hairStyledMovies = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "stuntmen")
    private List<Movie> stuntMovies = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "lnk_pays_personne", joinColumns = @JoinColumn(name = "fk_personne"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(mappedBy = "personSet")
    private Set<Award> awardSet = new HashSet<>();

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

    public static Person build(PersonDTO personDTO) {
        return
                Person.builder()
                        .id(personDTO.getId())
                        .name(personDTO.getName().trim())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonService.DEFAULT_PHOTO)
                        .dateOfBirth(personDTO.getDateOfBirth())
                        .dateOfDeath(personDTO.getDateOfDeath())
                        .types(personDTO.getTypes())
                        .creationDate(personDTO.getCreationDate())
                        .lastUpdate(personDTO.getLastUpdate())
                        .build()
                ;
    }

    @JsonIgnore
    public Uni<Set<Movie>> getAllRelatedMovies() {
        return
                Uni.createFrom().item(new HashSet<Movie>())
                        .chain(set -> Mutiny.fetch(playedMovies).map(movieActors -> movieActors.stream().map(MovieActor::getMovie).toList()).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(producedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(directedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(writtenMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(dialogueWrittenMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(musicalMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(photographedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(costumeMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(decoratedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(editedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(castedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(artDirectedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(soundEditedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(vfxSupervisedMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(makeupMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(hairStyledMovies).invoke(set::addAll).replaceWith(set))
                        .chain(set -> Mutiny.fetch(stuntMovies).invoke(set::addAll).replaceWith(set))
                ;
    }

    public Uni<Set<PersonType>> addType(PersonType type) {
        return
                Mutiny.fetch(types)
                        .map(
                                personTypes -> {
                                    personTypes.add(type);
                                    return personTypes;
                                }
                        )
                ;
    }

    /**
     * Ajoute un ensemble de pays à la collection existante des pays.
     * <p>
     * Cette méthode récupère l'ensemble actuel des pays via Mutiny, puis ajoute les pays
     * spécifiés dans {@code countrySet} s'ils ne sont pas nuls.
     * Si la collection initiale de pays est nulle, une exception {@link IllegalStateException}
     * est levée.
     * </p>
     *
     * @param countrySet l'ensemble des pays à ajouter ; peut être nul (aucune action dans ce cas)
     * @return un {@link Uni} contenant l'ensemble mis à jour des pays
     * @throws IllegalStateException si la collection de pays n'est pas initialisée
     */
    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés"))
                        .invoke(fetchCountries -> {
                            if (Objects.nonNull(countrySet)) {
                                fetchCountries.addAll(countrySet);
                            }
                        })
                ;
    }

    /**
     * Supprime un pays de la collection en fonction de son identifiant.
     * <p>
     * Cette méthode récupère l'ensemble actuel des pays via Mutiny, puis supprime
     * celui dont l'identifiant correspond à {@code id}. Si l'ensemble des pays n'est pas
     * initialisé (null), une exception {@link IllegalStateException} est levée.
     * </p>
     *
     * @param id l'identifiant du pays à supprimer
     * @return un {@link Uni} contenant l'ensemble mis à jour des pays
     * @throws IllegalStateException si l'ensemble des pays n'est pas initialisé
     */
    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(fetchCountries -> fetchCountries.removeIf(country -> Objects.equals(country.getId(), id)))
                ;
    }

    /**
     * Supprime tous les pays de la collection.
     * <p>
     * Cette méthode vide l'ensemble des pays récupéré via {@code Mutiny.fetch(countries)}.
     * Si l'ensemble des pays n'est pas initialisé (null), une exception {@link IllegalStateException} est levée.
     * </p>
     *
     * @return un {@link Uni} contenant l'ensemble désormais vide des pays
     * @throws IllegalStateException si l'ensemble des pays n'est pas initialisé
     */
    public Uni<Set<Country>> clearCountries() {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(Set::clear)
                ;
    }

    public static Set<Person> fromDTOSet(Set<PersonDTO> personDTOSet) {
        return
                personDTOSet.stream()
                        .map(Person::build)
                        .collect(Collectors.toSet())
                ;
    }

    /*public List<Movie> getMovies() {
        return Collections.emptyList();
    }*/

    @Override
    public int compareTo(Person p) {
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
