package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    public static final String DEFAULT_SORT = "title";
    public static final List<String> ALLOWED_SORT_FIELDS = List.of("id", DEFAULT_SORT, "originalTitle", "releaseDate", "runningTime", "budget", "boxOffice", "user.username", "awardsCount", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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
    @PositiveOrZero(message = "La durée doit avoir une valeur positive")
    private Long runningTime;

    @Column(name = "budget")
    @PositiveOrZero(message = "Le budget doit avoir une valeur positive")
    private Long budget;

    @Column(name = "monnaie_budget")
    private String budgetCurrency;

    @Column(name = "box_office")
    @PositiveOrZero(message = "Le box-office doit avoir une valeur positive")
    private Long boxOffice;

    @Column(name = "box_office_budget")
    private String boxOfficeCurrency;

    @Column(name = "chemin_affiche")
    private String posterFileName;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @ManyToOne
    @JoinColumn(name = "fk_utilisateur", nullable = false, foreignKey = @ForeignKey(name = "fk_film_utilisateur"), referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieProducer> movieProducers = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDirector> movieDirectors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieScreenwriter> movieScreenwriters = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDialogueWriter> movieDialogueWriters = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieMusician> movieMusicians = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoviePhotographer> moviePhotographers = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCostumier> movieCostumiers = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDecorator> movieDecorators = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieEditor> movieEditors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCaster> movieCasters = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieArtist> movieArtists = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieSoundEditor> movieSoundEditors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieVfxSupervisor> movieVfxSupervisors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieSfxSupervisor> movieSfxSupervisors = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieMakeupArtist> movieMakeupArtists = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieHairDresser> movieHairDressers = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieStuntman> movieStuntmen = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieActor> movieActors = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "lnk_film_pays",
            joinColumns = @JoinColumn(name = "fk_film"),
            inverseJoinColumns = @JoinColumn(name = "fk_pays")
    )
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "lnk_film_genre",
            joinColumns = @JoinColumn(name = "fk_film"),
            inverseJoinColumns = @JoinColumn(name = "fk_genre")
    )
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

    public static Movie of(MovieDTO movieDTO) {
        return Movie.builder()
                .title(StringUtils.capitalize(StringUtils.defaultString(movieDTO.getTitle()).trim()))
                .originalTitle(StringUtils.capitalize(StringUtils.defaultString(movieDTO.getOriginalTitle()).trim()))
                .releaseDate(movieDTO.getReleaseDate())
                .synopsis(StringUtils.defaultString(movieDTO.getSynopsis()).trim())
                .runningTime(movieDTO.getRunningTime())
                .budget(movieDTO.getBudget())
                .boxOffice(movieDTO.getBoxOffice())
                .posterFileName(movieDTO.getPosterFileName())
                .user(User.fromDTO(movieDTO.getUser()))
                .build();
    }

    public <T extends MovieTechnician> void removeObsoleteTechnicians(List<T> technicians, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        technicians.removeIf(t ->
                movieTechnicianDTOList.stream().noneMatch(movieTechnicianDTO ->
                        Objects.nonNull(movieTechnicianDTO.getId()) && movieTechnicianDTO.getId().equals(t.getId())
                )
        );
    }

    public <T extends MovieTechnician> void updateExistingTechnicians(List<T> technicians, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        technicians.forEach(t ->
                movieTechnicianDTOList.stream()
                        .filter(dto -> Objects.equals(dto.getId(), t.getId()))
                        .findFirst()
                        .ifPresent(dto -> {
                            if (!Objects.equals(t.getRole(), dto.getRole())) {
                                t.setRole(dto.getRole());
                            }
                        })
        );
    }

    public <T extends MovieTechnician> Uni<Boolean> addTechnicians(
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory
    ) {
        List<Uni<T>> newTechniciansUnis = movieTechnicianDTOList.stream()
                .filter(dto -> Objects.isNull(dto.getId()))
                .map(dto -> asyncTechnicianFactory.apply(this, dto)) // async creation
                .toList();

        return
                Uni.join().all(newTechniciansUnis)
                        .usingConcurrencyOf(1)
                        .andCollectFailures()
                        .map(tList -> techniciansGetter.apply(this).addAll(tList))
                ;
    }

    public void removeObsoleteActors(List<MovieActorDTO> movieActorsDTOList) {
        movieActors.removeIf(movieActor ->
                movieActorsDTOList.stream().noneMatch(movieActorDTO ->
                        Objects.nonNull(movieActorDTO.getId()) && movieActorDTO.getId().equals(movieActor.getId())
                )
        );
    }

    public void updateExistingActors(List<MovieActorDTO> movieActorsDTOList) {
        movieActors.forEach(movieActor ->
                movieActorsDTOList.stream()
                        .filter(dto -> Objects.equals(dto.getId(), movieActor.getId()))
                        .findFirst()
                        .ifPresent(dto -> {
                            if (!Objects.equals(movieActor.getRole(), dto.getRole())) {
                                movieActor.setRole(dto.getRole());
                            }
                            if (!Objects.equals(movieActor.getRank(), dto.getRank())) {
                                movieActor.setRank(dto.getRank());
                            }
                        })
        );
    }

    public Uni<Boolean> addMovieActors(List<MovieActorDTO> movieActorsDTOList, BiFunction<Movie, MovieActorDTO, Uni<MovieActor>> asyncActorFactory) {
        List<Uni<MovieActor>> newActorsUnis = movieActorsDTOList.stream()
                .filter(dto -> Objects.isNull(dto.getId()))
                .map(dto -> asyncActorFactory.apply(this, dto)) // async creation
                .toList();

        return
                newActorsUnis.isEmpty()
                        ? Uni.createFrom().item(false)
                        :
                        Uni.join().all(newActorsUnis)
                                .usingConcurrencyOf(1)
                                .andCollectFailures()
                                .map(movieActors::addAll)
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
                        .invoke(fetchGenres -> {
                            if (Objects.nonNull(genreSet)) {
                                fetchGenres.addAll(genreSet);
                            }
                        })
                ;
    }

    /**
     * Ajoute un ensemble de pays à la collection existante.
     *
     * @param countrySet L'ensemble des pays à ajouter.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des pays après l'ajout.
     * @throws IllegalStateException Si la collection des pays n'est pas initialisée.
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
     * Ajoute un ensemble de récompenses à la collection existante.
     *
     * @param awardSet L'ensemble des récompenses à ajouter.
     * @return Une {@link Uni} contenant la collection mise à jour des {@link Award}.
     * @throws IllegalStateException si la collection des récompenses n'est pas initialisée.
     */
    public Uni<Set<Award>> addAwards(Set<Award> awardSet) {
        return
                Mutiny.fetch(awards)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La collection des récompenses n'est pas initialisés"))
                        .invoke(fetchAwards -> {
                            if (Objects.nonNull(awardSet)) {
                                fetchAwards.addAll(awardSet);
                            }
                        })
                ;
    }

    /**
     * Retire une personne de la collection existante de personnes en fonction de son identifiant.
     *
     * @param techniciansGetter L'ensemble des personnes dans lequel rechercher.
     * @param id                L'identifiant de la personne à retirer.
     * @param errorMessage      Le message d'erreur à retourner si l'ensemble des personnes est null.
     * @return Une {@link Uni} contenant l'ensemble mis à jour des personnes après suppression.
     * @throws IllegalStateException Si la collection existante des personnes n'est pas initialisée.
     */
    public <T extends MovieTechnician> Uni<List<T>> removeTechnician(Function<Movie, List<T>> techniciansGetter, Long id, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter.apply(this))
                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                        .invoke(tList -> tList.removeIf(person -> Objects.equals(person.id, id)))
                ;
    }

    /**
     * Supprime un acteur de la collection existante de personnes en fonction de son identifiant.
     *
     * @param id L'identifiant de l'entité {@link MovieActor} à supprimer.
     * @return Une instance de {@link Uni} contenant la liste mise à jour des acteurs.
     * @throws IllegalStateException Si la liste des acteurs n'est pas initialisée.
     */
    public Uni<List<MovieActor>> removeMovieActor(Long id) {
        return
                Mutiny.fetch(movieActors)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                        .invoke(movieActorList -> movieActorList.removeIf(movieActor -> Objects.equals(movieActor.getId(), id)))
                ;
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
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des genres n'est pas initialisé"))
                        .invoke(fetchGenres -> fetchGenres.removeIf(genre -> Objects.equals(genre.getId(), id)))
                ;
    }

    /**
     * Retire un pays de la collection existante en fonction de son ID.
     *
     * @param id L'ID du pays à retirer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des pays après la suppression.
     * @throws IllegalStateException Si la collection existante des pays est null.
     */
    public Uni<Set<Country>> removeCountry(Long id) {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(fetchCountries -> fetchCountries.removeIf(country -> Objects.equals(country.getId(), id)))
                ;
    }

    /**
     * Supprime une récompense par son identifiant de l'ensemble des récompenses.
     * <p>
     * Cette méthode permet de supprimer une récompense de l'ensemble des récompenses en fonction de son identifiant.
     * Si l'ensemble des récompenses n'est pas initialisé, une exception est levée. La suppression de la récompense se
     * fait en recherchant la récompense dont l'identifiant correspond à celui fourni.
     *
     * @param id L'identifiant de la récompense à supprimer.
     * @return Un {@link Uni} contenant l'ensemble des récompenses après suppression de celle correspondant à l'identifiant.
     * @throws IllegalStateException Si l'ensemble des récompenses n'est pas initialisé.
     */
    public Uni<Set<Award>> removeAward(Long id) {
        return
                Mutiny.fetch(awards)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des récompenses n'est pas initialisé"))
                        .invoke(fetchAwards -> fetchAwards.removeIf(award -> Objects.equals(award.getId(), id)))
                ;
    }

    /**
     * Vide un ensemble de personnes.
     * <p>
     * Cette méthode permet de vider un ensemble de personnes spécifié. Avant de procéder à l'opération, elle vérifie
     * si l'ensemble est initialisé (non nul). Si l'ensemble est nul, une exception est levée avec le message d'erreur
     * fourni. Après validation, l'ensemble est vidé à l'aide de la méthode {@link Set#clear()}.
     *
     * @param techniciansGetter L'ensemble des personnes à vider.
     * @param errorMessage      Le message d'erreur à utiliser dans le cas où l'ensemble des personnes n'est pas initialisé.
     * @param <T>               Le type des éléments dans l'ensemble des personnes.
     * @return Un {@link Uni} contenant un ensemble vide après que l'opération a été effectuée.
     * @throws IllegalStateException Si l'ensemble des personnes n'est pas initialisé (null).
     */
    public <T> Uni<List<T>> clearPersons(List<T> techniciansGetter, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter)
                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                        .invoke(List::clear)
                ;
    }

    /**
     * Vide l'ensemble des genres associés à un objet.
     * <p>
     * Cette méthode permet de vider la collection des genres associés à l'objet en utilisant la méthode
     * {@link Set#clear()}. Elle vérifie également si l'ensemble des genres est correctement initialisé.
     * Si la collection des genres est nulle, une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble vide de genres après avoir vidé la collection.
     * @throws IllegalStateException Si l'ensemble des genres n'est pas initialisé (null).
     */
    public Uni<Set<Genre>> clearGenres() {
        return
                Mutiny.fetch(genres)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des genres n'est pas initialisé"))
                        .invoke(Set::clear)
                ;
    }

    /**
     * Vide l'ensemble des pays associés à un objet.
     * <p>
     * Cette méthode permet de vider la collection des pays associés à l'objet en utilisant la méthode
     * {@link Set#clear()}. Elle vérifie également si l'ensemble des pays est correctement initialisé.
     * Si la collection des pays est nulle, une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble vide de pays après avoir vidé la collection.
     * @throws IllegalStateException Si l'ensemble des pays n'est pas initialisé (null).
     */
    public Uni<Set<Country>> clearCountries() {
        return
                Mutiny.fetch(countries)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des pays n'est pas initialisé"))
                        .invoke(Set::clear)
                ;
    }

    /**
     * Vide l'ensemble des récompenses associées à un objet.
     * <p>
     * Cette méthode permet de vider la collection des récompenses associées à l'objet en utilisant la méthode
     * {@link Set#clear()}. Elle vérifie également si l'ensemble des récompenses est correctement initialisée.
     * Si la collection des récompenses est nulle, une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble vide de récompenses après avoir vidé la collection.
     * @throws IllegalStateException Si l'ensemble des récompenses n'est pas initialisée (null).
     */
    public Uni<Set<Award>> clearAwards() {
        return
                Mutiny.fetch(awards)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("L'ensemble des récompenses n'est pas initialisé"))
                        .invoke(Set::clear)
                ;
    }

    public Uni<List<MovieActorDTO>> fetchAndMapActorList() {
        return
                Mutiny.fetch(movieActors)
                        .onItem().ifNull().failWith(() -> new IllegalStateException("La liste des acteurs n'est pas initialisée"))
                        .map(movieActorList ->
                                movieActorList
                                        .stream()
                                        .map(MovieActorDTO::of)
                                        .sorted(MovieActorDTO::compareTo)
                                        .toList()
                        )
                ;
    }

    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> fetchAndMapTechniciansList(Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter.apply(this))
                        .onItem().ifNull().failWith(() -> new IllegalStateException(errorMessage))
                        .map(tList ->
                                tList
                                        .stream()
                                        .map(MovieTechnicianDTO::of)
                                        .toList()
                        )
                ;
    }
}
