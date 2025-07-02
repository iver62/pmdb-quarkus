package org.desha.app.domain.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.dto.CeremonyAwardsDTO;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

@Entity
@Table(name = "ceremonie_recompenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CeremonyAwards extends PanacheEntityBase {

    public static final String AWARD_LIST_NOT_INITIALIZED = "La liste des récompenses n'est pas initialisée";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_ceremonie", nullable = false)
    private Ceremony ceremony;

    @ManyToOne
    @JoinColumn(name = "fk_film", nullable = false)
    private Movie movie;

    @OneToMany(mappedBy = "ceremonyAwards", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Award> awards = new ArrayList<>();

    public static CeremonyAwards build(Ceremony ceremony, Movie movie, List<Award> awards) {
        return
                CeremonyAwards.builder()
                        .ceremony(ceremony)
                        .movie(movie)
                        .awards(awards)
                        .build()
                ;
    }

    public Uni<CeremonyAwards> updateExistingCeremonyAwards(CeremonyAwardsDTO ceremonyAwardsDTO, Map<Long, Person> personMap) {
        List<AwardDTO> dtoAwards = Objects.nonNull(ceremonyAwardsDTO.getAwards()) ? ceremonyAwardsDTO.getAwards() : List.of();
        // Mettre à jour la cérémonie si besoin
        setCeremony(Ceremony.of(ceremonyAwardsDTO.getCeremony()));

        return
                Mutiny.fetch(awards)
                        .onItem().ifNull().failWith(() -> new IllegalStateException(CeremonyAwards.AWARD_LIST_NOT_INITIALIZED))
                        .invoke(awardList -> {
                            updateAwards(dtoAwards, personMap); // Mettre à jour les récompenses existantes
                            removeAwards(dtoAwards); // Enlever les récompenses obsolètes
                            addAwards(dtoAwards, personMap); // Ajouter les nouvelles récompenses
                        })
                        .replaceWith(this);
    }

    public void addAwards(List<AwardDTO> awardDTOList, Map<Long, Person> personMap) {
        awardDTOList.stream()
                .filter(awardDTO -> Objects.isNull(awardDTO.getId()))
                .forEach(awardDTO -> {
                    Award newAward = Award.createAward(awardDTO, this, personMap);
                    awards.add(newAward);
                });
    }

    public void updateAwards(List<AwardDTO> awardDTOList, Map<Long, Person> personMap) {
        awards.forEach(award ->
                awardDTOList.stream()
                        .filter(awardDTO -> Objects.nonNull(awardDTO.getId()) && awardDTO.getId().equals(award.getId()))
                        .findFirst()
                        .ifPresent(awardDTO -> award.updateAward(awardDTO, personMap))
        );
    }

    /**
     * Supprime une récompense par son identifiant de l'ensemble des récompenses.
     * <p>
     * Cette méthode permet de supprimer une récompense de l'ensemble des récompenses en fonction de son identifiant.
     * Si l'ensemble des récompenses n'est pas initialisé, une exception est levée. La suppression de la récompense se
     * fait en recherchant la récompense dont l'identifiant correspond à celui fourni.
     *
     * @param id L'identifiant de la récompense à supprimer.
     * @return Un {@link Uni} contenant l'ensemble des récompenses après suppression de celle correspondant à l'identifiant.
     * @throws IllegalStateException Si l'ensemble des récompenses n'est pas initialisé.
     */
    public void removeAward(Long id) {
        awards.removeIf(award -> Objects.equals(award.getId(), id));
    }

    public void removeAwards(List<AwardDTO> awardDTOList) {
        awards.removeIf(existing ->
                awardDTOList.stream()
                        .noneMatch(dtoA -> Objects.equals(dtoA.getId(), existing.getId()))
        );
    }

    /**
     * Vide l'ensemble des récompenses associées à un objet.
     * <p>
     * Cette méthode permet de vider la collection des récompenses associées à l'objet en utilisant la méthode
     * {@link Set#clear()}. Elle vérifie également si l'ensemble des récompenses est correctement initialisée.
     * Si la collection des récompenses est nulle, une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble vide de récompenses après avoir vidé la collection.
     * @throws IllegalStateException Si l'ensemble des récompenses n'est pas initialisée (null).
     */
    public void clearAwards() {
        awards.clear();
    }

    /**
     * Récupère et convertit les récompenses associées à un film en objets {@link AwardDTO}.
     * <p>
     * Cette méthode utilise Mutiny pour récupérer les récompenses liées à un film
     * et les transformer en un ensemble de DTOs. Si la liste des récompenses est `null`,
     * une exception est levée.
     *
     * @return Un {@link Uni} contenant un ensemble de {@link AwardDTO}.
     * @throws IllegalStateException si la liste des récompenses n'est pas initialisée.
     */
    public Uni<List<AwardDTO>> fetchAndMapAwardSet() {
        return
                Mutiny.fetch(awards)
                        .onItem().ifNull().failWith(() -> new IllegalStateException(AWARD_LIST_NOT_INITIALIZED))
                        .map(AwardDTO::fromEntityListWithPersons)
                ;
    }
}
