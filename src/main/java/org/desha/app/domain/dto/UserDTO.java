package org.desha.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informations sur un utilisateur")
public class UserDTO {

    @Schema(description = "Identifiant unique de l'utilisateur", type = SchemaType.STRING, examples = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nom d'utilisateur unique", type = SchemaType.STRING, uniqueItems = true, examples = "john_doe")
    private String username;

    @Schema(description = "Adresse email de l'utilisateur", type = SchemaType.STRING, examples = "john.doe@example.com")
    private String email;

    @Schema(description = "Nom de famille de l'utilisateur", type = SchemaType.STRING, examples = "Doe")
    private String lastname;

    @Schema(description = "Prénom de l'utilisateur", type = SchemaType.STRING, examples = "John")
    private String firstname;

    @Schema(description = "Indique si l'email a été vérifié", type = SchemaType.BOOLEAN, examples = "true")
    private Boolean emailVerified;

    @Schema(description = "Nombre de films ajoutés par l'utilisateur", type = SchemaType.INTEGER, examples = "12")
    private Integer numberOfMovies;

    public static UserDTO build(UUID id, String username, String email, Boolean emailVerified, String lastname, String firstname, Integer numberOfMovies) {
        return
                UserDTO.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .emailVerified(emailVerified)
                        .lastname(lastname)
                        .firstname(firstname)
                        .numberOfMovies(numberOfMovies)
                        .build()
                ;
    }
}