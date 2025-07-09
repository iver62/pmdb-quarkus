package org.desha.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.desha.app.domain.AuditCategoryListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "categorie")
@EntityListeners(AuditCategoryListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category extends PanacheEntityBase {

    public static final String DEFAULT_SORT = "name";
    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "creationDate", "lastUpdate");

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty(message = "Le nom ne peut pas être vide")
    @Column(name = "nom", nullable = false, unique = true)
    private String name;

    @Column(name = "date_creation")
    private LocalDateTime creationDate;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime lastUpdate;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    private Set<Movie> movies = new HashSet<>();

    public static Category build(Long id, String name) {
        return
                Category.builder()
                        .id(id)
                        .name(StringUtils.capitalize(StringUtils.defaultString(name).trim()))
                        .build()
                ;
    }

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return id + "-" + name;
    }

}
