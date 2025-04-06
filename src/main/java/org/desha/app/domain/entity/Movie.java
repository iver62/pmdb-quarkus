package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieDTO;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
public class Movie extends PanacheEntityBase {

    public static final List<String> ALLOWED_SORT_FIELDS = List.of("title", "originalTitle", "releaseDate", "runningTime", "budget", "boxOffice", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Long id;

    @NotEmpty(message = "Le titre ne peut pas être vide")
    @Column(name = "titre", nullable = false)
    private String title;

    @Column(name = "titre_original")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "date_sortie", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate releaseDate;

    @Column(name = "duree")
    private Long runningTime;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "box_office")
    private Long boxOffice;

    @Column(name = "chemin_affiche")
    private String posterFileName;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @ManyToOne
    @JoinColumn(name = "fk_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_film_utilisateur"), referencedColumnName = "id")
    private User user;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_producteur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_producteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Producer> producers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_realisateur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_realisateur"))
    @Fetch(FetchMode.SELECT)
    private Set<Director> directors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_scenariste", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_scenariste"))
    @Fetch(FetchMode.SELECT)
    private Set<Screenwriter> screenwriters = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_musicien", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_musicien"))
    @Fetch(FetchMode.SELECT)
    private Set<Musician> musicians = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_photographe", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_photographe"))
    @Fetch(FetchMode.SELECT)
    private Set<Photographer> photographers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_costumier", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_costumier"))
    @Fetch(FetchMode.SELECT)
    private Set<Costumier> costumiers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_decorateur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_decorateur"))
    @Fetch(FetchMode.SELECT)
    private Set<Decorator> decorators = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_monteur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_monteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Editor> editors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_casteur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_casteur"))
    @Fetch(FetchMode.SELECT)
    private Set<Caster> casters = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_directeur_artistique", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_directeur_artistique"))
    @Fetch(FetchMode.SELECT)
    private Set<ArtDirector> artDirectors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_ingenieur_son", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_ingenieur_son"))
    @Fetch(FetchMode.SELECT)
    private Set<SoundEditor> soundEditors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_specialiste_effets_speciaux", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_specialiste_effets_speciaux"))
    @Fetch(FetchMode.SELECT)
    private Set<VisualEffectsSupervisor> visualEffectsSupervisors = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_maquilleur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_maquilleur"))
    @Fetch(FetchMode.SELECT)
    private Set<MakeupArtist> makeupArtists = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_coiffeur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_coiffeur"))
    @Fetch(FetchMode.SELECT)
    private Set<HairDresser> hairDressers = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_cascadeur", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_cascadeur"))
    @Fetch(FetchMode.SELECT)
    private Set<Stuntman> stuntmen = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = {CascadeType.ALL})
    @Fetch(FetchMode.SELECT)
    private List<MovieActor> movieActors = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_pays", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_pays"))
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "lnk_film_genre", joinColumns = @JoinColumn(name = "fk_film"), inverseJoinColumns = @JoinColumn(name = "fk_genre"))
    private Set<Genre> genres = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Award> awards = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public static Movie fromDTO(MovieDTO movieDTO) {
        return Movie.builder()
                .title(movieDTO.getTitle())
                .originalTitle(movieDTO.getOriginalTitle())
                .releaseDate(movieDTO.getReleaseDate())
                .synopsis(movieDTO.getSynopsis())
                .runningTime(movieDTO.getRunningTime())
                .budget(movieDTO.getBudget())
                .boxOffice(movieDTO.getBoxOffice())
                .posterFileName(movieDTO.getPosterFileName())
                .user(User.fromDTO(movieDTO.getUser()))
                .build();
    }

    public static Uni<List<Movie>> getByTitle(String title) {
        return list("title", title);
    }

    public static Uni<List<Movie>> getAllMovies(String sort, Sort.Direction direction, String title) {
        return
                find("lower(title) like lower(?1)", Sort.by(sort, direction), MessageFormat.format("%{0}%", title))
                        .list()
                ;
    }

    /**
     * Ajoute un ensemble de personnes à une collection existante.
     *
     * @param <T>          Le type des personnes à ajouter.
     * @param persons      L'ensemble existant de personnes auquel ajouter les nouvelles personnes.
     * @param peopleSet    L'ensemble des personnes à ajouter.
     * @param errorMessage Le message d'erreur à retourner si l'ensemble existant est null.
     * @return Une {@link Uni} contenant l'ensemble mis à jour après l'ajout des nouvelles personnes.
     * @throws IllegalStateException Si la collection existante des personnes est null.
     */
    public <T> Uni<Set<T>> addPeople(Set<T> persons, Set<T> peopleSet, String errorMessage) {
        return
                Mutiny.fetch(persons)
                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                        .invoke(tSet -> tSet.addAll(peopleSet))
                ;
    }

    public Uni<List<MovieActor>> addRole(MovieActor movieActor) {
        return
                Mutiny.fetch(movieActors)
                        .map(
                                fetchRoles -> {
                                    fetchRoles.add(movieActor);
                                    return fetchRoles;
                                }
                        )
                ;
    }

    public Uni<Set<MovieActor>> addRoles(Set<MovieActor> roles) {
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

    /**
     * Ajoute un ensemble de genres à la collection existante.
     *
     * @param genreSet L'ensemble des genres à ajouter.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des genres après l'ajout.
     * @throws IllegalStateException Si la collection existante des genres est null.
     */
    public Uni<Set<Genre>> addGenres(Set<Genre> genreSet) {
        return
                Mutiny.fetch(genres)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Genres non initialisés"))
                        .invoke(fetchGenres -> fetchGenres.addAll(genreSet))
                ;
    }

    /**
     * Ajoute un ensemble de pays à la collection existante.
     *
     * @param countrySet L'ensemble des pays à ajouter.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des pays après l'ajout.
     * @throws IllegalStateException Si la collection existante des pays est null.
     */
    public Uni<Set<Country>> addCountries(Set<Country> countrySet) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés"))
                        .invoke(fetchCountries -> fetchCountries.addAll(countrySet))
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

    /**
     * Retire une personne dde la collection existante de personnes en fonction de son identifiant.
     *
     * @param <T>          Le type de la personne (doit être une sous-classe de {@link Person}).
     * @param persons      L'ensemble des personnes dans lequel rechercher.
     * @param id           L'identifiant de la personne à retirer.
     * @param errorMessage Le message d'erreur à retourner si l'ensemble des personnes est null.
     * @return Une {@link Uni} contenant l'ensemble mis à jour des personnes après suppression.
     * @throws IllegalStateException Si la collection existante des personnes est null.
     */
    public <T extends Person> Uni<Set<T>> removePerson(Set<T> persons, Long id, String errorMessage) {
        return
                Mutiny.fetch(persons)
                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                        .invoke(tSet -> tSet.removeIf(t -> Objects.equals(t.id, id)))
                ;
    }

    public void removeRole(Long id) {
        this.movieActors.removeIf(role -> Objects.equals(role.id, id));
    }

    /**
     * Retire un genre de la collection existante en fonction de son ID.
     *
     * @param id L'ID du genre à supprimer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des genres après la suppression.
     * @throws IllegalStateException Si la collection existante des genres est null.
     */
    public Uni<Set<Genre>> removeGenre(Long id) {
        return
                Mutiny.fetch(genres)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Genres non initialisés"))
                        .invoke(fetchGenres -> fetchGenres.removeIf(genre -> Objects.equals(genre.id, id)))
                ;
    }

    /**
     * Supprime un pays de la collection existante en fonction de son ID.
     *
     * @param id L'ID du pays à supprimer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des pays après la suppression.
     * @throws IllegalStateException Si la collection existante des pays est null.
     */
    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("Pays non initialisés"))
                        .invoke(fetchCountries -> fetchCountries.removeIf(country -> Objects.equals(country.id, id)))
                ;
    }

    public Uni<Set<Award>> removeAward(Long id) {
        return
                Mutiny.fetch(awards)
                        .map(
                                fetchAwards -> {
                                    fetchAwards.removeIf(award -> Objects.equals(award.getId(), id));
                                    return fetchAwards;
                                }
                        )
                ;
    }
}
