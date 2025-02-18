package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CountryDTO;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.reactive.mutiny.Mutiny;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "pays")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Country extends PanacheEntity {

    @Column(name = "code", nullable = false, unique = true)
    private int code;

    @Column(name = "alpha2")
    private String alpha2;

    @Column(name = "alpha3")
    private String alpha3;

    @Column(name = "nom_en_gb")
    private String nomEnGb;

    @Column(name = "nom_fr_fr")
    private String nomFrFr;

    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Actor> actors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Producer> producers;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Director> directors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Screenwriter> screenwriters;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Musician> musicians;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Photographer> photographers;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Costumier> costumiers;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Decorator> decorators;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Editor> editors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Caster> casters;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<ArtDirector> artDirectors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<SoundEditor> soundEditors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<VisualEffectsSupervisor> visualEffectsSupervisors;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<MakeupArtist> makeupArtists;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<HairDresser> hairDressers;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Stuntman> stuntmen;

    @JsonIgnore
    @ManyToMany(mappedBy = "countries")
    @Fetch(FetchMode.SELECT)
    private Set<Movie> movies = new HashSet<>();

    public Uni<Movie> addMovie(Movie movie) {
        return
                Mutiny.fetch(movies)
                        .invoke(
                                fetchedMovies -> {
                                    fetchedMovies.add(movie);
                                    log.info("Added movie {} to country {}", movie, this);
                                }
                        )
                        .replaceWith(movie)
                ;
    }

    @PrePersist
    public void onCreate() {
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    public static Uni<Long> count(String nameFrFr) {
        return count("LOWER(name) LIKE LOWER(?1)", "%" + nameFrFr + "%");
    }

    public static Uni<Country> getById(Long id) {
        return findById(id);
    }

    public static Uni<List<Country>> getAll() {
        return listAll();
    }

    public static Uni<Long> countMovies(Long id, String title) {
        return
                count(
                        "SELECT COUNT(m) FROM Movie m JOIN m.countries c WHERE c.id = ?1 AND LOWER(m.title) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", title)
                );
    }

    public static Uni<Long> countActors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(a) FROM Actor a JOIN a.countries c WHERE c.id = ?1 AND LOWER(a.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countProducers(Long id, String name) {
        return
                count(
                        "SELECT COUNT(p) FROM Producer p JOIN p.countries c WHERE c.id = ?1 AND LOWER(p.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countDirectors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(d) FROM Director d JOIN d.countries c WHERE c.id = ?1 AND LOWER(d.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countScreenwriters(Long id, String name) {
        return
                count(
                        "SELECT COUNT(s) FROM Screenwriter s JOIN s.countries c WHERE c.id = ?1 AND LOWER(s.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countMusicians(Long id, String name) {
        return
                count(
                        "SELECT COUNT(m) FROM Musician m JOIN m.countries c WHERE c.id = ?1 AND LOWER(m.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countDecorators(Long id, String name) {
        return
                count(
                        "SELECT COUNT(d) FROM Decorator d JOIN d.countries c WHERE c.id = ?1 AND LOWER(d.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countCostumiers(Long id, String name) {
        return
                count(
                        "SELECT COUNT(co) FROM Costumier co JOIN co.countries c WHERE c.id = ?1 AND LOWER(co.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countPhotographers(Long id, String name) {
        return
                count(
                        "SELECT COUNT(p) FROM Photographer p JOIN p.countries c WHERE c.id = ?1 AND LOWER(p.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countEditors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(e) FROM Editor e JOIN e.countries c WHERE c.id = ?1 AND LOWER(e.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countCasters(Long id, String name) {
        return
                count(
                        "SELECT COUNT(ca) FROM Caster ca JOIN ca.countries c WHERE c.id = ?1 AND LOWER(ca.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countArtDirectors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(ad) FROM ArtDirector ad JOIN ad.countries c WHERE c.id = ?1 AND LOWER(ad.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countSoundEditors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(se) FROM SoundEditor se JOIN se.countries c WHERE c.id = ?1 AND LOWER(se.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countVisualEffectsSupervisors(Long id, String name) {
        return
                count(
                        "SELECT COUNT(ves) FROM VisualEffectsSupervisor ves JOIN ves.countries c WHERE c.id = ?1 AND LOWER(ves.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countMakeupArtists(Long id, String name) {
        return
                count(
                        "SELECT COUNT(ma) FROM MakeupArtist ma JOIN ma.countries c WHERE c.id = ?1 AND LOWER(ma.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countHairDressers(Long id, String name) {
        return
                count(
                        "SELECT COUNT(hd) FROM HairDresser hd JOIN hd.countries c WHERE c.id = ?1 AND LOWER(hd.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<Long> countStuntmen(Long id, String name) {
        return
                count(
                        "SELECT COUNT(s) FROM Stuntman s JOIN s.countries c WHERE c.id = ?1 AND LOWER(s.name) LIKE LOWER(?2)",
                        id, MessageFormat.format("%{0}%", name)
                );
    }

    public static Uni<List<Movie>> getMovies(Long id, String sort, Sort.Direction direction, String title) {
        return
                find(
                        "SELECT m FROM Movie m JOIN m.countries c WHERE c.id = ?1 AND LOWER(m.title) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", title)
                ).list();
    }

    public static Uni<List<Movie>> getMovies(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String title) {
        return
                find(
                        "SELECT m FROM Movie m JOIN m.countries c WHERE c.id = ?1 AND LOWER(m.title) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", title)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Actor>> getActors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT a FROM Actor a JOIN a.countries c WHERE c.id = ?1 AND LOWER(a.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Producer>> getProducers(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT p FROM Producer p JOIN p.countries c WHERE c.id = ?1 AND LOWER(p.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Director>> getDirectors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT d FROM Director d JOIN d.countries c WHERE c.id = ?1 AND LOWER(d.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Screenwriter>> getScreenwriters(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT s FROM Screenwriter s JOIN s.countries c WHERE c.id = ?1 AND LOWER(s.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Musician>> getMusicians(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT m FROM Musician m JOIN m.countries c WHERE c.id = ?1 AND LOWER(m.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Decorator>> getDecorators(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT d FROM Decorator d JOIN d.countries c WHERE c.id = ?1 AND LOWER(d.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Costumier>> getCostumiers(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT co FROM Costumier co JOIN co.countries c WHERE c.id = ?1 AND LOWER(co.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Photographer>> getPhotographers(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT p FROM Photographer p JOIN p.countries c WHERE c.id = ?1 AND LOWER(p.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    /*public Uni<Set<Editor>> getEditors() {
        return Mutiny.fetch(editors)
                .map(
                        editorSet ->
                                editorSet
                                        .stream()
                                        .sorted(Comparator.comparing(Person::getName))
                                        .collect(Collectors.toCollection(LinkedHashSet::new))
                );
    }*/

    public static Uni<List<Editor>> getEditors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT e FROM Editor e JOIN e.countries c WHERE c.id = ?1 AND LOWER(e.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Caster>> getCasters(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT ca FROM Caster ca JOIN ca.countries c WHERE c.id = ?1 AND LOWER(ca.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<ArtDirector>> getArtDirectors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT ad FROM ArtDirector ad JOIN ad.countries c WHERE c.id = ?1 AND LOWER(ad.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<SoundEditor>> getSoundEditors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT se FROM SoundEditor se JOIN se.countries c WHERE c.id = ?1 AND LOWER(se.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<VisualEffectsSupervisor>> getVisualEffectsSupervisors(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT ves FROM VisualEffectsSupervisor ves JOIN ves.countries c WHERE c.id = ?1 AND LOWER(ves.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<MakeupArtist>> getMakeupArtists(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT ma FROM MakeupArtist ma JOIN ma.countries c WHERE c.id = ?1 AND LOWER(ma.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<HairDresser>> getHairDressers(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT hd FROM HairDresser hd JOIN hd.countries c WHERE c.id = ?1 AND LOWER(hd.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    public static Uni<List<Stuntman>> getStuntmen(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String name) {
        return
                find(
                        "SELECT s FROM Stuntman s JOIN s.countries c WHERE c.id = ?1 AND LOWER(s.name) LIKE LOWER(?2)",
                        Sort.by(sort, direction),
                        id, MessageFormat.format("%{0}%", name)
                )
                        .page(pageIndex, size)
                        .list();
    }

    /*public Uni<Set<Person>> addPerson(Person person) {
        return
                Mutiny.fetch(persons)
                        .map(
                                people -> {
                                    people.add(person);
                                    return people;
                                }
                        )
                ;
    }*/

    public Uni<Set<Movie>> removeMovie(Long id) {
        return
                Mutiny.fetch(movies)
                        .invoke(
                                fetchedMovies -> {
                                    fetchedMovies.removeIf(movie -> Objects.equals(movie.id, id));
                                    log.info("Removed movie with id {} from country {}", id, this);
                                }
                        )
                ;
    }

    /*public Uni<Set<Person>> removePerson(Long id) {
        return
                Mutiny.fetch(persons)
                        .map(
                                fetchPersons -> {
                                    fetchPersons.removeIf(person -> Objects.equals(person.id, id));
                                    return fetchPersons;
                                }
                        )
                ;
    }*/

    public static Uni<Country> update(Long id, CountryDTO countryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                getById(id)
                                        .onItem().ifNotNull().invoke(
                                                entity -> {
                                                    entity.setCode(countryDTO.getCode());
                                                    entity.setAlpha2(countryDTO.getAlpha2());
                                                    entity.setAlpha3(countryDTO.getAlpha3());
                                                    entity.setNomEnGb(countryDTO.getNomEnGb());
                                                    entity.setNomFrFr(countryDTO.getNomFrFr());
                                                }
                                        )
                        )
                ;
    }
}
