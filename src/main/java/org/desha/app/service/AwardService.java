package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.LitePersonDTO;
import org.desha.app.domain.entity.Person;
import org.desha.app.mapper.AwardMapper;
import org.desha.app.repository.AwardRepository;
import org.desha.app.utils.Messages;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Slf4j
public class AwardService {

    private final AwardMapper awardMapper;
    private final PersonService personService;
    private final AwardRepository awardRepository;

    @Inject
    public AwardService(
            AwardMapper awardMapper,
            PersonService personService,
            AwardRepository awardRepository
    ) {
        this.awardMapper = awardMapper;
        this.personService = personService;
        this.awardRepository = awardRepository;
    }

    /**
     * Récupère une récompense (Award) par son identifiant et la transforme en DTO.
     *
     * <p>Cette méthode recherche l'entité {@code Award} correspondant à l'identifiant fourni.
     * Si elle est trouvée, elle est transformée en {@link AwardDTO}. Si aucune récompense n'est trouvée,
     * une exception {@link IllegalArgumentException} est levée.</p>
     *
     * @param id l'identifiant de la récompense à récupérer
     * @return un {@link Uni} contenant le {@link AwardDTO} correspondant
     * @throws IllegalArgumentException si aucune récompense n'est trouvée avec l'identifiant donné
     */
    public Uni<AwardDTO> getAward(Long id) {
        return
                awardRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_AWARD))
                        .map(awardMapper::awardToAwardDTO)
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la récupération de la récompense", throwable);
                                    return new WebApplicationException("Erreur lors de la récupération de la récompense", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    public Uni<List<Person>> getPersonsByAwards(List<AwardDTO> awardDTOList) {
        return
                personService.getByIds(
                        awardDTOList.stream()
                                .filter(dto -> Objects.nonNull(dto.getPersons()))
                                .flatMap(dto -> dto.getPersons().stream())
                                .map(LitePersonDTO::getId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList()
                );
    }

    /**
     * Met à jour une récompense (Award) existante avec les données fournies dans le DTO.
     *
     * <p>Cette méthode recherche une récompense avec l'identifiant spécifié. Si une récompense est trouvée,
     * ses attributs sont mis à jour avec les valeurs fournies dans le {@link AwardDTO}. Les champs sont capitalisés
     * avant d'être enregistrés. Si aucune récompense n'est trouvée avec l'identifiant donné, une exception
     * {@link IllegalArgumentException} est levée.</p>
     *
     * @param id       l'identifiant de la récompense à mettre à jour
     * @param awardDTO l'objet contenant les nouvelles données pour la mise à jour de la récompense
     * @return un {@link Uni} contenant la récompense mise à jour après la transaction
     * @throws IllegalArgumentException si aucune récompense n'est trouvée avec l'identifiant donné
     */
    public Uni<AwardDTO> updateAward(Long id, AwardDTO awardDTO) {
        return
                Panache
                        .withTransaction(() ->
                                awardRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_AWARD))
                                        .invoke(entity -> {
                                            entity.setName(StringUtils.capitalize(awardDTO.getName().trim()));
                                            entity.setYear(awardDTO.getYear());
                                        })
                                        .call(category -> awardRepository.flush())
                                        .map(awardMapper::awardToAwardDTO)
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la modification de la récompense", throwable);
                                    return new WebApplicationException("Erreur lors de la modification de la récompense", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

    /**
     * Supprime une récompense (Award) par son identifiant.
     *
     * <p>Cette méthode effectue l'opération de suppression dans une transaction à l'aide de Panache.
     * Si la récompense avec l'identifiant donné n'existe pas, une exception est levée.</p>
     *
     * @param id l'identifiant de la récompense à supprimer
     * @return un {@link Uni} contenant {@code true} si la suppression a réussi
     * @throws IllegalArgumentException si aucune récompense n'est trouvée avec l'identifiant fourni
     */
    public Uni<Boolean> deleteAward(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                awardRepository.deleteById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_AWARD))
                        )
                        .onFailure().transform(throwable -> {
                                    if (throwable instanceof WebApplicationException) {
                                        return throwable;
                                    }
                                    log.error("Erreur lors de la suppression de la récompense", throwable);
                                    return new WebApplicationException("Erreur lors de la suppression de la récompense", Response.Status.INTERNAL_SERVER_ERROR);
                                }
                        )
                ;
    }

}
