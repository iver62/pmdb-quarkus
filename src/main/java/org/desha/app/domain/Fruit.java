package org.desha.app.domain;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Cacheable
@AllArgsConstructor
@NoArgsConstructor
public class Fruit extends PanacheEntity {

    @Column(length = 40, unique = true)
    public String name;

}