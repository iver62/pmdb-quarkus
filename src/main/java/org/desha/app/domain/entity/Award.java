package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.dto.AwardDTO;

import java.time.Year;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "recompense", uniqueConstraints = {@UniqueConstraint(columnNames = {"ceremonie", "nom"})})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Award extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "La cérémonie ne peut pas être vide")
    @Column(name = "ceremonie", nullable = false)
    private String ceremony;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "annee")
    private Year year;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "fk_film", nullable = false)
    private Movie movie;

    public static Award fromDTO(AwardDTO awardDTO) {
        return
                Award.builder()
                        .id(awardDTO.getId())
                        .ceremony(StringUtils.capitalize(awardDTO.getCeremony().trim()))
                        .name(StringUtils.capitalize(awardDTO.getName().trim()))
                        .year(awardDTO.getYear())
                        .build();
    }

}
