package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
                        .build();
    }

    public static Ceremony of(CeremonyDTO ceremonyDTO) {
        return Ceremony.build(ceremonyDTO.getId(), ceremonyDTO.getName());
    }

}
