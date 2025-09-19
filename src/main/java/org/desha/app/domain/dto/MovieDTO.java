package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations détaillées sur un film")
public class MovieDTO {

    @Schema(description = "Identifiant unique du film", type = SchemaType.NUMBER, examples = "1")
    private Long id;

    @NotBlank(message = "Le titre du film est obligatoire")
    @Schema(description = "Titre du film", required = true, type = SchemaType.STRING, examples = "Inception")
    private String title;

    @Schema(description = "Titre original du film", type = SchemaType.STRING, examples = "Inception")
    private String originalTitle;

    @Schema(description = "Synopsis du film", type = SchemaType.STRING, examples = "Un voleur capable d'entrer dans les rêves...")
    private String synopsis;

    @Schema(description = "Date de sortie du film", type = SchemaType.STRING, format = "date", examples = "2010-07-16")
    private LocalDate releaseDate;

    @Schema(description = "Durée du film en minutes", type = SchemaType.INTEGER, examples = "148")
    private Integer runningTime;

    @Schema(description = "Budget du film", type = SchemaType.OBJECT)
    private BudgetDTO budget;

    @Schema(description = "Box-office du film", type = SchemaType.OBJECT)
    private BoxOfficeDTO boxOffice;

    @Schema(description = "Nom du fichier de l'affiche du film", type = SchemaType.STRING, examples = "inception.jpg")
    private String posterFileName;

    @Schema(description = "Nombre de récompenses reçues", type = SchemaType.INTEGER, examples = "4")
    private Integer numberOfAwards;

    @Schema(description = "Utilisateur ayant ajouté le film", type = SchemaType.OBJECT)
    private UserDTO user;

    @Schema(description = "Équipe technique associée au film", type = SchemaType.OBJECT)
    private TechnicalTeamDTO technicalTeam;

    @Schema(description = "Liste des acteurs du film", type = SchemaType.ARRAY)
    private List<MovieActorDTO> movieActors;

    @Schema(description = "Liste des pays associés au film", type = SchemaType.ARRAY)
    private Set<CountryDTO> countries;

    @Schema(description = "Liste des catégories du film", type = SchemaType.ARRAY)
    private Set<CategoryDTO> categories;

    @Schema(description = "Liste des récompenses associées au film", type = SchemaType.ARRAY)
    private Set<CeremonyAwardsDTO> ceremonyAwards;

    @Schema(description = "Date de création de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2024-09-10T15:30:00")
    private LocalDateTime creationDate;

    @Schema(description = "Date de dernière mise à jour de l'enregistrement", type = SchemaType.STRING, format = "date-time", examples = "2024-09-11T10:00:00")
    private LocalDateTime lastUpdate;

    public static MovieDTO build(Long id, String title, String originalTitle, String synopsis, LocalDate releaseDate, Integer runningTime, Long budgetValue, String budgetCurrency, Long boxOfficeValue, String boxOfficeCurrency, String posterFileName, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                MovieDTO.builder()
                        .id(id)
                        .title(title)
                        .originalTitle(originalTitle)
                        .synopsis(synopsis)
                        .releaseDate(releaseDate)
                        .runningTime(runningTime)
                        .budget(BudgetDTO.build(budgetValue, budgetCurrency))
                        .boxOffice(BoxOfficeDTO.build(boxOfficeValue, boxOfficeCurrency))
                        .posterFileName(posterFileName)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
                        .build()
                ;
    }

}
