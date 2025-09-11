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
import org.desha.app.domain.dto.PersonDTO;
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
import java.util.Set;
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
     * Récupère un ensemble de techniciens associés à un film spécifique.
     *
     * @param id                L'identifiant du film.
     * @param techniciansGetter Fonction permettant de récupérer le bon ensemble de techniciens depuis l'entité {@link Movie}.
     * @param errorMessage      Message d'erreur en cas de liste non initialisée.
     * @return Un {@link Uni} contenant un {@link Set} de {@link PersonDTO} correspondant aux techniciens du film.
     * @throws IllegalArgumentException Si le film n'existe pas.
     * @throws IllegalStateException    Si la liste des techniciens n'est pas initialisée pour ce film.
     */
    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> getMovieTechniciansByMovie(Long id, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
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
                                    log.error("Erreur lors de la récupération des techniciens du film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la récupération du casting", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> saveTechnicians(
            Long id,
            List<MovieTechnicianDTO> movieTechnicianDTOList,
            Function<Movie, List<T>> techniciansGetter,
            BiFunction<Movie, MovieTechnicianDTO, Uni<T>> asyncTechnicianFactory,
            String errorMessage
    ) {
        return
                Panache.withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .chain(movie ->
                                                Mutiny.fetch(techniciansGetter.apply(movie))
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
                                        .flatMap(movie -> fetchAndMapTechniciansList(movie, techniciansGetter, errorMessage))
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour des techniciens pour le film {}", id, throwable);
                                    return new WebApplicationException("Erreur lors de la mise à jour des techniciens", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Ajoute des personnes à un film en fonction d'un ensemble de DTO et d'un service associé.
     *
     * @param id                     L'identifiant du film auquel les personnes doivent être ajoutées.
     * @param movieTechnicianDTOList L'ensemble des personnes à ajouter, sous forme de DTO.
     * @param techniciansGetter      Une fonction permettant de récupérer l'ensemble des personnes déjà associées au film.
     * @param nullCheckErrorMessage  Le message d'erreur à utiliser en cas d'échec de l'opération.
     * @return Une instance de {@link Uni} contenant l'ensemble des personnes ajoutées sous forme de {@link PersonDTO}.
     * En cas d'erreur, une exception est levée avec un message approprié.
     * @throws IllegalArgumentException Si le film n'est pas trouvé ou si certaines personnes sont introuvables.
     * @throws IllegalStateException    Si une erreur se produit lors de la récupération des personnes après la mise à jour.
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
                            return new MovieUpdateException(globalErrorMessage, throwable);
                        })
                ;
    }

    /**
     * Retire une personne spécifique d'un film.
     *
     * @param movieId               L'identifiant du film.
     * @param personId              L'identifiant de la personne à retirer.
     * @param techniciansGetter     Fonction permettant d'obtenir la liste des personnes à modifier depuis l'entité {@link Movie}.
     * @param nullCheckErrorMessage Message d'erreur si la liste des personnes n'est pas initialisée.
     * @return Une {@link Uni} contenant un {@link Set} de {@link PersonDTO} :
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws IllegalStateException    si la collection de personnes n'est pas initialisée pour ce film.
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
                                        .call(movie -> movie.removeTechnician(techniciansGetter, personId, nullCheckErrorMessage))
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
     * Vide un ensemble de personnes (acteurs, réalisateurs, etc.) associé à un film.
     * <p>
     * Cette méthode permet de vider un ensemble spécifique de personnes associées à un film (comme les acteurs, réalisateurs, etc.).
     * Elle récupère cet ensemble en appliquant une fonction (peopleGetter) sur le film et, si l'ensemble est initialisé,
     * elle appelle la méthode `clearPersons` sur le film pour le vider. Si l'ensemble est nul ou si le film n'est pas trouvé,
     * une exception est levée. Enfin, le film est persisté après l'opération.
     *
     * @param id                L'identifiant du film dont les personnes associées doivent être supprimées.
     * @param techniciansGetter Une fonction permettant d'obtenir l'ensemble des personnes à partir du film (par exemple, acteurs ou réalisateurs).
     * @param errorMessage      Le message d'erreur à utiliser si l'ensemble des personnes est nul.
     * @return Un {@link Uni} contenant `true` si l'opération a été réalisée avec succès.
     * @throws IllegalArgumentException Si le film n'est pas trouvé.
     * @throws WebApplicationException  Si une erreur se produit lors de la suppression des personnes.
     */
    public <T extends MovieTechnician> Uni<Boolean> clearTechnicians(Long id, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Panache
                        .withTransaction(() ->
                                movieRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_FILM))
                                        .call(movie -> movie.clearPersons(techniciansGetter.apply(movie), errorMessage))
                                        .call(movieRepository::persist)
                                        .map(movie -> true)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression des techniciens", throwable);
                            return new WebApplicationException("Erreur lors de la suppression des techniciens", Response.Status.INTERNAL_SERVER_ERROR);
                        });
    }

    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> fetchAndMapTechniciansList(Movie movie, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter.apply(movie))
                        .onItem().ifNull().failWith(() -> new WebApplicationException(errorMessage))
                        .map(movieTechnicianMapper::toDTOList)
//                        .map(tList ->
//                                tList.stream()
//                                        .map(movieTechnicianMapper::toMovieTechnicianDTO)
//                                        .toList()
//                        )
                ;
    }
}
