package org.desha.app.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.Producer;
import org.desha.app.qualifier.PersonType;
import org.desha.app.service.PersonService;

@Path("producers")
@ApplicationScoped
@Slf4j
public class ProducerResource extends PersonResource<Producer> {

    @Inject
    public ProducerResource(@PersonType(Role.PRODUCER) PersonService<Producer> producerService) {
        super(producerService);
    }

    @Override
    protected Producer createEntityInstance() {
        return Producer.builder().build();
    }

}
