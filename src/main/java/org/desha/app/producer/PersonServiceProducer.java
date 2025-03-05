package org.desha.app.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.desha.app.service.CountryService;
import org.desha.app.service.FileService;
import org.desha.app.service.PersonService;

@Slf4j
@ApplicationScoped
public class PersonServiceProducer {

    private final CountryService countryService;
    private final FileService fileService;
    private final MovieRepository movieRepository;

    @Inject
    public PersonServiceProducer(
            CountryService countryService,
            FileService fileService,
            MovieRepository movieRepository
    ) {
        this.countryService = countryService;
        this.fileService = fileService;
        this.movieRepository = movieRepository;
    }

    @Produces
    @PersonType(Role.ACTOR)
    public PersonService<Actor> createActorService(@PersonType(Role.ACTOR) PersonRepository<Actor> actorRepository) {
        return new PersonService<>(countryService, movieRepository, actorRepository, fileService);
    }

    @Produces
    @PersonType(Role.PRODUCER)
    public PersonService<Producer> createProducerService(@PersonType(Role.PRODUCER) PersonRepository<Producer> producerRepository) {
        return new PersonService<>(countryService, movieRepository, producerRepository, fileService);
    }

    @Produces
    @PersonType(Role.DIRECTOR)
    public PersonService<Director> createDirectorService(@PersonType(Role.DIRECTOR) PersonRepository<Director> directorRepository) {
        return new PersonService<>(countryService, movieRepository, directorRepository, fileService);
    }

    @Produces
    @PersonType(Role.SCREENWRITER)
    public PersonService<Screenwriter> createScreenwriterService(@PersonType(Role.SCREENWRITER) PersonRepository<Screenwriter> screenwriterRepository) {
        return new PersonService<>(countryService, movieRepository, screenwriterRepository, fileService);
    }

    @Produces
    @PersonType(Role.MUSICIAN)
    public PersonService<Musician> createMusicianService(@PersonType(Role.MUSICIAN) PersonRepository<Musician> musicianRepository) {
        return new PersonService<>(countryService, movieRepository, musicianRepository, fileService);
    }

    @Produces
    @PersonType(Role.PHOTOGRAPHER)
    public PersonService<Photographer> createPhotographerService(@PersonType(Role.PHOTOGRAPHER) PersonRepository<Photographer> photographerRepository) {
        return new PersonService<>(countryService, movieRepository, photographerRepository, fileService);
    }

    @Produces
    @PersonType(Role.COSTUMIER)
    public PersonService<Costumier> createCostumierService(@PersonType(Role.COSTUMIER) PersonRepository<Costumier> costumierRepository) {
        return new PersonService<>(countryService, movieRepository, costumierRepository, fileService);
    }

    @Produces
    @PersonType(Role.DECORATOR)
    public PersonService<Decorator> createDecoratorService(@PersonType(Role.DECORATOR) PersonRepository<Decorator> decoratorRepository) {
        return new PersonService<>(countryService, movieRepository, decoratorRepository, fileService);
    }

    @Produces
    @PersonType(Role.EDITOR)
    public PersonService<Editor> createEditorService(@PersonType(Role.EDITOR) PersonRepository<Editor> editorRepository) {
        return new PersonService<>(countryService, movieRepository, editorRepository, fileService);
    }

    @Produces
    @PersonType(Role.CASTER)
    public PersonService<Caster> createCasterService(@PersonType(Role.CASTER) PersonRepository<Caster> casterRepository) {
        return new PersonService<>(countryService, movieRepository, casterRepository, fileService);
    }

    @Produces
    @PersonType(Role.ART_DIRECTOR)
    public PersonService<ArtDirector> createArtDirectorService(@PersonType(Role.ART_DIRECTOR) PersonRepository<ArtDirector> artDirectorRepository) {
        return new PersonService<>(countryService, movieRepository, artDirectorRepository, fileService);
    }

    @Produces
    @PersonType(Role.SOUND_EDITOR)
    public PersonService<SoundEditor> createSoundEditorService(@PersonType(Role.SOUND_EDITOR) PersonRepository<SoundEditor> soundEditorRepository) {
        return new PersonService<>(countryService, movieRepository, soundEditorRepository, fileService);
    }

    @Produces
    @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR)
    public PersonService<VisualEffectsSupervisor> createVisualEffectsSupervisorService(@PersonType(Role.VISUAL_EFFECTS_SUPERVISOR) PersonRepository<VisualEffectsSupervisor> visualEffectsSupervisorRepository) {
        return new PersonService<>(countryService, movieRepository, visualEffectsSupervisorRepository, fileService);
    }

    @Produces
    @PersonType(Role.MAKEUP_ARTIST)
    public PersonService<MakeupArtist> createMakeupArtistService(@PersonType(Role.MAKEUP_ARTIST) PersonRepository<MakeupArtist> makeupArtistRepository) {
        return new PersonService<>(countryService, movieRepository, makeupArtistRepository, fileService);
    }

    @Produces
    @PersonType(Role.HAIR_DRESSER)
    public PersonService<HairDresser> createHairDresserService(@PersonType(Role.HAIR_DRESSER) PersonRepository<HairDresser> hairDresserRepository) {
        return new PersonService<>(countryService, movieRepository, hairDresserRepository, fileService);
    }

    @Produces
    @PersonType(Role.STUNT_MAN)
    public PersonService<Stuntman> createStuntmanService(@PersonType(Role.STUNT_MAN) PersonRepository<Stuntman> stuntmanRepository) {
        return new PersonService<>(countryService, movieRepository, stuntmanRepository, fileService);
    }
}