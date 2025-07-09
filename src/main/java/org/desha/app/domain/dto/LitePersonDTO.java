package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LitePersonDTO {

    private Long id;
    private String name;
    private String photoFileName;
    private LocalDate dateOfBirth;
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
