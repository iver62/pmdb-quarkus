package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.CeremonyDTO;
import org.desha.app.domain.entity.Ceremony;
import org.desha.app.mapper.CeremonyMapper;
import org.desha.app.repository.CeremonyRepository;
import org.desha.app.utils.Messages;

import java.util.Objects;
import java.util.Set;

@ApplicationScoped
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
                ;
    }

    public Uni<Ceremony> findOrCreateCeremony(CeremonyDTO ceremonyDTO) {
        return
                Objects.nonNull(ceremonyDTO.getId())
                        ? ceremonyRepository.findById(ceremonyDTO.getId())
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException(Messages.CEREMONY_NOT_FOUND))
                        : ceremonyRepository.persist(ceremonyMapper.ceremonyDTOtoCeremony(ceremonyDTO))
                ;
    }

}
