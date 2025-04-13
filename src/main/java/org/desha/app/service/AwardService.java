package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;
import org.desha.app.domain.entity.Award;
import org.desha.app.repository.AwardRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AwardService {

    private final AwardRepository awardRepository;

    @Inject
    public AwardService(AwardRepository awardRepository) {
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
        return awardRepository.findById(id)
                .onItem().ifNull().failWith(() -> new IllegalArgumentException("Récompense introuvable"))
                .map(AwardDTO::fromEntity);
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
    public Uni<List<String>> getCeremonies(Page page, Sort.Direction direction, String term) {
        return awardRepository.findCeremonies(page, direction, term);
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
    public Uni<Award> updateAward(Long id, AwardDTO awardDTO) {
        return
                Panache
                        .withTransaction(() ->
                                awardRepository.findById(id)
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Récompense introuvable"))
                                        .invoke(entity -> {
                                            entity.setCeremony(StringUtils.capitalize(awardDTO.getCeremony().trim()));
                                            entity.setName(StringUtils.capitalize(awardDTO.getName().trim()));
                                            entity.setYear(awardDTO.getYear());
                                        })
                                        .flatMap(award -> awardRepository.findById(award.getId()))
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
                                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Récompense introuvable"))
                        )
                ;
    }

    public Set<AwardDTO> fromAwardSetEntity(Set<Award> awardSet) {
        return
                awardSet
                        .stream()
                        .map(AwardDTO::fromEntity)
                        .collect(Collectors.toSet())
                ;
    }
}
