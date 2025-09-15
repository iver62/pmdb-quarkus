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
                                                        .onItem().ifNull().failWith(() -> new IllegalStateException(nullCheckErrorMessage))
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

    public <T extends MovieTechnician> Uni<List<MovieTechnicianDTO>> fetchAndMapTechniciansList(Movie movie, Function<Movie, List<T>> techniciansGetter, String errorMessage) {
        return
                Mutiny.fetch(techniciansGetter.apply(movie))
                        .onItem().ifNull().failWith(() -> new WebApplicationException(errorMessage))
                        .map(movieTechnicianMapper::toDTOList)
                ;
    }
}
