package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.MovieTechnicianDTO;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.MovieTechnician;
import org.desha.app.domain.enums.NotificationType;
import org.desha.app.exception.MovieUpdateException;
import org.desha.app.mapper.MovieTechnicianMapper;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.MovieTechnicianRepository;
import org.desha.app.utils.Messages;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@ApplicationScoped
@Slf4j
public class MovieTechnicianService {

    private final MovieRepository movieRepository;
    private final MovieTechnicianRepository movieTechnicianRepository;
    private final MovieTechnicianMapper movieTechnicianMapper;
    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;

    @Inject
    public MovieTechnicianService(MovieRepository movieRepository, MovieTechnicianRepository movieTechnicianRepository, MovieTechnicianMapper movieTechnicianMapper, NotificationService notificationService, UserNotificationService userNotificationService) {
        this.movieRepository = movieRepository;
        this.movieTechnicianRepository = movieTechnicianRepository;
        this.movieTechnicianMapper = movieTechnicianMapper;
        this.notificationService = notificationService;
        this.userNotificationService = userNotificationService;
    }

    /**
     * Récupère la liste des techniciens d’un film donné et les convertit en DTO.
     * <p>
     * Cette méthode effectue les étapes suivantes :
     * <ul>
     *     <li>Recherche le film par son identifiant via {@link MovieRepository#findById(Long)}.
     *         Si aucun film n’est trouvé, une {@link NotFoundException} est levée.</li>
     *     <li>Récupère la liste des techniciens à partir du film en utilisant {@code techniciansGetter}.
     *         Si la liste est nulle, une {@link WebApplicationException} est levée avec le message fourni.</li>
     *     <li>Transforme la liste des techniciens en liste de {@link MovieTechnicianDTO} via {@code movieTechnicianMapper}.</li>
     *     <li>En cas d’exception non gérée, la méthode log l’erreur et renvoie une {@link WebApplicationException}
     *         avec le message global et le statut HTTP 500.</li>
     * </ul>
     *
     * @param <T>                le type de technicien du film héritant de {@link MovieTechnician}
     * @param id                 l’identifiant du film dont on souhaite récupérer les techniciens
     * @param techniciansGetter  fonction permettant d’extraire la liste des techniciens d’un {@link Movie}
     * @param errorMessage       message d’erreur à utiliser si la liste de techniciens est nulle
     * @param globalErrorMessage message d’erreur global en cas d’échec inattendu lors de la récupération
     * @return un {@link Uni} contenant la liste des {@link MovieTechnicianDTO} du film
     * @throws NotFoundException       si le film n’existe pas
     * @throws WebApplicationException si la liste des techniciens est nulle ou en cas d’erreur serveur
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> getMovieTechniciansByMovie(
            Long id,
            Function<Movie, List<T>> techniciansGetter,
            String errorMessage,
            String globalErrorMessage
    ) {
        return
                movieRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                        .flatMap(movie ->
                                Mutiny.fetch(techniciansGetter.apply(movie))
                                        .onItem().ifNull().failWith(() -> new WebApplicationException(errorMessage))
                                        .map(movieTechnicianMapper::toDTOList)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération des techniciens pour le film {}", id, throwable);
                                    return new WebApplicationException(globalErrorMessage, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Sauvegarde et met à jour la liste des techniciens pour un film donné.
     * <p>
     * Cette méthode effectue les étapes suivantes de manière transactionnelle :
     * <ul>
     *     <li>Recherche le film par son identifiant via {@link MovieRepository#findById(Long)}.
     *         Si aucun film n’est trouvé, une {@link NotFoundException} est levée.</li>
     *     <li>Récupère la liste des techniciens existants via {@code techniciansGetter}.
     *         Si la liste est nulle, une {@link WebApplicationException} est levée avec le message fourni.</li>
     *     <li>Supprime les techniciens obsolètes de la liste existante via {@link Movie#removeObsoleteTechnicians(List, List)}.</li>
     *     <li>Met à jour les techniciens existants via {@link Movie#updateExistingTechnicians(List, List)}.</li>
     *     <li>Ajoute les nouveaux techniciens à l’aide de {@code asyncTechnicianFactory} via {@link Movie#addTechnicians(List, Function, BiFunction)}.</li>
     *     <li>Persiste les modifications du film et force la génération des identifiants via {@link MovieTechnicianRepository#flush()}.</li>
     *     <li>Crée une notification sur la modification des techniciens et informe les administrateurs.</li>
     *     <li>Récupère enfin la liste mise à jour des techniciens et la convertit en {@link MovieTechnicianDTO}.</li>
     * </ul>
     *
     * @param <T>                    le type de technicien du film héritant de {@link MovieTechnician}
     * @param id                     l’identifiant du film pour lequel les techniciens doivent être mis à jour
     * @param movieTechnicianDTOList la liste des DTO représentant les techniciens à sauvegarder ou mettre à jour
     * @param techniciansGetter      fonction permettant d’extraire la liste des techniciens existants d’un {@link Movie}
     * @param asyncTechnicianFactory fonction asynchrone qui transforme un {@link MovieTechnicianDTO} en entité technicien spécifique au film
     * @param nullCheckErrorMessage  message d’erreur à utiliser si la liste de techniciens est nulle
     * @param globalErrorMessage     message d’erreur global en cas d’échec inattendu lors de la sauvegarde
     * @return un {@link Uni} contenant la liste mise à jour des {@link MovieTechnicianDTO} du film
     * @throws NotFoundException       si le film n’existe pas
     * @throws WebApplicationException si la liste des techniciens est nulle ou en cas d’erreur serveur
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> saveTechnicians(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(nullCheckErrorMessage))
                                                        .invoke(existingTechnicians -> movie.removeObsoleteTechnicians(existingTechnicians, movieTechnicianDTOList)) // Supprimer les techniciens obsolètes
                                                        .invoke(existingTechnicians -> movie.updateExistingTechnicians(existingTechnicians, movieTechnicianDTOList)) // Mettre à jour les techniciens existants
                                                        .chain(existingTechnicians -> movie.addTechnicians(movieTechnicianDTOList, techniciansGetter, asyncTechnicianFactory)) // Ajouter les nouveaux techniciens
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieTechnicianRepository::flush) // Force la génération des IDs
                                        .call(movie -> notificationService.createNotification("Les techniciens du film " + movie.getTitle() + " ont été modifiés.", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(movie -> fetchAndMapTechniciansList(movie, techniciansGetter, nullCheckErrorMessage))
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour des techniciens du film {}", id, throwable);
                                    return new WebApplicationException(globalErrorMessage, Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Ajoute une liste de techniciens à un film donné.
     * <p>
     * Cette méthode effectue les étapes suivantes de manière transactionnelle :
     * <ul>
     *     <li>Recherche le film par son identifiant via {@link MovieRepository#findById(Long)}.
     *         Si aucun film n’est trouvé, une {@link NotFoundException} est levée.</li>
     *     <li>Récupère la liste des techniciens existants via {@code techniciansGetter}.
     *         Si la liste est nulle, une {@link WebApplicationException} est levée avec le message fourni.</li>
     *     <li>Ajoute les nouveaux techniciens à l’aide de {@code asyncTechnicianFactory} via {@link Movie#addTechnicians(List, Function, BiFunction)}.</li>
     *     <li>Persiste les modifications du film et force la génération des identifiants via {@link MovieTechnicianRepository#flush()}.</li>
     *     <li>Crée une notification indiquant l’ajout des techniciens et informe les administrateurs.</li>
     *     <li>Récupère enfin la liste mise à jour des techniciens et la convertit en {@link MovieTechnicianDTO}.</li>
     * </ul>
     *
     * @param <T>                    le type de technicien du film héritant de {@link MovieTechnician}
     * @param id                     l’identifiant du film auquel les techniciens doivent être ajoutés
     * @param movieTechnicianDTOList la liste des DTO représentant les techniciens à ajouter
     * @param techniciansGetter      fonction permettant d’extraire la liste des techniciens existants d’un {@link Movie}
     * @param asyncTechnicianFactory fonction asynchrone qui transforme un {@link MovieTechnicianDTO} en entité technicien spécifique au film
     * @param nullCheckErrorMessage  message d’erreur à utiliser si la liste de techniciens est nulle
     * @param globalErrorMessage     message d’erreur global en cas d’échec inattendu lors de l’ajout
     * @return un {@link Uni} contenant la liste mise à jour des {@link MovieTechnicianDTO} du film
     * @throws NotFoundException       si le film n’existe pas
     * @throws WebApplicationException si la liste des techniciens est nulle
     * @throws MovieUpdateException    en cas d’erreur serveur lors de l’ajout des techniciens
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> addTechnicians(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory,
            String nullCheckErrorMessage,
            String globalErrorMessage
    ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(nullCheckErrorMessage))
                                                        .chain(existingTechnicians -> movie.addTechnicians(movieTechnicianDTOList, techniciansGetter, asyncTechnicianFactory)) // Ajouter les nouveaux techniciens
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .call(movieTechnicianRepository::flush) // Force la génération des IDs
                                        .call(movie -> notificationService.createNotification("Des techniciens ont été ajoutés au film " + movie.getTitle() + ".", NotificationType.INFO)
                                                .chain(userNotificationService::notifyAdmins)
                                        )
                                        .flatMap(movie -> fetchAndMapTechniciansList(movie, techniciansGetter, nullCheckErrorMessage))
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de l'ajout des techniciens du film {}", id, throwable);
                            return new MovieUpdateException(globalErrorMessage, throwable);
                        })
                ;
    }

    /**
     * Supprime un technicien spécifique d’un film donné.
     * <p>
     * Cette méthode effectue les étapes suivantes de manière transactionnelle :
     * <ul>
     *     <li>Recherche le film par son identifiant via {@link MovieRepository#findById(Long)}.
     *         Si aucun film n’est trouvé, une {@link NotFoundException} est levée.</li>
     *     <li>Récupère la liste des techniciens existants via {@code techniciansGetter}.
     *         Si la liste est nulle, une {@link WebApplicationException} est levée avec le message fourni.</li>
     *     <li>Supprime le technicien identifié par {@code personId} de la liste des techniciens du film via {@link Movie#removeTechnician(List, Long)}.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Récupère enfin la liste mise à jour des techniciens et la convertit en {@link MovieTechnicianDTO}.</li>
     * </ul>
     *
     * @param <T>                   le type de technicien du film héritant de {@link MovieTechnician}
     * @param movieId               l’identifiant du film dont on souhaite supprimer un technicien
     * @param personId              l’identifiant de la personne (technicien) à supprimer
     * @param techniciansGetter     fonction permettant d’extraire la liste des techniciens existants d’un {@link Movie}
     * @param nullCheckErrorMessage message d’erreur à utiliser si la liste des techniciens est nulle
     * @param globalMessage         message d’erreur global en cas d’échec inattendu lors de la suppression
     * @return un {@link Uni} contenant la liste mise à jour des {@link MovieTechnicianDTO} du film
     * @throws NotFoundException       si le film n’existe pas
     * @throws WebApplicationException si la liste des techniciens est nulle
     * @throws MovieUpdateException    en cas d’erreur serveur lors de la suppression du technicien
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> removeTechnician(
            Long movieId,
            Long personId,
            Function<Movie, List<T>> techniciansGetter,
            String nullCheckErrorMessage,
            String globalMessage
    ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(movieId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(nullCheckErrorMessage))
                                                        .invoke(techniciansList -> movie.removeTechnician(techniciansList, personId))
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .flatMap(movie -> fetchAndMapTechniciansList(movie, techniciansGetter, nullCheckErrorMessage))
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression du technicien {} du film {}", personId, movieId, throwable);
                            return new MovieUpdateException(globalMessage, throwable);
                        })
                ;
    }

    /**
     * Supprime tous les techniciens associés à un film donné.
     * <p>
     * Cette méthode effectue les étapes suivantes de manière transactionnelle :
     * <ul>
     *     <li>Recherche le film par son identifiant via {@link MovieRepository#findById(Long)}.
     *         Si aucun film n’est trouvé, une {@link NotFoundException} est levée.</li>
     *     <li>Récupère la liste des techniciens existants via {@code techniciansGetter}.
     *         Si la liste est nulle, une {@link WebApplicationException} est levée avec le message fourni.</li>
     *     <li>Supprime tous les techniciens du film via {@link Movie#clearTechnicians(List)}.</li>
     *     <li>Persiste les modifications du film.</li>
     *     <li>Renvoie {@code true} si l’opération s’est déroulée avec succès.</li>
     * </ul>
     * Les erreurs inattendues sont transformées en {@link WebApplicationException} avec le
     * message {@code globalMessage} et un statut HTTP 500.
     *
     * @param <T>                   le type de technicien du film héritant de {@link MovieTechnician}
     * @param id                    l’identifiant du film dont on souhaite supprimer tous les techniciens
     * @param techniciansGetter     fonction permettant d’extraire la liste des techniciens existants d’un {@link Movie}
     * @param nullCheckErrorMessage message d’erreur à utiliser si la liste des techniciens est nulle
     * @param globalMessage         message d’erreur global en cas d’échec inattendu lors de la suppression
     * @return un {@link Uni} contenant {@code true} si tous les techniciens ont été supprimés avec succès
     * @throws NotFoundException       si le film n’existe pas
     * @throws WebApplicationException si la liste des techniciens est nulle ou en cas d’erreur serveur
     */
    public <T extends MovieTechnician> Uni<Boolean> clearTechnicians(
            Long id,
            Function<Movie, List<T>> techniciansGetter,
            String nullCheckErrorMessage,
            String globalMessage
    ) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
                                                        .onItem().ifNull().failWith(() -> new WebApplicationException(nullCheckErrorMessage))
                                                        .invoke(movie::clearTechnicians)
                                                        .replaceWith(movie)
                                        )
                                        .chain(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression de tous les techniciens pour le film {}", id, throwable);
                            return new WebApplicationException(globalMessage, Response.Status.INTERNAL_SERVER_ERROR);
                        });
    }

    /**
     * Récupère la liste des techniciens d’un film et la transforme en DTO.
     * <p>
     * Cette méthode effectue les étapes suivantes :
     * <ul>
     *     <li>Récupère la liste des techniciens du film via {@code techniciansGetter}.</li>
     *     <li>Si la liste est nulle, lève une {@link WebApplicationException} avec le message fourni.</li>
     *     <li>Transforme chaque {@link MovieTechnician} en {@link MovieTechnicianDTO} via {@link MovieTechnicianMapper#toDTOList(List)}.</li>
     * </ul>
     *
     * @param <T>               le type de technicien du film héritant de {@link MovieTechnician}
     * @param movie             le film dont on souhaite récupérer les techniciens
     * @param techniciansGetter fonction permettant d’extraire la liste des techniciens existants du film
     * @param errorMessage      message d’erreur à utiliser si la liste des techniciens est nulle
     * @return un {@link Uni} contenant la liste des {@link MovieTechnicianDTO} du film
     * @throws WebApplicationException si la liste des techniciens est nulle
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> fetchAndMapTechniciansList(Movie movie, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter.apply(movie))
                        .onItem().ifNull().failWith(() -> new WebApplicationException(errorMessage))
                        .map(movieTechnicianMapper::toDTOList)
                ;
    }
}
