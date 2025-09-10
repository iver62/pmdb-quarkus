package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations simplifiées sur une personne")
public class LitePersonDTO {

    @Schema(description = "Identifiant unique de la personne", examples = "101")
    private Long id;

    @NotBlank(message = "Le nom de la personne ne peut pas être vide")
    @Schema(description = "Nom complet de la personne", type = SchemaType.STRING, required = true, examples = "Jean Dupont")
    private String name;

    @Schema(description = "Nom du fichier de la photo de la personne", type = SchemaType.STRING, examples = "jean_dupont.jpg")
    private String photoFileName;

    @Schema(description = "Date de naissance de la personne", type = SchemaType.STRING, format = "date", examples = "1980-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Date de décès de la personne, si applicable", type = SchemaType.STRING, format = "date", examples = "2020-10-20")
    private LocalDate dateOfDeath;

    public static LitePersonDTO build(Long id, String name, String photoFileName, LocalDate dateOfBirth, LocalDate dateOfDeath) {
        return
                LitePersonDTO.builder()
                        .id(id)
                        .name(name)
                        .dateOfBirth(dateOfBirth)
                        .dateOfDeath(dateOfDeath)
                        .photoFileName(photoFileName)
                        .build();
    }

}
