package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
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
     * Récupère une récompense ({@link AwardDTO}) à partir de son identifiant.
     * <p>
     * Si aucune récompense ne correspond à l'identifiant fourni, une exception {@link NotFoundException} est levée.
     * <p>
     * En cas d’erreur lors de l’exécution de la requête, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant de la récompense à récupérer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} contenant la {@link AwardDTO} correspondant à l’identifiant fourni.
     * @throws NotFoundException       si aucune récompense n’est trouvée pour l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la récupération de la récompense.
     */
    public Uni<AwardDTO> getAward(@NotNull Long id) {
        return
                awardRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_AWARD))
                        .map(awardMapper::todDTO)
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

    /**
     * Récupère la liste des personnes associées aux récompenses fournies.
     * <p>
     * Pour chaque {@link AwardDTO} de la liste {@code awardDTOList}, cette méthode extrait les personnes associées,
     * filtre les identifiants non nuls, supprime les doublons et retourne la liste correspondante de {@link Person}.
     * <p>
     * Si aucune personne n’est associée aux récompenses fournies, la méthode retourne une liste vide.
     *
     * @param awardDTOList La liste des récompenses à partir desquelles récupérer les personnes. Ne peut pas être {@code null}.
     * @return Un {@link Uni} émettant une {@link List} de {@link Person} associée aux récompenses fournies.
     */
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
     * Met à jour une récompense existante avec les informations fournies dans un {@link AwardDTO}.
     * <p>
     * Si aucune récompense ne correspond à l’identifiant {@code id}, une exception {@link NotFoundException} est levée.
     * La méthode applique les modifications à l’entité existante et retourne le {@link AwardDTO} mis à jour.
     * <p>
     * En cas d’erreur lors de l’exécution de la transaction, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id       L’identifiant de la récompense à mettre à jour. Ne peut pas être {@code null}.
     * @param awardDTO Les données à appliquer pour la mise à jour de la récompense.
     * @return Un {@link Uni} émettant le {@link AwardDTO} mis à jour.
     * @throws NotFoundException       si aucune récompense ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la mise à jour de la récompense.
     */
    public Uni<AwardDTO> updateAward(@NotNull Long id, AwardDTO awardDTO) {
        return
                Panache
                        .withTransaction(() ->
                                awardRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new NotFoundException(Messages.NOT_FOUND_AWARD))
                                        .invoke(award -> award.updateAward(awardDTO))
                                        .call(category -> awardRepository.flush())
                                        .map(awardMapper::todDTO)
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
     * Supprime une récompense existante à partir de son identifiant.
     * <p>
     * Si aucune récompense ne correspond à l’identifiant {@code id}, une exception {@link NotFoundException} est levée.
     * La suppression est effectuée dans une transaction et retourne un indicateur de succès.
     * <p>
     * En cas d’erreur lors de la suppression, une exception {@link WebApplicationException}
     * est levée avec un statut HTTP 500.
     *
     * @param id L’identifiant de la récompense à supprimer. Ne peut pas être {@code null}.
     * @return Un {@link Uni} émettant {@code true} si la suppression a réussi.
     * @throws NotFoundException       si aucune récompense ne correspond à l’identifiant fourni.
     * @throws WebApplicationException si une erreur survient lors de la suppression de la récompense.
     */
    public Uni<Boolean> deleteAward(@NotNull Long id) {
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
