package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @WithSession
    public Uni<CeremonyDTO> getCeremony(Long id) {
        return
                ceremonyRepository.findById(id)
                        .onItem().ifNull().failWith(new NotFoundException(Messages.NOT_FOUND_CEREMONY))
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
     * Récupère la liste des cérémonies correspondant à un terme de recherche donné,
     * avec pagination et tri.
     *
     * @param page      la pagination à appliquer (index de page, taille de page)
     * @param direction la direction du tri (ASC ou DESC)
     * @param term      le terme à rechercher (filtre sur le nom de la cérémonie)
     * @return un {@link Uni} contenant la liste des noms de cérémonies correspondantes
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

    public Uni<Ceremony> findOrCreateCeremony(CeremonyDTO ceremonyDTO) {
        return
                Objects.nonNull(ceremonyDTO.getId())
                        ? ceremonyRepository.findById(ceremonyDTO.getId())
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.NOT_FOUND_CEREMONY))
                        : ceremonyRepository.persist(ceremonyMapper.ceremonyDTOtoCeremony(ceremonyDTO))
                ;
    }

    public Uni<CeremonyDTO> update(Long id, CeremonyDTO ceremonyDTO) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_CEREMONY))
                                        .invoke(ceremony -> ceremony.setName(StringUtils.capitalize(ceremonyDTO.getName().trim())))
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

    public Uni<Boolean> deleteCeremony(Long id) {
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
