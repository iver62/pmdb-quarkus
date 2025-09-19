package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.CeremonyAwards;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.mapper.AwardMapper;
import org.desha.app.mapper.CeremonyAwardsMapper;
import org.desha.app.repository.CeremonyAwardsRepository;
import org.desha.app.utils.Messages;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class CeremonyAwardsService {

    private final AwardMapper awardMapper;
    private final CeremonyAwardsMapper ceremonyAwardsMapper;
    private final AwardService awardService;
    private final CeremonyService ceremonyService;
    private final CeremonyAwardsRepository ceremonyAwardsRepository;

    @Inject
    public CeremonyAwardsService(
            AwardMapper awardMapper,
            CeremonyAwardsMapper ceremonyAwardsMapper,
            AwardService awardService,
            CeremonyService ceremonyService,
            CeremonyAwardsRepository ceremonyAwardsRepository
    ) {
        this.awardMapper = awardMapper;
        this.ceremonyAwardsMapper = ceremonyAwardsMapper;
        this.awardService = awardService;
        this.ceremonyService = ceremonyService;
        this.ceremonyAwardsRepository = ceremonyAwardsRepository;
    }

    /**
     * Crée une nouvelle entité {@link CeremonyAwards} à partir des informations fournies dans un {@link CeremonyAwardsDTO}
     * et l’associe au film spécifié.
     * <p>
     * Cette méthode :
     * <ul>
     *   <li>Recherche ou crée la cérémonie via {@code ceremonyService.findOrCreateCeremony}.</li>
     *   <li>Construit un objet {@link CeremonyAwards} lié au film.</li>
     *   <li>Ajoute les récompenses ({@link Award}) décrites dans le DTO, en les associant à la cérémonie.</li>
     *   <li>Associe, le cas échéant, les personnes concernées à chaque récompense en les récupérant depuis {@code personMap}.</li>
     * </ul>
     * Les erreurs internes sont journalisées et transformées en exceptions HTTP appropriées.
     *
     * @param movie             Le film auquel rattacher les récompenses (non nul).
     * @param ceremonyAwardsDTO Les données de la cérémonie et des récompenses à créer.
     * @param personMap         Un cache des personnes existantes, indexées par leur identifiant, utilisé pour rattacher les {@link Person}.
     * @return Un {@link Uni} émettant l’entité {@link CeremonyAwards} nouvellement créée avec ses récompenses associées.
     * @throws WebApplicationException avec {@link Response.Status#INTERNAL_SERVER_ERROR} en cas d’erreur inattendue lors de la création de la cérémonie ou des récompenses.
     */
    public Uni<CeremonyAwards> createNewCeremonyAwards(Movie movie, CeremonyAwardsDTO ceremonyAwardsDTO, Map<Long, Person> personMap) {
        return
                ceremonyService.findOrCreateCeremony(ceremonyAwardsDTO.getCeremony())
                        .map(ceremony -> CeremonyAwards.build(ceremony, movie, new ArrayList<>()))
                        .invoke(ceremonyAwards -> {
                                    if (Objects.nonNull(ceremonyAwardsDTO.getAwards())) {
                                        for (AwardDTO awardDTO : ceremonyAwardsDTO.getAwards()) {
                                            Award award = awardMapper.toEntity(awardDTO);
                                            award.setCeremonyAwards(ceremonyAwards);

                                            if (Objects.nonNull(awardDTO.getPersons())) {
                                                Set<Person> linkedPersons = awardDTO.getPersons().stream()
                                                        .map(LitePersonDTO::getId)
                                                        .map(personMap::get)
                                                        .filter(Objects::nonNull)
                                                        .collect(Collectors.toSet());

                                                award.setPersonSet(linkedPersons);
                                            }

                                            ceremonyAwards.getAwards().add(award);
                                        }
                                    }
                                }
                        )
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la création des récompenses pour la cérémonie", throwable);
                                    return new WebApplicationException(
                                            "Impossible de créer les récompenses pour la cérémonie",
                                            Response.Status.INTERNAL_SERVER_ERROR
                                    );
                                }
                        )
                ;
    }

    /**
     * Ajoute une liste de récompenses à une cérémonie existante identifiée par son ID.
     * <p>
     * Cette méthode :
     * <ul>
     *   <li>Recherche la cérémonie correspondante en base via {@link CeremonyAwardsRepository#findById(Object)}.</li>
     *   <li>Si la cérémonie n’existe pas, lève une exception avec le message {@code Messages.NOT_FOUND_CEREMONY}.</li>
     *   <li>Récupère les personnes liées aux récompenses fournies grâce au service {@code awardService}.</li>
     *   <li>Associe les récompenses et les personnes à la cérémonie via {@link CeremonyAwards#addAwards(List, java.util.Map)}.</li>
     *   <li>Persiste et flush l’entité mise à jour en base de données.</li>
     *   <li>Retourne un {@link CeremonyAwardsDTO} représentant l’état de la cérémonie avec ses récompenses mises à jour.</li>
     * </ul>
     *
     * @param id           L’identifiant unique de la cérémonie.
     * @param awardDTOList La liste des récompenses à associer à la cérémonie.
     * @return Un {@link Uni} émettant le {@link CeremonyAwardsDTO} mis à jour avec les nouvelles récompenses.
     * @throws IllegalArgumentException si aucune cérémonie n’est trouvée pour l’ID donné.
     * @throws WebApplicationException  avec {@link Response.Status#INTERNAL_SERVER_ERROR} si une erreur inattendue survient lors de l’ajout ou de la persistance des récompenses.
     */
    public Uni<CeremonyAwardsDTO> addAwards(Long id, List<AwardDTO> awardDTOList) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_CEREMONY))
                                        .chain(
                                                ceremonyAwards ->
                                                        awardService.getPersonsByAwards(awardDTOList)
                                                                .map(personList ->
                                                                        personList.stream()
                                                                                .collect(Collectors.toMap(Person::getId, p -> p))
                                                                )
                                                                .chain(personMap ->
                                                                        Mutiny.fetch(ceremonyAwards.getAwards())
                                                                                .invoke(awardList -> ceremonyAwards.addAwards(awardDTOList, personMap))
                                                                                .replaceWith(ceremonyAwards)
                                                                )
                                        )
                                        .call(ceremonyAwardsRepository::persist)
                                        .call(ceremonyAwardsRepository::flush)
                                        .map(ceremonyAwardsMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de l'ajout des récompenses pour la cérémonie avec l'ID {}", id, throwable);
                                    return new WebApplicationException(
                                            "Impossible d'ajouter les récompenses pour la cérémonie",
                                            Response.Status.INTERNAL_SERVER_ERROR
                                    );
                                }
                        )
                ;
    }

    /**
     * Supprime une récompense spécifique d’une cérémonie existante identifiée par son ID.
     * <p>
     * Cette méthode :
     * <ul>
     *   <li>Recherche la cérémonie correspondante en base via {@link CeremonyAwardsRepository#findById(Object)}.</li>
     *   <li>Si la cérémonie n’existe pas, lève une exception {@link NotFoundException} avec le message {@code Messages.NOT_FOUND_CEREMONY}.</li>
     *   <li>Charge la liste des récompenses associées à la cérémonie et retire celle correspondant à l’ID fourni.</li>
     *   <li>Persiste l’entité mise à jour en base de données.</li>
     *   <li>Retourne un {@link CeremonyAwardsDTO} représentant l’état actuel de la cérémonie après suppression.</li>
     * </ul>
     *
     * @param ceremonyAwardsId L’identifiant unique de la cérémonie.
     * @param awardId          L’identifiant de la récompense à supprimer.
     * @return Un {@link Uni} émettant le {@link CeremonyAwardsDTO} mis à jour après suppression de la récompense.
     * @throws NotFoundException       si aucune cérémonie n’est trouvée pour l’ID donné.
     * @throws WebApplicationException avec {@link Response.Status#INTERNAL_SERVER_ERROR} si une erreur inattendue survient lors de la suppression ou de la persistance des récompenses.
     */
    public Uni<CeremonyAwardsDTO> removeAward(Long ceremonyAwardsId, Long awardId) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(ceremonyAwardsId)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                                        .chain(ceremonyAwards ->
                                                Mutiny.fetch(ceremonyAwards.getAwards())
                                                        .invoke(awardList -> ceremonyAwards.removeAward(awardId))
                                                        .replaceWith(ceremonyAwards)
                                        )
                                        .call(ceremonyAwardsRepository::persist)
                                        .map(ceremonyAwardsMapper::toDTO)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression de la récompense {} pour la cérémonie avec l’ID {}", awardId, ceremonyAwardsId, throwable);
                            return new WebApplicationException(
                                    "Impossible de supprimer la récompense pour la cérémonie",
                                    Response.Status.INTERNAL_SERVER_ERROR
                            );
                        })
                ;
    }

    /**
     * Supprime toutes les récompenses associées à une cérémonie existante identifiée par son ID.
     * <p>
     * Cette méthode :
     * <ul>
     *   <li>Recherche la cérémonie correspondante en base via {@link CeremonyAwardsRepository#findById(Object)}.</li>
     *   <li>Si la cérémonie n’existe pas, lève une exception avec le message {@code Messages.NOT_FOUND_CEREMONY}.</li>
     *   <li>Charge la liste des récompenses liées à la cérémonie et les supprime via {@link CeremonyAwards#clearAwards()}.</li>
     *   <li>Persiste l’entité mise à jour en base de données.</li>
     *   <li>Retourne {@code true} si l’opération s’est déroulée avec succès.</li>
     * </ul>
     *
     * @param id L’identifiant unique de la cérémonie.
     * @return Un {@link Uni} émettant {@code true} si toutes les récompenses ont été supprimées avec succès.
     * @throws IllegalArgumentException si aucune cérémonie n’est trouvée pour l’ID donné.
     * @throws WebApplicationException  avec {@link Response.Status#INTERNAL_SERVER_ERROR} si une erreur inattendue survient lors de la suppression ou de la persistance des récompenses.
     */
    public Uni<Boolean> clearAwards(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                                        .chain(ceremonyAwards ->
                                                Mutiny.fetch(ceremonyAwards.getAwards())
                                                        .invoke(awardList -> ceremonyAwards.clearAwards())
                                                        .replaceWith(ceremonyAwards)
                                        )
                                        .chain(ceremonyAwardsRepository::persist)
                                        .map(ceremonyAwards -> true)
                        )
                        .onFailure().transform(throwable -> {
                            if (throwable instanceof WebApplicationException) {
                                return throwable;
                            }
                            log.error("Erreur lors de la suppression de toutes les récompenses pour la cérémonie avec l’ID {}", id, throwable);
                            return new WebApplicationException(
                                    "Impossible de supprimer toutes les récompenses pour la cérémonie",
                                    Response.Status.INTERNAL_SERVER_ERROR
                            );
                        })
                ;
    }

}
