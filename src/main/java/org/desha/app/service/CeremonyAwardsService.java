package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.desha.app.domain.dto.LightPersonDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.domain.entity.CeremonyAwards;
import org.desha.app.domain.entity.Movie;
import org.desha.app.domain.entity.Person;
import org.desha.app.repository.CeremonyAwardsRepository;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class CeremonyAwardsService {

    private final AwardService awardService;
    private final CeremonyService ceremonyService;
    private final CeremonyAwardsRepository ceremonyAwardsRepository;

    @Inject
    public CeremonyAwardsService(
            AwardService awardService,
            CeremonyService ceremonyService,
            CeremonyAwardsRepository ceremonyAwardsRepository
    ) {
        this.awardService = awardService;
        this.ceremonyService = ceremonyService;
        this.ceremonyAwardsRepository = ceremonyAwardsRepository;
    }

    public Uni<CeremonyAwards> createNewCeremonyAwards(Movie movie, CeremonyAwardsDTO ceremonyAwardsDTO, Map<Long, Person> personMap) {
        return
                ceremonyService.findOrCreateCeremony(ceremonyAwardsDTO.getCeremony())
                        .map(ceremony -> CeremonyAwards.build(ceremony, movie, new ArrayList<>()))
                        .invoke(ceremonyAwards -> {
                                    if (Objects.nonNull(ceremonyAwardsDTO.getAwards())) {
                                        for (AwardDTO awardDTO : ceremonyAwardsDTO.getAwards()) {
                                            Award award = Award.of(awardDTO);
                                            award.setCeremonyAwards(ceremonyAwards);

                                            if (Objects.nonNull(awardDTO.getPersons())) {
                                                Set<Person> linkedPersons = awardDTO.getPersons().stream()
                                                        .map(LightPersonDTO::getId)
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
                ;
    }

    public Uni<CeremonyAwardsDTO> addAwards(Long id, List<AwardDTO> awardDTOList) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Cérémonie introuvable"))
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
                                        .map(ceremonyAwards -> CeremonyAwardsDTO.of(ceremonyAwards, ceremonyAwards.getAwards()))
                        )
                ;
    }

    /**
     * Supprime une récompense associée à un film.
     * <p>
     * Cette méthode recherche un film par son identifiant et supprime une récompense
     * spécifique de sa liste de récompenses si elle est présente. Si le film n'est pas trouvé,
     * une exception est levée. Après la suppression, les changements sont persistés en base,
     * puis la liste mise à jour des récompenses est récupérée et retournée sous forme de DTOs.
     *
     * @param awardId L'identifiant de la récompense à supprimer.
     * @return Un {@link Uni} contenant l'ensemble mis à jour des {@link AwardDTO} du film.
     * @throws IllegalArgumentException si le film est introuvable.
     */
    public Uni<CeremonyAwardsDTO> removeAward(Long ceremonyAwardsId, Long awardId) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(ceremonyAwardsId)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Cérémonie introuvable"))
                                        .chain(ceremonyAwards ->
                                                Mutiny.fetch(ceremonyAwards.getAwards())
                                                        .invoke(awardList -> ceremonyAwards.removeAward(awardId))
                                                        .replaceWith(ceremonyAwards)
                                        )
                                        .call(ceremonyAwardsRepository::persist)
                                        .map(ceremonyAwards -> CeremonyAwardsDTO.of(ceremonyAwards, ceremonyAwards.getAwards()))
                        )
                ;
    }

    public Uni<Boolean> clearAwards(Long id) {
        return
                Panache
                        .withTransaction(() ->
                                ceremonyAwardsRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Cérémonie introuvable"))
                                        .chain(ceremonyAwards ->
                                                Mutiny.fetch(ceremonyAwards.getAwards())
                                                        .invoke(awardList -> ceremonyAwards.clearAwards())
                                                        .replaceWith(ceremonyAwards)
                                        )
                                        .chain(ceremonyAwardsRepository::persist)
                                        .map(ceremonyAwards -> true)
                        )
                        .onFailure().transform(throwable -> {
                            log.error(throwable.getMessage());
                            throw new WebApplicationException("Erreur lors de la suppression des récompenses", throwable);
                        });
    }

}
