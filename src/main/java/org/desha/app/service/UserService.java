package org.desha.app.service;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.UserDTO;
import org.desha.app.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Délègue le comptage des utilisateurs au repository en appliquant un filtre optionnel.
     *
     * <p>Cette méthode permet de récupérer le nombre d'utilisateurs dont les données correspondent
     * au terme de recherche spécifié. Elle ne contient pas de logique métier, mais sert de point
     * d'entrée au niveau du service.</p>
     *
     * @param term le terme à utiliser pour filtrer les utilisateurs (par exemple nom ou email).
     *             Peut être {@code null} ou vide pour inclure tous les utilisateurs.
     * @return un {@link Uni} contenant le nombre d'utilisateurs correspondant au filtre
     */
    public Uni<Long> countUsers(String term) {
        return userRepository.countUsers(term);
    }

    /**
     * Récupère un utilisateur par son identifiant unique et le transforme en {@link UserDTO}.
     *
     * <p>Si aucun utilisateur correspondant à l'identifiant fourni n'est trouvé, une exception est levée.
     * Le DTO généré inclut également le nombre de films associés à cet utilisateur.</p>
     *
     * @param id l'identifiant unique de l'utilisateur à rechercher
     * @return un {@link Uni} contenant le {@link UserDTO} si l'utilisateur est trouvé
     * @throws IllegalArgumentException si aucun utilisateur correspondant n'est trouvé
     */
    public Uni<UserDTO> getUser(UUID id) {
        return
                userRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Utilisateur introuvable"))
                        .map(user -> UserDTO.of(user, user.getMovies().size()))
                ;
    }

    /**
     * Récupère une liste paginée d'utilisateurs correspondant aux critères de recherche spécifiés,
     * triée selon le champ et la direction donnés. Chaque utilisateur est transformé en {@link UserDTO},
     * incluant le nombre de films associés.
     *
     * @param page      les informations de pagination (index et taille de page)
     * @param sort      le champ sur lequel effectuer le tri
     * @param direction la direction du tri (ascendant ou descendant)
     * @param term      le terme de recherche (filtré sur le nom, l'email, etc.)
     * @return un {@link Uni} contenant une liste de {@link UserDTO}
     */
    public Uni<List<UserDTO>> getUsers(Page page, String sort, Sort.Direction direction, String term) {
        return
                userRepository.findUsers(page, sort, direction, term)
                        .map(users ->
                                users
                                        .stream()
                                        .map(user -> UserDTO.of(user, user.getMovies().size()))
                                        .toList()
                        )
                ;
    }

    /**
     * Récupère la liste complète des utilisateurs correspondant au terme de recherche spécifié.
     * La liste est d'abord récupérée depuis le repository selon les critères de tri donnés, puis
     * triée par nom d'utilisateur (`username`) côté application.
     *
     * @param sort      le champ sur lequel effectuer le tri dans la requête de base de données
     * @param direction la direction du tri (ASC ou DESC)
     * @param term      le terme de recherche pour filtrer les utilisateurs (nom, email, etc.)
     * @return un {@link Uni} contenant la liste triée de {@link UserDTO}
     */
    public Uni<List<UserDTO>> getUsers(String sort, Sort.Direction direction, String term) {
        return
                userRepository.findUsers(sort, direction, term)
                        .map(UserDTO::fromUserListEntity)
                ;
    }
}
