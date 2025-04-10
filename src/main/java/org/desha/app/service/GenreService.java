package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.GenreDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Genre;
import org.desha.app.repository.GenreRepository;
import org.desha.app.repository.MovieRepository;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    @Inject
    public GenreService(
            GenreRepository genreRepository,
            MovieRepository movieRepository
    ) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    public Uni<Long> count(String term) {
        return genreRepository.count(term);
    }

    public Uni<Genre> getById(Long id) {
        return genreRepository.findById(id);
    }

    public Uni<List<Genre>> getAll() {
        return genreRepository.listAll();
    }

    public Uni<Long> countMovies(Long id, String term) {
        return movieRepository.countMoviesByGenre(id, term);
    }

    public Uni<List<GenreDTO>> getGenres(String sort, Sort.Direction direction, String term) {
        return
                genreRepository
                        .findGenres(sort, direction, term)
                        .map(this::fromGenreListEntity)
                ;
    }

    public Uni<Set<Genre>> getByIds(Set<GenreDTO> genreSet) {
        return
                genreRepository.findByIds(
                        Optional.ofNullable(genreSet).orElse(Collections.emptySet())
                                .stream()
                                .map(GenreDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<Set<Genre>> getByIds(List<Long> ids) {
        return genreRepository.findByIds(ids).map(HashSet::new);
    }

    public Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findMoviesByGenre(id, page, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()

                        )
//                        .onFailure().recoverWithItem(Collections.emptyList())
                ;
    }

    /**
     * Crée un nouveau genre à partir des données fournies.
     * <p>
     * Cette méthode convertit un {@link GenreDTO} en entité {@link Genre} et le persiste dans la
     * base de données. L'opération est effectuée dans une transaction.
     *
     * @param genreDTO L'objet contenant les informations du genre à créer.
     * @return Un {@link Uni} contenant le genre créé après sa persistance.
     */
    public Uni<Genre> create(GenreDTO genreDTO) {
        return
                Panache
                        .withTransaction(() -> {
                            Genre genre = Genre.fromDTO(genreDTO);
                            genre.setName(StringUtils.capitalize(genre.getName().trim())); // Normalisation du nom
                            return genre.persist();
                        })
                ;
    }

    /**
     * Met à jour un genre existant en modifiant son nom.
     * <p>
     * Cette méthode recherche un genre par son identifiant et met à jour son nom
     * avec la valeur fournie dans {@code genreDTO}. Si aucun genre n'est trouvé,
     * une exception est levée. L'opération est effectuée dans une transaction.
     *
     * @param id       L'identifiant du genre à mettre à jour.
     * @param genreDTO L'objet contenant les nouvelles données du genre.
     * @return Un {@link Uni} contenant l'entité mise à jour.
     * @throws IllegalArgumentException si aucun genre n'est trouvé avec l'identifiant donné.
     */
    public Uni<Genre> update(Long id, GenreDTO genreDTO) {
        return
                Panache
                        .withTransaction(() ->
                                genreRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Genre introuvable"))
                                        .invoke(entity -> entity.setName(StringUtils.capitalize(genreDTO.getName().trim())))
                                        .flatMap(genre -> genreRepository.findById(genre.getId()))
                        )
                ;
    }

    /**
     * Supprime un genre par son identifiant.
     * <p>
     * Cette méthode recherche un genre par son identifiant et tente de le supprimer.
     * Si aucun genre correspondant n'est trouvé, une exception est levée.
     * L'opération est effectuée dans une transaction.
     *
     * @param id L'identifiant du genre à supprimer.
     * @return Un {@link Uni} contenant `true` si la suppression a réussi, `false` sinon.
     * @throws IllegalArgumentException si aucun genre n'est trouvé avec l'identifiant donné.
     */
    public Uni<Boolean> deleteGenre(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                genreRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Genre introuvable"))
                        )
                ;
    }

    public List<GenreDTO> fromGenreListEntity(List<Genre> genreList) {
        return
                genreList
                        .stream()
                        .map(GenreDTO::fromEntity)
                        .toList()
                ;
    }

    public Set<GenreDTO> fromGenreSetEntity(Set<Genre> genreSet) {
        return
                genreSet
                        .stream()
                        .map(GenreDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
