package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {

    private Long id;
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public static CategoryDTO build(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdate) {
        return
                CategoryDTO.builder()
                        .id(id)
                        .name(name)
                        .creationDate(creationDate)
                        .lastUpdate(lastUpdate)
                        .build()
                ;
    }

}
