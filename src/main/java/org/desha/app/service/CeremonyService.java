package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.domain.entity.Ceremony;
import org.desha.app.mapper.CeremonyMapper;
import org.desha.app.repository.CeremonyRepository;
import org.desha.app.utils.Messages;

import java.util.Objects;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class CeremonyService {

    private final CeremonyMapper ceremonyMapper;
    private final CeremonyRepository ceremonyRepository;

    @Inject
    public CeremonyService(
            CeremonyMapper ceremonyMapper,
            CeremonyRepository ceremonyRepository
    ) {
        this.ceremonyMapper = ceremonyMapper;
        this.ceremonyRepository = ceremonyRepository;
    }

    /**
     * Récupère une cérémonie ({@link CeremonyDTO}) à partir de son identifiant.
     * <p>
     * Si aucune cérémonie ne correspond à l'identifiant fourni, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant de la cérémonie à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la {@link CeremonyDTO} correspondant à l’identifiant fourni.
     * @throws NotFoundException       si aucune cérémonie n’est trouvée pour l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération de la cérémonie.
     */
    @WithSession
    public Uni<CeremonyDTO> getCeremony(@NotNull Long id) {
        return
                ceremonyRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                        .map(ceremonyMapper::ceremonyToCeremonyDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération de la cérémonie", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération de la cérémonie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Récupère une liste paginée et triée de cérémonies, éventuellement filtrée par un terme de recherche,
     * et retourne le résultat sous forme de {@link CeremonyDTO}.
     * <p>
     * La recherche s'effectue sur le champ {@code name} et ignore la casse et les accents.
     * Si {@code term} est {@code null}, toutes les cérémonies sont retournées.
     * <p>
     * En cas d’erreur lors de la récupération, une exception {@link WebApplicationException} est levée
     * avec un statut HTTP 500.
     *
     * @param page      Les informations de pagination à appliquer (index et taille de page).
     * @param direction La direction du tri (ASC ou DESC), définie par {@link Sort.Direction}.
     * @param term      Un terme de recherche optionnel pour filtrer les cérémonies par nom. Peut être {@code null}.
     * @return Un {@link Uni} contenant un {@link Set} de {@link CeremonyDTO} correspondant aux critères fournis.
     * @throws WebApplicationException si une erreur survient lors de la récupération des cérémonies.
     */
    @WithSession
    public Uni<Set<CeremonyDTO>> getCeremonies(Page page, Sort.Direction direction, String term) {
        return
                ceremonyRepository.findCeremonies(page, direction, term)
                        .map(ceremonyMapper::toDTOSet)
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la récupération des cérémonies", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération des cérémonies", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Crée une nouvelle cérémonie à partir des informations fournies dans un {@link CeremonyDTO}.
     * <p>
     * La création est effectuée dans une transaction et retourne le {@link CeremonyDTO} correspondant à l’entité persistée.
     * <p>
     * En cas d’erreur lors de la création, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param ceremonyDTO Les données de la cérémonie à créer.
     * @return Un {@link Uni} contenant le {@link CeremonyDTO} correspondant à la cérémonie créée.
     * @throws WebApplicationException si une erreur survient lors de la création de la cérémonie.
     */
    public Uni<CeremonyDTO> create(CeremonyDTO ceremonyDTO) {
        return
                Panache
                        .withTransaction(() -> {
                            Ceremony ceremony = ceremonyMapper.ceremonyDTOtoCeremony(ceremonyDTO);
                            return ceremonyRepository.persist(ceremony)
                                    .map(ceremonyMapper::ceremonyToCeremonyDTO);
                        })
                        .onFailure().transform(throwable -> {
                                    log.error("Erreur lors de la création de la cérémonie", throwable);
                                    return new WebApplicationException("Erreur lors de la création de la cérémonie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Recherche une cérémonie existante à partir de l'identifiant fourni dans {@link CeremonyDTO},
     * ou crée une nouvelle cérémonie si l'identifiant est {@code null}.
     * <p>
     * Si {@code ceremonyDTO.getId()} est non nul mais qu'aucune cérémonie ne correspond à cet identifiant,
     * une exception {@link IllegalArgumentException} est levée.
     * <p>
     * Si l'identifiant est {@code null}, la méthode persiste une nouvelle entité {@link Ceremony} construite
     * à partir des informations du {@link CeremonyDTO}.
     *
     * @param ceremonyDTO Les données de la cérémonie à rechercher ou créer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la {@link Ceremony} trouvée ou créée.
     * @throws NotFoundException si l'identifiant est fourni mais aucune cérémonie correspondante n’est trouvée.
     */
    public Uni<Ceremony> findOrCreateCeremony(CeremonyDTO ceremonyDTO) {
        return
                Objects.nonNull(ceremonyDTO.getId())
                        ?
                        ceremonyRepository.findById(ceremonyDTO.getId())
                                .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                        :
                        ceremonyRepository.persist(ceremonyMapper.ceremonyDTOtoCeremony(ceremonyDTO))
                ;
    }

    /**
     * Met à jour une cérémonie existante avec les informations fournies dans un {@link CeremonyDTO}.
     * <p>
     * Si aucune cérémonie ne correspond à l’identifiant {@code id}, une exception {@link NotFoundException} est levée.
     * La méthode applique les modifications à l’entité existante et retourne le {@link CeremonyDTO} mis à jour.
     * <p>
     * En cas d’erreur lors de l’exécution de la transaction, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id          L’identifiant de la cérémonie à mettre à jour. Ne peut pas être {@code null}.
     * @param ceremonyDTO Les données à appliquer pour la mise à jour de la cérémonie.
     * @return Un {@link Uni} émettant le {@link CeremonyDTO} mis à jour.
     * @throws NotFoundException       si aucune cérémonie ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la mise à jour de la cérémonie.
     */
    public Uni<CeremonyDTO> update(@NotNull Long id, CeremonyDTO ceremonyDTO) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                                        .invoke(ceremony -> ceremony.updateCeremony(ceremonyDTO))
                                        .call(ceremony -> ceremonyRepository.flush())
                                        .map(ceremonyMapper::ceremonyToCeremonyDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la mise à jour de la cérémonie", throwable);
                                    return new WebApplicationException("Erreur lors de la mise à jour de la cérémonie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime une cérémonie existante à partir de son identifiant.
     * <p>
     * Si aucune cérémonie ne correspond à l’identifiant {@code id}, une exception {@link NotFoundException} est levée.
     * La suppression est effectuée dans une transaction et retourne un indicateur de succès.
     * <p>
     * En cas d’erreur lors de la suppression, une exception {@link WebApplicationException} est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant de la cérémonie à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} émettant {@code true} si la suppression a réussi.
     * @throws NotFoundException       si aucune cérémonie ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la suppression de la cérémonie.
     */
    public Uni<Boolean> deleteCeremony(@NotNull Long id) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression de la cérémonie", throwable);
                                    return new WebApplicationException("Erreur lors de la suppression de la cérémonie", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

}
