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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", DEFAULT_SORT, "dateOfBirth", "dateOfDeath", "moviesCount", "awardsCount", "creationDate", "lastUpdate");

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

    @OneToMany(mappedBy = "actor")
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
    private List<MovieDecorator> decoratedMovies = new ArrayList<>();

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

    public static Person of(PersonDTO personDTO, PersonType type) {
        return
                Person.builder()
                        .id(personDTO.getId())
                        .name(personDTO.getName().trim())
                        .photoFileName(Objects.nonNull(personDTO.getPhotoFileName()) ? personDTO.getPhotoFileName() : PersonService.DEFAULT_PHOTO)
                        .dateOfBirth(personDTO.getDateOfBirth())
                        .dateOfDeath(personDTO.getDateOfDeath())
                        .types(
                                Stream.concat(
                                        Optional.ofNullable(personDTO.getTypes()).orElse(Set.of()).stream(),
                                        Stream.of(type)
                                ).collect(Collectors.toSet())
                        )
                        .creationDate(personDTO.getCreationDate())
                        .lastUpdate(personDTO.getLastUpdate())
                        .build()
                ;
    }

    @JsonIgnore
    public Uni<Set<Movie>> getAllRelatedMovies() {
        /*return
                Uni.join().all(
                                getPlayedMovies(),
                                getMoviesByType(() -> Mutiny.fetch(producedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(directedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(assistantDirectedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(writtenMovies)),
                                getMoviesByType(() -> Mutiny.fetch(composedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(musicalMovies)),
                                getMoviesByType(() -> Mutiny.fetch(photographedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(costumedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(decoratedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(editedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(castedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(artistMovies)),
                                getMoviesByType(() -> Mutiny.fetch(soundEditedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(vfxSupervisedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(sfxSupervisedMovies)),
                                getMoviesByType(() -> Mutiny.fetch(makeupMovies)),
                                getMoviesByType(() -> Mutiny.fetch(hairStyledMovies)),
                                getMoviesByType(() -> Mutiny.fetch(stuntMovies))
                        )
                        .usingConcurrencyOf(1)
                        .andCollectFailures()
                        .map(lists ->
                                lists.stream()
                                        .map(HashSet::new)
                                        .reduce(new HashSet<>(), (acc, set) -> {
                                            acc.addAll(set);
                                            return acc;
                                        })
                        )
                ;*/

        return
                Uni.createFrom().item(new HashSet<Movie>())
                        .chain(set -> getPlayedMovies().invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(producedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(directedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(assistantDirectedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(writtenMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(composedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(musicalMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(photographedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(costumeDesignedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(decoratedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(editedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(castedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(artistMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(soundEditedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(vfxSupervisedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(sfxSupervisedMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(makeupMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(hairStyledMovies)).invoke(set::addAll).replaceWith(set))
                        .chain(set -> getMoviesByType(() -> Mutiny.fetch(stuntMovies)).invoke(set::addAll).replaceWith(set))
                ;
    }

    private Uni<List<Movie>> getPlayedMovies() {
        return
                Mutiny.fetch(playedMovies)
                        .map(movieActors ->
                                movieActors
                                        .stream()
                                        .map(MovieActor::getMovie)
                                        .toList()
                        )
                ;
    }

    private <T extends MovieTechnician> Uni<List<Movie>> getMoviesByType(Supplier<Uni<List<T>>> getTechnicians) {
        return
                getTechnicians.get()
                        .map(tList ->
                                tList
                                        .stream()
                                        .map(T::getMovie)
                                        .toList()
                        )
                ;
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
        return name.toLowerCase().compareTo(p.getName().toLowerCase());
    }
}
