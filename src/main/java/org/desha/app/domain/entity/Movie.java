package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.desha.app.domain.dto.MovieActorDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.MovieTechnicianDTO;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movie extends PanacheEntityBase {

    public static final String DEFAULT_POSTER = "default-poster.jpg";
    public static final String DEFAULT_SORT = "title";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "originalTitle", "releaseDate", "runningTime", "budget", "boxOffice", "user.username", "awardsCount", "creationDate", "lastUpdate");

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
    private LocalDate releaseDate;

    @Column(name = "duree")
    @PositiveOrZero(message = "La durée doit avoir une valeur positive")
    private Integer runningTime;

    @Column(name = "budget")
    @PositiveOrZero(message = "Le budget doit avoir une valeur positive")
    private Long budget;

    @Column(name = "budget_devise")
    private String budgetCurrency;

    @Column(name = "box_office")
    @PositiveOrZero(message = "Le box-office doit avoir une valeur positive")
    private Long boxOffice;

    @Column(name = "box_office_devise")
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
    private List<MovieActor> movieActors = new ArrayList<>();

    @Embedded
    private TechnicalTeam technicalTeam;

    @ManyToMany(cascade = {CascadeType.ALL, CascadeType.MERGE})
    @JoinTable(
            name = "lnk_film_pays",
            joinColumns = @JoinColumn(name = "fk_film"),
            inverseJoinColumns = @JoinColumn(name = "fk_pays")
    )
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "lnk_film_categorie",
            joinColumns = @JoinColumn(name = "fk_film"),
            inverseJoinColumns = @JoinColumn(name = "fk_categorie")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CeremonyAwards> ceremoniesAwards = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    /**
     * Met à jour les informations générales du film à partir d'un {@link MovieDTO}.
     * <p>
     * Cette méthode recopie les principales propriétés de l'objet {@code movieDTO} vers l'entité {@code Movie}.
     * Les champs concernés incluent :
     * <ul>
     *   <li>le titre et le titre original,</li>
     *   <li>le synopsis,</li>
     *   <li>la date de sortie,</li>
     *   <li>la durée du film (running time),</li>
     *   <li>le budget et sa devise,</li>
     *   <li>le box-office et sa devise.</li>
     * </ul>
     *
     * @param movieDTO l'objet contenant les informations générales à appliquer au film.
     * @throws NullPointerException si {@code movieDTO} est {@code null}.
     * @implNote - La méthode suppose que {@code movieDTO.getBudget()} et {@code movieDTO.getBoxOffice()}
     * ne sont pas {@code null}. Si ce n’est pas garanti, il est recommandé d’ajouter des contrôles avant d’appeler leurs sous-propriétés.
     * - Cette méthode ne met à jour que les informations dites "générales" et n'affecte pas d'autres aspects du film
     * (par ex. casting, techniciens, catégories, etc.).
     */
    public void updateGeneralInfos(MovieDTO movieDTO) {
        setTitle(movieDTO.getTitle());
        setOriginalTitle(movieDTO.getOriginalTitle());
        setSynopsis(movieDTO.getSynopsis());
        setReleaseDate(movieDTO.getReleaseDate());
        setRunningTime(movieDTO.getRunningTime());
        setBudget(movieDTO.getBudget().value());
        setBudgetCurrency(movieDTO.getBudget().currency());
        setBoxOffice(movieDTO.getBoxOffice().value());
        setBoxOfficeCurrency(movieDTO.getBoxOffice().currency());
    }

    /**
     * Supprime de la liste des techniciens existants ceux qui ne figurent plus dans la liste des techniciens transmise en paramètre.
     * <p>
     * Cette méthode compare les identifiants de chaque technicien présent dans la liste existante ({@code technicians})
     * avec ceux contenus dans la liste de référence ({@code movieTechnicianDTOList}). Si un technicien existant n'a pas
     * d'équivalent (même {@code id}) dans la liste de référence, il est retiré.
     *
     * @param technicians            la liste des techniciens déjà associés au film, potentiellement obsolète.
     * @param movieTechnicianDTOList la nouvelle liste de techniciens à jour, contenant les identifiants valides à conserver.
     * @param <T>                    un type qui étend {@link MovieTechnician}.
     * @throws NullPointerException si la liste {@code technicians} est {@code null}.
     * @implNote - Si {@code movieTechnicianDTOList} est vide, tous les techniciens sont supprimés.<br>
     * - Si un {@code MovieTechnicianDTO} n'a pas d'identifiant ({@code null}), il n'est pas pris en compte pour la correspondance.<br>
     * - La suppression est effectuée en place sur la liste {@code technicians}.
     */
    public <T extends MovieTechnician> void removeObsoleteTechnicians(List<T> technicians, List<MovieTechnicianDTO> movieTechnicianDTOList) {
        technicians.removeIf(t ->
                movieTechnicianDTOList.stream().noneMatch(movieTechnicianDTO ->
                        Objects.nonNull(movieTechnicianDTO.getId()) && movieTechnicianDTO.getId().equals(t.getId())
                )
        );
    }

    /**
     * Met à jour les techniciens existants d'un film à partir d'une liste de {@link MovieTechnicianDTO}.
     * <p>
     * Pour chaque technicien présent dans la liste {@code technicians}, cette méthode recherche dans la
     * liste {@code movieTechnicianDTOList} un DTO correspondant (même identifiant). Si un technicien correspondant
     * est trouvé, son rôle est comparé à celui du DTO et mis à jour si nécessaire.
     * <p>
     * Les techniciens non présents dans la liste des DTO ne sont pas modifiés ni supprimés
     * par cette méthode (voir {@link #removeObsoleteTechnicians(List, List)} pour ce cas d’usage).
     *
     * @param technicians            la liste des techniciens existants liés au film (entités).
     * @param movieTechnicianDTOList la liste des techniciens envoyés par le client (DTOs), utilisée comme source de vérité pour la mise à jour.
     * @param <T>                    le type concret de technicien, qui doit hériter de {@link MovieTechnician}.
     * @throws NullPointerException si l'une des listes fournies est {@code null}.
     * @implNote - La comparaison est faite sur l'identifiant ({@code id}) du technicien.
     * - Actuellement, seule la propriété {@code role} est mise à jour. Si d'autres champs doivent évoluer, la logique devra être enrichie.
     * - Si un {@code MovieTechnicianDTO} n’a pas d’ID, il sera ignoré lors de la mise à jour.
     */
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
                newTechniciansUnis.isEmpty()
                        ?
                        Uni.createFrom().item(false)
                        :
                        Uni.join().all(newTechniciansUnis)
                                .usingConcurrencyOf(1)
                                .andCollectFailures()
                                .map(tList -> techniciansGetter.apply(this).addAll(tList))
                ;
    }

    /**
     * Supprime les acteurs obsolètes du film en fonction de la liste DTO fournie.
     * <p>
     * Parcourt la collection {@code movieActors} et retire tout acteur dont l'identifiant ne figure pas dans la liste
     * {@code movieActorsDTOList}. Un acteur est considéré comme obsolète si aucun DTO correspondant avec un identifiant
     * non nul n'est trouvé.
     *
     * @param movieActorsDTOList La liste de DTO représentant les acteurs à conserver. Si {@code null} ou vide, tous les acteurs
     *                           existants seront supprimés.
     * @implNote - La suppression est effectuée directement sur la collection {@code movieActors}.
     * - La comparaison des identifiants utilise {@link Objects#equals(Object, Object)}, ce qui gère correctement les valeurs {@code null}.
     * - Les acteurs dont l'identifiant est {@code null} dans le DTO sont considérés comme non correspondants et seront supprimés.
     */
    public void removeObsoleteActors(List<MovieActorDTO> movieActorsDTOList) {
        movieActors.removeIf(movieActor ->
                movieActorsDTOList.stream().noneMatch(movieActorDTO ->
                        Objects.nonNull(movieActorDTO.getId()) && movieActorDTO.getId().equals(movieActor.getId())
                )
        );
    }

    /**
     * Met à jour les informations des acteurs existants dans le film à partir d'une liste de DTO.
     * <p>
     * Pour chaque acteur dans la collection {@code movieActors}, cette méthode recherche un DTO correspondant dans
     * {@code movieActorsDTOList} basé sur l'identifiant. Si un DTO correspondant est trouvé, les propriétés {@code role}
     * et {@code rank} de l'acteur sont mises à jour uniquement si elles diffèrent de celles du DTO.
     *
     * @param movieActorsDTOList La liste de DTO représentant les acteurs avec les nouvelles informations.
     *                           Si {@code null} ou vide, aucun acteur ne sera mis à jour.
     * @implNote - La mise à jour est effectuée directement sur la collection {@code movieActors}.
     * - La comparaison des identifiants et des champs utilise {@link Objects#equals(Object, Object)} pour gérer correctement les valeurs {@code null}.
     * - Seuls les champs {@code role} et {@code rank} sont mis à jour ; les autres propriétés de l'acteur restent inchangées.
     */
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
                        ?
                        Uni.createFrom().item(false)
                        :
                        Uni.join().all(newActorsUnis)
                                .usingConcurrencyOf(1)
                                .andCollectFailures()
                                .map(movieActors::addAll)
                ;
    }

    /**
     * Ajoute une collection de catégories à l'ensemble des catégories associées au film.
     * <p>
     * Cette méthode ne remplace pas les catégories existantes mais les enrichit : toutes les catégories fournies
     * dans {@code categorySet} sont ajoutées à l'attribut {@code categories}. Comme {@code categories} est un {@link Set},
     * les doublons (catégories déjà présentes) seront automatiquement ignorés.
     *
     * @param categorySet l'ensemble des catégories à ajouter au film. Si l'ensemble est vide, aucune modification n'est effectuée.
     * @throws NullPointerException si {@code categorySet} est {@code null}.
     * @implNote - La logique repose sur le comportement du {@link Set} pour éviter les doublons.
     * - Si tu veux remplacer complètement les catégories existantes, une méthode dédiée (par ex. {@code setCategories}) serait plus appropriée.
     */
    public void addCategories(Set<Category> categorySet) {
        categories.addAll(categorySet);
    }

    /**
     * Ajoute une collection de pays à l'ensemble des pays associés au film.
     * <p>
     * Cette méthode enrichit la collection existante {@code countries} en y ajoutant tous les éléments contenus dans {@code countrySet}.
     * Comme {@code countries} est un {@link Set}, les doublons (pays déjà présents) seront automatiquement ignorés.
     *
     * @param countrySet l'ensemble des pays à associer au film. Si l'ensemble est vide, aucune modification n'est effectuée.
     * @throws NullPointerException si {@code countrySet} est {@code null}.
     * @implNote - L'utilisation d'un {@link Set} garantit l'unicité des pays.
     * - Pour remplacer complètement les pays associés au film, il est recommandé d'utiliser une méthode dédiée (par ex. {@code setCountries}).
     */
    public void addCountries(Set<Country> countrySet) {
        countries.addAll(countrySet);
    }

    /**
     * Ajoute une cérémonie à la collection associée au film.
     * <p>
     * L'objet {@code ceremonyAwards} est ajouté directement à la collection {@code ceremoniesAwards}.
     * La méthode ne vérifie pas les doublons : si l'objet est déjà présent dans la collection, il sera ajouté à nouveau.
     *
     * @param ceremonyAwards La cérémonie ou récompense à ajouter. Si {@code null}, {@code null} sera ajouté à la collection.
     * @implNote - L'ajout est effectué directement sur la collection {@code ceremoniesAwards}.
     */
    public void addCeremonyAwards(CeremonyAwards ceremonyAwards) {
        ceremoniesAwards.add(ceremonyAwards);
    }

    /**
     * Supprime un technicien spécifique de la liste en fonction de son identifiant.
     * <p>
     * Cette méthode parcourt la liste des techniciens et retire celui dont l'identifiant correspond à {@code id}.
     * Si aucun technicien avec cet identifiant n'est trouvé, la liste reste inchangée.
     *
     * @param technicians la liste des techniciens parmi lesquels effectuer la suppression. Ne doit pas être {@code null}.
     * @param id          l'identifiant du technicien à supprimer. Peut être {@code null}, auquel cas aucun élément ne sera supprimé.
     * @param <T>         un type qui hérite de {@link MovieTechnician}.
     * @implNote - Cette méthode modifie directement la liste passée en paramètre.
     * - Si plusieurs techniciens partagent le même identifiant (cas improbable), tous seront supprimés.
     */
    public <T extends MovieTechnician> void removeTechnician(List<T> technicians, Long id) {
        technicians.removeIf(t -> Objects.equals(t.id, id));
    }

    /**
     * Supprime un acteur du film en fonction de son identifiant.
     * <p>
     * Cette méthode parcourt la collection {@code movieActors} et retire tout acteur dont l'identifiant correspond à {@code id}.
     * Si aucun acteur correspondant n'est trouvé, la collection reste inchangée.
     *
     * @param id l'identifiant de l'acteur à supprimer. Peut être {@code null}, auquel cas aucun élément ne sera retiré.
     * @implNote - La suppression est effectuée directement sur la collection {@code movieActors}.
     * - Si plusieurs acteurs partagent le même identifiant (cas improbable), tous seront supprimés.
     * - Utilise {@link Objects#equals(Object, Object)} pour comparer les identifiants, afin d'éviter les problèmes liés aux valeurs nulles.
     */
    public void removeMovieActor(Long id) {
        movieActors.removeIf(movieActor -> Objects.equals(movieActor.getId(), id));
    }

    /**
     * Supprime une catégorie associée au film en fonction de son identifiant.
     * <p>
     * Cette méthode parcourt la collection {@code categories} et retire toute catégorie dont l'identifiant correspond à {@code id}.
     * Si aucune catégorie correspondante n'est trouvée, la collection reste inchangée.
     *
     * @param id l'identifiant de la catégorie à supprimer. Peut être {@code null}, auquel cas aucun élément ne sera retiré.
     * @implNote - La suppression est effectuée directement sur la collection {@code categories}.
     * - Si plusieurs catégories possèdent le même identifiant (cas improbable), toutes seront supprimées.
     * - Utilise {@link Objects#equals(Object, Object)} pour comparer les identifiants, afin d'éviter les problèmes liés aux valeurs nulles.
     */
    public void removeCategory(Long id) {
        categories.removeIf(category -> Objects.equals(category.getId(), id));
    }

    /**
     * Supprime un pays associé au film en fonction de son identifiant.
     * <p>
     * Parcourt la collection {@code countries} et retire tout pays dont l'identifiant correspond à la valeur passée en paramètre.
     * Si aucun pays correspondant n'est trouvé, la collection reste inchangée.
     *
     * @param id L'identifiant du pays à supprimer. Si {@code null}, aucun élément n'est retiré.
     * @implNote - La suppression est effectuée directement sur la collection {@code countries}.
     * - La comparaison des identifiants utilise {@link Objects#equals(Object, Object)}, ce qui gère correctement les valeurs {@code null}.
     * - Si plusieurs pays partagent le même identifiant, tous seront supprimés.
     */
    public void removeCountry(Long id) {
        countries.removeIf(country -> Objects.equals(country.getId(), id));
    }

    /**
     * Supprime une cérémonie associée au film en fonction de son identifiant.
     * <p>
     * Parcourt la collection {@code ceremoniesAwards} et retire tout élément dont l'identifiant correspond à la valeur passée en paramètre.
     * Si aucun élément correspondant n'est trouvé, la collection reste inchangée.
     *
     * @param id L'identifiant de la cérémonie à supprimer. Si {@code null}, aucun élément n'est retiré.
     * @implNote - La suppression est effectuée directement sur la collection {@code ceremoniesAwards}.
     * - La comparaison des identifiants utilise {@link Objects#equals(Object, Object)}, ce qui gère correctement les valeurs {@code null}.
     * - Si plusieurs éléments partagent le même identifiant, tous seront supprimés.
     */
    public void removeCeremonyAward(Long id) {
        ceremoniesAwards.removeIf(ceremonyAwards -> Objects.equals(ceremonyAwards.getId(), id));
    }

    /**
     * Vide la liste des techniciens passée en paramètre.
     * <p>
     * Cette méthode supprime tous les éléments de la collection {@code technicians}, laissant la liste vide.
     *
     * @param technicians La liste de techniciens à vider. Ne doit pas être {@code null}.
     * @param <T>         Le type spécifique de {@link MovieTechnician} contenu dans la liste.
     * @implNote - L'opération est effectuée directement sur la collection fournie.
     * - Après l'appel, {@code technicians.isEmpty()} retournera {@code true}.
     */
    public <T extends MovieTechnician> void clearTechnicians(List<T> technicians) {
        technicians.clear();
    }

    /**
     * Supprime tous les acteurs associés au film.
     * <p>
     * Parcourt la collection {@code movieActors} et retire tous les acteurs présents.
     * Après l'exécution, la collection sera vide.
     *
     * @implNote - La suppression est effectuée directement sur la collection {@code movieActors}.
     * - Tous les éléments de la collection sont supprimés.
     */
    public void clearActors() {
        movieActors.clear();
    }

    /**
     * Supprime toutes les catégories associées au film.
     * <p>
     * Parcourt la collection {@code categories} et retire tous les éléments présents.
     * Après l'exécution, la collection sera vide.
     *
     * @implNote - La suppression est effectuée directement sur la collection {@code categories}.
     * - Tous les éléments de la collection sont supprimés.
     */
    public void clearCategories() {
        categories.clear();
    }

    /**
     * Supprime tous les pays associés au film.
     * <p>
     * Parcourt la collection {@code countries} et retire tous les éléments présents.
     * Après l'exécution, la collection sera vide.
     *
     * @implNote - La suppression est effectuée directement sur la collection {@code countries}.
     * - Tous les éléments de la collection sont supprimés.
     */
    public void clearCountries() {
        countries.clear();
    }

    /**
     * Supprime toutes les cérémonies associées au film.
     * <p>
     * Parcourt la collection {@code ceremoniesAwards} et retire tous les éléments présents.
     * Après l'exécution, la collection sera vide.
     *
     * @implNote - La suppression est effectuée directement sur la collection {@code ceremoniesAwards}.
     * - Tous les éléments de la collection sont supprimés.
     */
    public void clearCeremoniesAwards() {
        ceremoniesAwards.clear();
    }

}
