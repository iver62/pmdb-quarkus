/*
package org.desha.app.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.repository.PersonRepository;

@ApplicationScoped
public class PersonRepositoryProducer {

    @Produces
    @PersonType(Role.PRODUCER)
    public PersonRepository<Producer> createProducerRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.DIRECTOR)
    public PersonRepository<Director> createDirectorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.SCREENWRITER)
    public PersonRepository<Screenwriter> createScreenwriterRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.MUSICIAN)
    public PersonRepository<Musician> createMusicianRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.PHOTOGRAPHER)
    public PersonRepository<Photographer> createPhotographerRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.COSTUMIER)
    public PersonRepository<Costumier> createCostumierRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.DECORATOR)
    public PersonRepository<Decorator> createDecoratorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.EDITOR)
    public PersonRepository<Editor> createEditorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.CASTER)
    public PersonRepository<Caster> createCasterRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.ART_DIRECTOR)
    public PersonRepository<ArtDirector> createArtDirectorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.SOUND_EDITOR)
    public PersonRepository<SoundEditor> createSoundEditorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR)
    public PersonRepository<VisualEffectsSupervisor> createVisualEffectsSupervisorRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.MAKEUP_ARTIST)
    public PersonRepository<MakeupArtist> createMakeupArtistRepository() {
        return new PersonRepository<>();
    }

    @Produces
    @PersonType(Role.HAIRDRESSER)
    public PersonRepository<HairDresser> createHairDresserRepository() {
        return new PersonRepository<>();
    }
}
*/
