package org.desha.app.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.desha.app.domain.entity.MakeupArtist;

@ApplicationScoped
public class MakeupArtistRepository extends PersonRepository<MakeupArtist> {
}
