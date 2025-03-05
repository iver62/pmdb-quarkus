package org.desha.app.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import org.desha.app.domain.Role;
import org.desha.app.domain.entity.*;
import org.desha.app.qualifier.PersonType;
import org.desha.app.repository.*;

@ApplicationScoped
public class PersonRepositoryProducer {

    private final ActorRepository actorRepository;
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
    private final StuntmanRepository stuntmanRepository;

    @Inject
    public PersonRepositoryProducer(
            ActorRepository actorRepository,
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
            HairDresserRepository hairDresserRepository,
            StuntmanRepository stuntmanRepository
    ) {
        this.actorRepository = actorRepository;
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
        this.stuntmanRepository = stuntmanRepository;
    }

    @Produces
    @PersonType(Role.ACTOR)
    public PersonRepository<Actor> createActorRepository() {
        return actorRepository;
    }

    @Produces
    @PersonType(Role.PRODUCER)
    public PersonRepository<Producer> createProducerRepository() {
        return producerRepository;
    }

    @Produces
    @PersonType(Role.DIRECTOR)
    public PersonRepository<Director> createDirectorRepository() {
        return directorRepository;
    }

    @Produces
    @PersonType(Role.SCREENWRITER)
    public PersonRepository<Screenwriter> createScreenwriterRepository() {
        return screenwriterRepository;
    }

    @Produces
    @PersonType(Role.MUSICIAN)
    public PersonRepository<Musician> createMusicianRepository() {
        return musicianRepository;
    }

    @Produces
    @PersonType(Role.PHOTOGRAPHER)
    public PersonRepository<Photographer> createPhotographerRepository() {
        return photographerRepository;
    }

    @Produces
    @PersonType(Role.COSTUMIER)
    public PersonRepository<Costumier> createCostumierRepository() {
        return costumierRepository;
    }

    @Produces
    @PersonType(Role.DECORATOR)
    public PersonRepository<Decorator> createDecoratorRepository() {
        return decoratorRepository;
    }

    @Produces
    @PersonType(Role.EDITOR)
    public PersonRepository<Editor> createEditorRepository() {
        return editorRepository;
    }

    @Produces
    @PersonType(Role.CASTER)
    public PersonRepository<Caster> createCasterRepository() {
        return casterRepository;
    }

    @Produces
    @PersonType(Role.ART_DIRECTOR)
    public PersonRepository<ArtDirector> createArtDirectorRepository() {
        return artDirectorRepository;
    }

    @Produces
    @PersonType(Role.SOUND_EDITOR)
    public PersonRepository<SoundEditor> createSoundEditorRepository() {
        return soundEditorRepository;
    }

    @Produces
    @PersonType(Role.VISUAL_EFFECTS_SUPERVISOR)
    public PersonRepository<VisualEffectsSupervisor> createVisualEffectsSupervisorRepository() {
        return visualEffectsSupervisorRepository;
    }

    @Produces
    @PersonType(Role.MAKEUP_ARTIST)
    public PersonRepository<MakeupArtist> createMakeupArtistRepository() {
        return makeupArtistRepository;
    }

    @Produces
    @PersonType(Role.HAIR_DRESSER)
    public PersonRepository<HairDresser> createHairDresserRepository() {
        return hairDresserRepository;
    }

    @Produces
    @PersonType(Role.STUNT_MAN)
    public PersonRepository<Stuntman> createStuntmanRepository() {
        return stuntmanRepository;
    }
}
