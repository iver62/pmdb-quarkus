package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.CeremonyDTO;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ceremonie")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ceremony extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Le nom de la cérémonie ne peut pas être vide")
    @Column(name = "nom", nullable = false)
    private String name;

    public static Ceremony build(Long id, String name) {
        return
                Ceremony.builder()
                        .id(id)
                        .name(StringUtils.capitalize(StringUtils.defaultString(name).trim()))
                        .build()
                ;
    }

    /**
     * Met à jour les informations de la cérémonie à partir d'un {@link CeremonyDTO}.
     * <p>
     * Actuellement, cette méthode met à jour uniquement le nom de la cérémonie,
     * en supprimant les espaces superflus et en capitalisant la première lettre.
     *
     * @param ceremonyDTO Les données de la cérémonie à appliquer. Ne peut pas être {@code null}.
     */
    public void updateCeremony(@NotNull CeremonyDTO ceremonyDTO) {
        setName(StringUtils.capitalize(ceremonyDTO.getName().trim()));
    }

}
