package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CategoryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.entity.Category;
import org.desha.app.mapper.CategoryMapper;
import org.desha.app.mapper.MovieMapper;
import org.desha.app.repository.CategoryRepository;
import org.desha.app.repository.MovieRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final MovieMapper movieMapper;
    private final CategoryRepository categoryRepository;
    private final MovieRepository movieRepository;

    @Inject
    public CategoryService(
            CategoryMapper categoryMapper,
            MovieMapper movieMapper,
            CategoryRepository categoryRepository,
            MovieRepository movieRepository
    ) {
        this.categoryMapper = categoryMapper;
        this.movieMapper = movieMapper;
        this.categoryRepository = categoryRepository;
        this.movieRepository = movieRepository;
    }

    public Uni<Long> count(String term) {
        return categoryRepository.countCategories(term);
    }

    public Uni<Category> getById(Long id) {
        return categoryRepository.findById(id);
    }

    public Uni<List<Category>> getAll() {
        return categoryRepository.listAll();
    }

    public Uni<Long> countMovies(Long id, String term) {
        return movieRepository.countMoviesByCategory(id, term);
    }

    public Uni<List<CategoryDTO>> getCategories(Page page, String sort, Sort.Direction direction, String term) {
        return
                categoryRepository
                        .findCategories(page, sort, direction, term)
                        .map(categoryMapper::toDTOList)
                ;
    }

    public Uni<Set<Category>> getByIds(List<Long> ids) {
        return categoryRepository.findByIds(ids).map(HashSet::new);
    }

    public Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO) {
        return
                movieRepository.findMoviesByCategory(id, page, sort, direction, criteriasDTO)
                        .map(movieWithAwardsNumberList ->
                                movieWithAwardsNumberList
                                        .stream()
                                        .map(movieMapper::movieWithAwardsNumberToMovieDTO)
                                        .toList()

                        )
                ;
    }

    /**
     * Crée une nouvelle catégorie à partir des données fournies.
     * <p>
     * Cette méthode convertit un {@link CategoryDTO} en entité {@link Category} et le persiste dans la
     * base de données. L'opération est effectuée dans une transaction.
     *
     * @param categoryDTO L'objet contenant les informations de la catégorie à créer.
     * @return Un {@link Uni} contenant la catégorie créée après sa persistance.
     */
    public Uni<Category> create(CategoryDTO categoryDTO) {
        return
                Panache
                        .withTransaction(() -> {
                            Category category = Category.build(categoryDTO.getId(), categoryDTO.getName());
                            return category.persist();
                        })
                ;
    }

    /**
     * Met à jour une catégorie existante en modifiant son nom.
     * <p>
     * Cette méthode recherche une catégorie par son identifiant et met à jour son nom
     * avec la valeur fournie dans {@link  CategoryDTO}. Si aucune catégorie n'est trouvée,
     * une exception est levée. L'opération est effectuée dans une transaction.
     *
     * @param id          L'identifiant de la catégorie à mettre à jour.
     * @param categoryDTO L'objet contenant les nouvelles données de la catégorie.
     * @return Un {@link Uni} contenant l'entité mise à jour.
     * @throws IllegalArgumentException si aucune catégorie n'est trouvée avec l'identifiant donné.
     */
    public Uni<Category> update(Long id, CategoryDTO categoryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                categoryRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Catégorie introuvable"))
                                        .invoke(category -> category.setName(StringUtils.capitalize(categoryDTO.getName().trim())))
                                        .flatMap(category -> categoryRepository.findById(category.getId()))
                        )
                ;
    }

    /**
     * Supprime une catégorie par son identifiant.
     * <p>
     * Cette méthode recherche une catégorie par son identifiant et tente de la supprimer.
     * Si aucune catégorie correspondante n'est trouvée, une exception est levée.
     * L'opération est effectuée dans une transaction.
     *
     * @param id L'identifiant de la catégorie à supprimer.
     * @return Un {@link Uni} contenant `true` si la suppression a réussi, `false` sinon.
     * @throws IllegalArgumentException si aucune catégorie n'est trouvée avec l'identifiant donné.
     */
    public Uni<Boolean> deleteCategory(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                categoryRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Catégorie introuvable"))
                        )
                ;
    }
}
