package org.desha.app.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.repository.*;
import org.desha.app.service.CountryService;
import org.desha.app.service.PersonService;

@Slf4j
@ApplicationScoped
public class PersonServiceProducer {

    private final CountryService countryService;
    private final ProducerRepository producerRepository;
    private final DirectorRepository directorRepository;
    private final ScreenwriterRepository screenwriterRepository;
    private final MusicianRepository musicianRepository;
    private final PhotographerRepository photographerRepository;
    private final CostumierRepository costumierRepository;
    private final DecoratorRepository decoratorRepository;
    private final EditorRepository editorRepository;
    private final CasterRepository casterRepository;
    private final ArtDirectorRepository artDirectorRepository;
    private final SoundEditorRepository soundEditorRepository;
    private final VisualEffectsSupervisorRepository visualEffectsSupervisorRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final HairDresserRepository hairDresserRepository;

    @Inject
    public PersonServiceProducer(
            CountryService countryService,
            ProducerRepository producerRepository,
            DirectorRepository directorRepository,
            ScreenwriterRepository screenwriterRepository,
            MusicianRepository musicianRepository,
            PhotographerRepository photographerRepository,
            CostumierRepository costumierRepository,
            DecoratorRepository decoratorRepository,
            EditorRepository editorRepository,
            CasterRepository casterRepository,
            ArtDirectorRepository artDirectorRepository,
            SoundEditorRepository soundEditorRepository,
            VisualEffectsSupervisorRepository visualEffectsSupervisorRepository,
            MakeupArtistRepository makeupArtistRepository,
            HairDresserRepository hairDresserRepository
    ) {
        this.countryService = countryService;
        this.producerRepository = producerRepository;
        this.directorRepository = directorRepository;
        this.screenwriterRepository = screenwriterRepository;
        this.musicianRepository = musicianRepository;
        this.photographerRepository = photographerRepository;
        this.costumierRepository = costumierRepository;
        this.decoratorRepository = decoratorRepository;
        this.editorRepository = editorRepository;
        this.casterRepository = casterRepository;
        this.artDirectorRepository = artDirectorRepository;
        this.soundEditorRepository = soundEditorRepository;
        this.visualEffectsSupervisorRepository = visualEffectsSupervisorRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.hairDresserRepository = hairDresserRepository;
    }

    @Produces
    @PersonType(Role.PRODUCER)
    public PersonService<Producer> createProducerService() {
        return new PersonService<>(countryService, producerRepository);
    }

    @Produces
    @PersonType(Role.DIRECTOR)
    public PersonService<Director> createDirectorService() {
        return new PersonService<>(countryService, directorRepository);
    }

    @Produces
    @PersonType(Role.SCREENWRITER)
    public PersonService<Screenwriter> createScreenwriterService() {
        return new PersonService<>(countryService, screenwriterRepository);
    }

    @Produces
    @PersonType(Role.MUSICIAN)
    public PersonService<Musician> createMusicianService() {
        return new PersonService<>(countryService, musicianRepository);
    }

    @Produces
    @PersonType(Role.PHOTOGRAPHER)
    public PersonService<Photographer> createPhotographerService() {
        return new PersonService<>(countryService, photographerRepository);
    }

    @Produces
    @PersonType(Role.COSTUMIER)
    public PersonService<Costumier> createCostumierService() {
        return new PersonService<>(countryService, costumierRepository);
    }

    @Produces
    @PersonType(Role.DECORATOR)
    public PersonService<Decorator> createDecoratorService() {
        return new PersonService<>(countryService, decoratorRepository);
    }

    @Produces
    @PersonType(Role.EDITOR)
    public PersonService<Editor> createEditorService() {
        return new PersonService<>(countryService, editorRepository);
    }

    @Produces
    @PersonType(Role.CASTER)
    public PersonService<Caster> createCasterService() {
        return new PersonService<>(countryService, casterRepository);
    }

    @Produces
    @PersonType(Role.ART_DIRECTOR)
    public PersonService<ArtDirector> createArtDirectorService() {
        return new PersonService<>(countryService, artDirectorRepository);
    }

    @Produces
    @PersonType(Role.SOUND_EDITOR)
    public PersonService<SoundEditor> createSoundEditorService() {
        return new PersonService<>(countryService, soundEditorRepository);
    }

    @Produces
    @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR)
    public PersonService<VisualEffectsSupervisor> createVisualEffectsSupervisorService() {
        return new PersonService<>(countryService, visualEffectsSupervisorRepository);
    }

    @Produces
    @PersonType(Role.MAKEUP_ARTIST)
    public PersonService<MakeupArtist> createMakeupArtistService() {
        return new PersonService<>(countryService, makeupArtistRepository);
    }

    @Produces
    @PersonType(Role.HAIR_DRESSER)
    public PersonService<HairDresser> createHairDresserService() {
        return new PersonService<>(countryService, hairDresserRepository);
    }

}
