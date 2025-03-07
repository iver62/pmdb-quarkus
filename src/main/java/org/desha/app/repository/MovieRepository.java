package org.desha.app.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.FiltersDTO;
import org.desha.app.domain.entity.Movie;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class MovieRepository implements PanacheRepositoryBase<Movie, Long> {

    public Uni<Long> countMovies(FiltersDTO filtersDTO) {
        String query = "FROM Movie m WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByActor(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.movieActors ma WHERE ma.actor.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByProducer(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.producers p WHERE p.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByDirector(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.directors d WHERE d.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByScreenwriter(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.screenwriters s WHERE s.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByMusician(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.musicians mu WHERE mu.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByDecorator(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.decorators d WHERE d.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByCostumier(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.costumiers c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByPhotographer(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.photographers p WHERE p.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByEditor(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.editors e WHERE e.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByCaster(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.casters c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByArtDirector(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.artDirectors ad WHERE ad.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesBySoundEditor(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.soundEditors se WHERE se.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByVisualEffectsSupervisor(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.visualEffectsSupervisors ves WHERE ves.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByMakeupArtist(long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.makeupArtists ma WHERE ma.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByHairDresser(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.hairDressers hd WHERE hd.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByStuntman(Long id, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.stuntmen s WHERE s.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return count(query, params);
    }

    public Uni<Long> countMoviesByCountry(Long id, String term) {
        return
                count(
                        "SELECT COUNT(m) FROM Movie m JOIN m.countries c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)",
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                );
    }

    public Uni<Long> countMoviesByGenre(Long id, String term) {
        return
                count(
                        "SELECT COUNT(m) FROM Movie m JOIN m.genres g WHERE g.id = :id AND LOWER(m.title) LIKE LOWER(:term)",
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                );
    }

    public Uni<Movie> findByIdWithCountriesAndGenres(Long id) {
        return
                find("FROM Movie m LEFT JOIN FETCH m.countries LEFT JOIN FETCH m.genres WHERE m.id = ?1", id)
                        .firstResult()
                        .onItem().ifNull().failWith(() -> new NotFoundException("Film introuvable avec l'ID : " + id))
                ;
    }

    public Uni<Movie> findByIdWithTechnicalTeam(Long id) {
        return
                find("FROM Movie m " +
                        "LEFT JOIN FETCH m.producers " +
                        "LEFT JOIN FETCH m.directors " +
                        "LEFT JOIN FETCH m.screenwriters " +
                        "LEFT JOIN FETCH m.musicians " +
                        "LEFT JOIN FETCH m.photographers " +
                        "LEFT JOIN FETCH m.costumiers " +
                        "LEFT JOIN FETCH m.decorators " +
                        "LEFT JOIN FETCH m.editors " +
                        "LEFT JOIN FETCH m.casters " +
                        "LEFT JOIN FETCH m.artDirectors " +
                        "LEFT JOIN FETCH m.soundEditors " +
                        "LEFT JOIN FETCH m.visualEffectsSupervisors " +
                        "LEFT JOIN FETCH m.makeupArtists " +
                        "LEFT JOIN FETCH m.hairDressers " +
                        "LEFT JOIN FETCH m.stuntmen " +
                        "WHERE m.id = ?1", id
                )
                        .firstResult()
                        .onItem().ifNull().failWith(() -> new NotFoundException("Film introuvable avec l'ID : " + id))
                ;
    }

    public Uni<List<Movie>> findMovies(int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m WHERE LOWER(FUNCTION('unaccent', m.title)) LIKE LOWER(FUNCTION('unaccent', :term))" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by(sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findByTitle(String title) {
        return list("title", title);
    }

    public Uni<List<Movie>> findMoviesByActor(Long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.movieActors ma JOIN ma.actor a WHERE a.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByProducer(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.producers p WHERE p.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByDirector(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.directors d WHERE d.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByScreenwriter(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.screenwriters s WHERE s.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByMusician(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.musicians mu WHERE mu.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByDecorator(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.decorators d WHERE d.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByCostumier(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.costumiers c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByPhotographer(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.photographers p WHERE p.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByEditor(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.editors e WHERE e.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByCaster(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.casters c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByArtDirector(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.artDirectors ad WHERE ad.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesBySoundEditor(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.soundEditors se WHERE se.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByVisualEffectsSupervisor(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.visualEffectsSupervisors ves WHERE ves.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByMakeupArtist(Long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.makeupArtists ma WHERE ma.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByHairDresser(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.hairDressers hd WHERE hd.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findMoviesByStuntman(long id, int pageIndex, int size, String sort, Sort.Direction direction, FiltersDTO filtersDTO) {
        String query = "FROM Movie m JOIN m.stuntmen s WHERE s.id = :id AND LOWER(m.title) LIKE LOWER(:term)" +
                addClauses(filtersDTO);

        Parameters params = addParameters(
                Parameters.with("id", id)
                        .and("term", "%" + filtersDTO.getTerm() + "%"),
                filtersDTO
        );

        return
                find(query, Sort.by("m." + sort, direction), params)
                        .page(pageIndex, size)
                        .list()
                ;
    }

    public Uni<List<Movie>> findAllMoviesByCountry(Long id, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "SELECT m FROM Movie m JOIN m.countries c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)",
                        Sort.by(sort, direction),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                ).list();
    }

    public Uni<List<Movie>> findMoviesByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "SELECT m FROM Movie m JOIN m.countries c WHERE c.id = :id AND LOWER(m.title) LIKE LOWER(:term)",
                        Sort.by(sort, direction),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                )
                        .page(pageIndex, size)
                        .list();
    }

    public Uni<List<Movie>> findMoviesByGenre(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                find(
                        "SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :id AND LOWER(m.title) LIKE LOWER(:term)",
                        Sort.by(sort, direction),
                        Parameters.with("id", id)
                                .and("term", "%" + term + "%")
                )
                        .page(pageIndex, size)
                        .list()
                ;
    }

    private String addClauses(FiltersDTO filtersDTO) {
        StringBuilder query = new StringBuilder();

        Optional.ofNullable(filtersDTO.getFromReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate >= :fromReleaseDate"));
        Optional.ofNullable(filtersDTO.getToReleaseDate()).ifPresent(date -> query.append(" AND m.releaseDate <= :toReleaseDate"));
        Optional.ofNullable(filtersDTO.getFromCreationDate()).ifPresent(date -> query.append(" AND m.creationDate >= :fromCreationDate"));
        Optional.ofNullable(filtersDTO.getToCreationDate()).ifPresent(date -> query.append(" AND m.creationDate <= :toCreationDate"));
        Optional.ofNullable(filtersDTO.getFromLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate >= :fromLastUpdate"));
        Optional.ofNullable(filtersDTO.getToLastUpdate()).ifPresent(date -> query.append(" AND m.lastUpdate <= :toLastUpdate"));

        if (Objects.nonNull(filtersDTO.getGenreIds()) && !filtersDTO.getGenreIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.genres g WHERE g.id IN :genreIds)");
        }

        if (Objects.nonNull(filtersDTO.getCountryIds()) && !filtersDTO.getCountryIds().isEmpty()) {
            query.append(" AND EXISTS (SELECT 1 FROM m.countries c WHERE c.id IN :countryIds)");
        }

        return query.toString();
    }

    private Parameters addParameters(Parameters params, FiltersDTO filtersDTO) {
        if (Objects.nonNull(filtersDTO.getFromReleaseDate())) {
            params.and("fromReleaseDate", filtersDTO.getFromReleaseDate());
        }
        if (Objects.nonNull(filtersDTO.getToReleaseDate())) {
            params.and("toReleaseDate", filtersDTO.getToReleaseDate());
        }
        if (Objects.nonNull(filtersDTO.getFromCreationDate())) {
            params.and("fromCreationDate", filtersDTO.getFromCreationDate());
        }
        if (Objects.nonNull(filtersDTO.getToCreationDate())) {
            params.and("toCreationDate", filtersDTO.getToCreationDate());
        }
        if (Objects.nonNull(filtersDTO.getFromLastUpdate())) {
            params.and("fromLastUpdate", filtersDTO.getFromLastUpdate());
        }
        if (Objects.nonNull(filtersDTO.getToLastUpdate())) {
            params.and("toLastUpdate", filtersDTO.getToLastUpdate());
        }

        if (Objects.nonNull(filtersDTO.getGenreIds()) && !filtersDTO.getGenreIds().isEmpty()) {
            params.and("genreIds", filtersDTO.getGenreIds());
        }
        if (Objects.nonNull(filtersDTO.getCountryIds()) && !filtersDTO.getCountryIds().isEmpty()) {
            params.and("countryIds", filtersDTO.getCountryIds());
        }

        return params;
    }
}
