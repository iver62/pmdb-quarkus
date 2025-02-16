package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
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
@NamedQueries({
        @NamedQuery(name = "Movie.searchByTitle", query = "from Movie where lower(title) LIKE lower(concat('%', ?1, '%'))")
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

    @Column(name = "chemin_affiche")
    private String posterFileName;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

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
                .build();
    }

    public static Uni<Movie> getById(Long id) {
        return findById(id);
    }

    public static Uni<Set<PanacheEntityBase>> getByTitle(String title) {
        return
                Panache.withTransaction(() -> list("title", title).map(HashSet::new));
    }

    public static Uni<Set<PanacheEntityBase>> searchByTitle(String title) {
        return
                Panache.withTransaction(() -> list("#Movie.searchByTitle", "%" + title + "%").map(HashSet::new));
    }

    public static Uni<Long> count(String title) {
        return count("LOWER(title) LIKE LOWER(?1)", "%" + title + "%");
    }

    public static Uni<List<Movie>> getMovies(String sort, Sort.Direction direction, String title) {
        return
                find("LOWER(title) LIKE LOWER(?1)", Sort.by(sort, direction), "%" + title + "%")
                        .list()
                ;
    }

    public static Uni<List<Movie>> getPaginatedMovies(int pageIndex, int size, String sort, Sort.Direction direction, String title) {
        return
                find("LOWER(title) LIKE LOWER(?1)", Sort.by(sort, direction), "%" + title + "%")
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<Set<Producer>> addProducers(Set<Producer> producerSet) {
        return
                Mutiny.fetch(producers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(producerSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Director>> addDirectors(Set<Director> directorSet) {
        return
                Mutiny.fetch(directors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(directorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Screenwriter>> addScreenwriters(Set<Screenwriter> screenwriterSet) {
        return
                Mutiny.fetch(screenwriters)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(screenwriterSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Musician>> addMusicians(Set<Musician> musicianSet) {
        return
                Mutiny.fetch(musicians)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(musicianSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Photographer>> addPhotographers(Set<Photographer> photographerSet) {
        return
                Mutiny.fetch(photographers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(photographerSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Costumier>> addCostumiers(Set<Costumier> costumierSet) {
        return
                Mutiny.fetch(costumiers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(costumierSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Decorator>> addDecorators(Set<Decorator> decoratorSet) {
        return
                Mutiny.fetch(decorators)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(decoratorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Editor>> addEditors(Set<Editor> editorSet) {
        return
                Mutiny.fetch(editors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(editorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<Caster>> saveCasters(Set<Caster> casterSet) {
        return
                Mutiny.fetch(casters)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(casterSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<ArtDirector>> saveArtDirectors(Set<ArtDirector> artDirectorSet) {
        return
                Mutiny.fetch(artDirectors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(artDirectorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<SoundEditor>> saveSoundEditors(Set<SoundEditor> soundEditorSet) {
        return
                Mutiny.fetch(soundEditors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(soundEditorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<VisualEffectsSupervisor>> saveVisualEffectsSupervisors(Set<VisualEffectsSupervisor> visualEffectsSupervisorSet) {
        return
                Mutiny.fetch(visualEffectsSupervisors)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(visualEffectsSupervisorSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<MakeupArtist>> saveMakeupArtists(Set<MakeupArtist> makeupArtistSet) {
        return
                Mutiny.fetch(makeupArtists)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(makeupArtistSet);
                                    return people;
                                }
                        )
                ;
    }

    public Uni<Set<HairDresser>> saveHairDressers(Set<HairDresser> hairDresserSet) {
        return
                Mutiny.fetch(hairDressers)
                        .map(
                                people -> {
                                    people.clear();
                                    people.addAll(hairDresserSet);
                                    return people;
                                }
                        )
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

    public Uni<Set<Producer>> removeProducer(Long id) {
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

    public Uni<Set<Director>> removeDirector(Long id) {
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

    public Uni<Set<Screenwriter>> removeScreenwriter(Long id) {
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

    public Uni<Set<Musician>> removeMusician(Long id) {
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

    public Uni<Set<Photographer>> removePhotographer(Long id) {
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

    public Uni<Set<Costumier>> removeCostumier(Long id) {
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

    public Uni<Set<Decorator>> removeDecorator(Long id) {
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

    public Uni<Set<Editor>> removeEditor(Long id) {
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

    public Uni<Set<Caster>> removeCaster(Long id) {
        return
                Mutiny.fetch(casters)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<ArtDirector>> removeArtDirector(Long id) {
        return
                Mutiny.fetch(artDirectors)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<SoundEditor>> removeSoundEditor(Long id) {
        return
                Mutiny.fetch(soundEditors)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<VisualEffectsSupervisor>> removeVisualEffectsSupervisor(Long id) {
        return
                Mutiny.fetch(visualEffectsSupervisors)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<MakeupArtist>> removeMakeupArtist(Long id) {
        return
                Mutiny.fetch(makeupArtists)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<HairDresser>> removeHairDresser(Long id) {
        return
                Mutiny.fetch(hairDressers)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public Uni<Set<Stuntman>> removeStuntman(Long id) {
        return
                Mutiny.fetch(stuntmen)
                        .map(
                                persons -> {
                                    persons.removeIf(person -> Objects.equals(person.id, id));
                                    return persons;
                                }
                        )
                ;
    }

    public void removeRole(Long id) {
        this.movieActors.removeIf(role -> Objects.equals(role.id, id));
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
