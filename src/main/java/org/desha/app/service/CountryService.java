package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Country;
import org.desha.app.repository.*;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.*;

@ApplicationScoped
public class CountryService {

    private final ActorRepository actorRepository;
    private final ArtDirectorRepository artDirectorRepository;
    private final CasterRepository casterRepository;
    private final CostumierRepository costumierRepository;
    private final DecoratorRepository decoratorRepository;
    private final DirectorRepository directorRepository;
    private final EditorRepository editorRepository;
    private final HairDresserRepository hairDresserRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final MusicianRepository musicianRepository;
    private final PhotographerRepository photographerRepository;
    private final ProducerRepository producerRepository;
    private final ScreenwriterRepository screenwriterRepository;
    private final StuntmanRepository stuntmanRepository;
    private final SoundEditorRepository soundEditorRepository;
    private final VisualEffectsSupervisorRepository visualEffectsSupervisorRepository;
    private final CountryRepository countryRepository;
    private final MovieRepository movieRepository;

    @Inject
    public CountryService(
            ActorRepository actorRepository,
            ArtDirectorRepository artDirectorRepository,
            CasterRepository casterRepository,
            CostumierRepository costumierRepository,
            DecoratorRepository decoratorRepository,
            DirectorRepository directorRepository,
            EditorRepository editorRepository,
            HairDresserRepository hairDresserRepository,
            MakeupArtistRepository makeupArtistRepository,
            MusicianRepository musicianRepository,
            PhotographerRepository photographerRepository,
            ProducerRepository producerRepository,
            ScreenwriterRepository screenwriterRepository,
            SoundEditorRepository soundEditorRepository,
            StuntmanRepository stuntmanRepository,
            VisualEffectsSupervisorRepository visualEffectsSupervisorRepository,
            CountryRepository countryRepository,
            MovieRepository movieRepository
    ) {
        this.actorRepository = actorRepository;
        this.artDirectorRepository = artDirectorRepository;
        this.casterRepository = casterRepository;
        this.costumierRepository = costumierRepository;
        this.decoratorRepository = decoratorRepository;
        this.directorRepository = directorRepository;
        this.editorRepository = editorRepository;
        this.hairDresserRepository = hairDresserRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.musicianRepository = musicianRepository;
        this.photographerRepository = photographerRepository;
        this.producerRepository = producerRepository;
        this.screenwriterRepository = screenwriterRepository;
        this.soundEditorRepository = soundEditorRepository;
        this.stuntmanRepository = stuntmanRepository;
        this.visualEffectsSupervisorRepository = visualEffectsSupervisorRepository;
        this.countryRepository = countryRepository;
        this.movieRepository = movieRepository;
    }

    public Uni<Long> countCountries(String term) {
        return countryRepository.countCountries(term);
    }

    public Uni<Country> getById(Long id) {
        return
                countryRepository.findById(id)
                        .onFailure().recoverWithNull()
                ;
    }

    public Uni<List<CountryDTO>> getCountries(int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                countryRepository
                        .findCountries(pageIndex, size, sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    public Uni<List<CountryDTO>> getCountries(String sort, Sort.Direction direction, String term) {
        return
                countryRepository
                        .findCountries(sort, direction, term)
                        .map(
                                countryList ->
                                        countryList
                                                .stream()
                                                .map(CountryDTO::fromEntity)
                                                .toList()
                        )
                ;
    }

    /**
     * Compte le nombre de films associés à un pays donné, en fonction d'un terme de recherche dans le titre des films.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le titre des films.
     * @return Un objet {@link Uni} contenant le nombre de films correspondant aux critères.
     */
    public Uni<Long> countMovies(Long countryId, String term) {
        return movieRepository.countMoviesByCountry(countryId, term);
    }

    /**
     * Compte le nombre de producteurs associés à un pays donné, en fonction d'un terme de recherche sur leur nom.
     *
     * @param countryId L'identifiant du pays pour lequel rechercher les producteurs.
     * @param term      Le terme de recherche à comparer avec le nom des producteurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de producteurs correspondant aux critères de recherche.
     */
    /*public Uni<Long> countProducersByCountry(Long countryId, String term) {
        return producerRepository.countProducersByCountry(countryId, term);
    }*/

    /**
     * Compte le nombre d'acteurs associés à un pays donné, en fonction d'un terme de recherche dans le nom de l'acteur.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le nom des acteurs.
     * @return Un objet {@link Uni} contenant le nombre d'acteurs correspondant aux critères.
     */
    /*public Uni<Long> countActorsByCountry(Long countryId, String term) {
        return actorRepository.countActorsByCountry(countryId, term);
    }*/

    /**
     * Compte le nombre de réalisateurs associés à un pays donné, en fonction d'un terme de recherche dans le nom du réalisateur.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le nom des réalisateurs.
     * @return Un objet {@link Uni} contenant le nombre de réalisateurs correspondant aux critères.
     */
   /* public Uni<Long> countDirectorsByCountry(Long countryId, String term) {
        return directorRepository.countDirectorsByCountry(countryId, term);
    }*/

    /**
     * Compte le nombre de scénaristes associés à un pays donné, en fonction d'un terme de recherche dans le nom du scénariste.
     *
     * @param countryId L'identifiant du pays.
     * @param term      Le terme de recherche dans le nom des scénaristes.
     * @return Un objet {@link Uni} contenant le nombre de scénaristes correspondant aux critères.
     */
    /*public Uni<Long> countScreenwritersByCountry(Long countryId, String term) {
        return screenwriterRepository.countScreenwritersByCountry(countryId, term);
    }*/

    /**
     * Compte le nombre de musiciens associés à un pays donné en fonction d'un critère de recherche sur le nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les musiciens.
     * @param term Le terme de recherche à comparer avec le nom des musiciens (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de musiciens correspondant aux critères.
     */
    /*public Uni<Long> countMusiciansByCountry(Long id, String term) {
        return musicianRepository.countMusiciansByCountry(id, term);
    }*/

    /**
     * Compte le nombre de décorateurs associés à un pays donné en fonction d'un critère de recherche sur le nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les décorateurs.
     * @param term Le terme de recherche à comparer avec le nom des décorateurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de décorateurs correspondant aux critères.
     */
    /*public Uni<Long> countDecoratorsByCountry(Long id, String term) {
        return decoratorRepository.countDecoratorsByCountry(id, term);
    }*/

    /**
     * Compte le nombre de costumiers associés à un pays donné, en fonction d'un terme de recherche sur le nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les costumiers.
     * @param term Le terme de recherche à comparer avec le nom des costumiers (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de costumiers correspondant aux critères.
     */
   /* public Uni<Long> countCostumiersByCountry(Long id, String term) {
        return costumierRepository.countCostumiersByCountry(id, term);
    }*/

    /**
     * Compte le nombre de photographes associés à un pays donné, en fonction d'un terme de recherche sur leur nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les photographes.
     * @param term Le terme de recherche à comparer avec le nom des photographes (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de photographes correspondant aux critères.
     */
    /*public Uni<Long> countPhotographersByCountry(Long id, String term) {
        return photographerRepository.countPhotographersByCountry(id, term);
    }*/

    /**
     * Compte le nombre de monteurs associés à un pays donné, en fonction d'un terme de recherche sur leur nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les monteurs.
     * @param term Le terme de recherche à comparer avec le nom des monteurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de monteurs correspondant aux critères de recherche.
     */
   /* public Uni<Long> countEditorsByCountry(Long id, String term) {
        return editorRepository.countEditorsByCountry(id, term);
    }*/

    /**
     * Compte le nombre de castings associés à un pays donné, en fonction d'un terme de recherche sur leur nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les castings.
     * @param term Le terme de recherche à comparer avec le nom des castings (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de castings correspondant aux critères de recherche.
     */
//    public Uni<Long> countCastersByCountry(Long id, String term) {
//        return casterRepository.countCastersByMovie(id, term);
//    }

    /**
     * Compte le nombre de directeurs artistiques associés à un pays donné, en fonction d'un terme de recherche sur leur nom.
     *
     * @param id   L'identifiant du pays pour lequel rechercher les directeurs artistiques.
     * @param term Le terme de recherche à comparer avec le nom des directeurs artistiques (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de directeurs artistiques correspondant aux critères de recherche.
     */
    /*public Uni<Long> countArtDirectorsByCountry(Long id, String term) {
        return artDirectorRepository.countArtDirectorsByMovie(id, term);
    }*/

    /**
     * Compte le nombre d'ingénieurs son associés à un pays donné, en fonction d'un terme de recherche pour le nom.
     * Le nom des ingénieurs son est comparé avec le terme de recherche de manière insensible à la casse.
     *
     * @param id   L'identifiant du pays pour lequel compter les ingénieurs son.
     * @param term Le terme de recherche à comparer avec les noms des ingénieurs son (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre d'ingénieurs son correspondant aux critères.
     */
    /*public Uni<Long> countSoundEditorsByCountry(Long id, String term) {
        return soundEditorRepository.countSoundEditorsByCountry(id, term);
    }*/

    /**
     * Compte le nombre de superviseurs des effets spéciaux associés à un pays donné, en fonction d'un terme de recherche pour le nom.
     * Le nom des superviseurs des effets spéciaux est comparé avec le terme de recherche de manière insensible à la casse.
     *
     * @param id   L'identifiant du pays pour lequel compter les superviseurs des effets spéciaux.
     * @param term Le terme de recherche à comparer avec les noms des superviseurs des effets spéciaux (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de superviseurs des effets spéciaux correspondant aux critères.
     */
    public Uni<Long> countVisualEffectsSupervisorsByCountry(Long id, String term) {
        return visualEffectsSupervisorRepository.countVisualEffectsSupervisorsByCountry(id, term);
    }

    /**
     * Compte le nombre de maquilleurs associés à un pays spécifique, en filtrant les résultats avec un terme
     * de recherche insensible à la casse sur les noms des maquilleurs.
     *
     * @param id   L'identifiant du pays pour lequel compter les maquilleurs associés.
     * @param term Le terme de recherche à appliquer sur les noms des maquilleurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre de maquilleurs correspondant à la recherche.
     */
    public Uni<Long> countMakeupArtistsByCountry(Long id, String term) {
        return makeupArtistRepository.countMakeupArtistsByMovie(id, term);
    }

    /**
     * Compte le nombre de coiffeurs associés à un pays spécifique en fonction d'un terme de recherche appliqué à leur nom.
     *
     * @param id   L'identifiant du pays pour lequel compter les coiffeurs associés.
     * @param term Le terme de recherche appliqué au nom des coiffeurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre total de coiffeurs correspondant aux critères.
     */
    public Uni<Long> countHairDressersByCountry(Long id, String term) {
        return hairDresserRepository.countHairDressersByCountry(id, term);
    }

    /**
     * Compte le nombre de cascadeurs associés à un pays spécifique correspondant à un terme de recherche donné.
     *
     * @param id   L'identifiant du pays pour lequel compter les cascadeurs.
     * @param term Le terme de recherche appliqué au nom des cascadeurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant le nombre total de cascadeurs correspondant aux critères.
     */
    public Uni<Long> countStuntmenByCountry(Long id, String term) {
        return stuntmanRepository.countStuntmenByCountry(id, term);
    }

    public Uni<Set<Country>> getByIds(Set<CountryDTO> countries) {
        return
                countryRepository.findByIds(
                        Optional.ofNullable(countries).orElse(Collections.emptySet())
                                .stream()
                                .map(CountryDTO::getId)
                                .toList()
                ).map(HashSet::new);
    }

    public Uni<CountryDTO> getFull(Long id) {
        return
                countryRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Pays non trouvé"))
                        .call(country -> Mutiny.fetch(country.getMovies()))
                        .call(country -> Mutiny.fetch(country.getActors()))
                        .call(country -> Mutiny.fetch(country.getProducers()))
                        .call(country -> Mutiny.fetch(country.getDirectors()))
                        .call(country -> Mutiny.fetch(country.getScreenwriters()))
                        .call(country -> Mutiny.fetch(country.getMusicians()))
                        .call(country -> Mutiny.fetch(country.getPhotographers()))
                        .call(country -> Mutiny.fetch(country.getCostumiers()))
                        .call(country -> Mutiny.fetch(country.getDecorators()))
                        .call(country -> Mutiny.fetch(country.getEditors()))
                        .call(country -> Mutiny.fetch(country.getCasters()))
                        .call(country -> Mutiny.fetch(country.getArtDirectors()))
                        .call(country -> Mutiny.fetch(country.getSoundEditors()))
                        .call(country -> Mutiny.fetch(country.getVisualEffectsSupervisors()))
                        .call(country -> Mutiny.fetch(country.getMakeupArtists()))
                        .call(country -> Mutiny.fetch(country.getHairDressers()))
                        .call(country -> Mutiny.fetch(country.getStuntmen()))
                        .map(CountryDTO::fromFullEntity)
                ;
    }

    public Uni<List<MovieDTO>> getAllMovies(Long id, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findAllMoviesByCountry(id, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    /**
     * Récupère une liste de films associés à un pays donné, avec pagination, tri et filtrage sur le titre.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les films.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le titre des films (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de films sous forme de {@link MovieDTO}.
     */
    public Uni<List<MovieDTO>> getMovies(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                movieRepository.findMoviesByCountry(id, pageIndex, size, sort, direction, term)
                        .map(movieList ->
                                movieList
                                        .stream()
                                        .map(MovieDTO::fromEntity)
                                        .toList()
                        )
                ;
    }

    /**
     * Récupère une liste de producteurs associés à un pays donné, avec pagination, tri et filtrage sur le nom,
     * et transforme chaque producteur en un {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les producteurs.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des producteurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} correspondant aux critères.
     */
    /*public Uni<List<PersonDTO>> getProducersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                producerRepository.findProducersByCountry(id, pageIndex, size, sort, direction, term)
                        .map(actorList ->
                                actorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste d'acteurs associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les acteurs.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des acteurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée d'acteurs sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getActorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                actorRepository.findActorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(actorList ->
                                actorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de réalisateurs associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les réalisateurs.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des réalisateurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de réalisateurs sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getDirectorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                directorRepository.findDirectorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(directorList ->
                                directorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de scénaristes associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les scénaristes.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des scénaristes (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de scénaristes sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getScreenwritersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                screenwriterRepository.findScreenwritersByCountry(id, pageIndex, size, sort, direction, term)
                        .map(screenwriterList ->
                                screenwriterList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de musiciens associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les musiciens.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des musiciens (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de musiciens sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getMusiciansByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                musicianRepository.findMusiciansByCountry(id, pageIndex, size, sort, direction, term)
                        .map(musicianList ->
                                musicianList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de décorateurs associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les décorateurs.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des décorateurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de décorateurs sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getDecoratorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                decoratorRepository.findDecoratorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(decoratorList ->
                                decoratorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de costumiers associés à un pays donné, avec pagination, tri et filtrage sur le nom.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les costumiers.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des costumiers (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de costumiers sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getCostumiersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                costumierRepository.findCostumiersByMovie(id, pageIndex, size, sort, direction, term)
                        .map(costumierList ->
                                costumierList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de photographes associés à un pays donné, avec pagination, tri et filtrage sur le nom,
     * et transforme chaque photographe en un DTO (Data Transfer Object).
     *
     * @param id        L'identifiant du pays pour lequel rechercher les photographes.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des photographes (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de photographes sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getPhotographersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                photographerRepository.findPhotographersByCountry(id, pageIndex, size, sort, direction, term)
                        .map(photographerList ->
                                photographerList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de monteurs associés à un pays donné, avec pagination, tri et filtrage sur le nom,
     * et transforme chaque monteur en un {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les monteurs.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des monteurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} correspondant aux critères.
     */
    /*public Uni<List<PersonDTO>> getEditorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                editorRepository.findEditorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(editorList ->
                                editorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de castings associés à un pays donné, avec pagination, tri et filtrage sur le nom des castings,
     * et transforme chaque casting en un {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les castings.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des castings (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} correspondant aux critères.
     */
    /*public Uni<List<PersonDTO>> getCastersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                casterRepository.findCastersByMovie(id, pageIndex, size, sort, direction, term)
                        .map(casterList ->
                                casterList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de directeurs artistiques associés à un pays donné, avec pagination, tri et filtrage, puis les transforme en {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les directeurs artistiques.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des directeurs artistiques (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} représentant les directeurs artistiques correspondant aux critères de recherche.
     */
    /*public Uni<List<PersonDTO>> getArtDirectorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                artDirectorRepository.findArtDirectorsByMovie(id, pageIndex, size, sort, direction, term)
                        .map(artDirectorList ->
                                artDirectorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste d'ingénieurs du son associés à un pays donné, avec pagination, tri et filtrage par nom,
     * puis les transforme en une liste de {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel rechercher les ingénieurs du son.
     * @param pageIndex L'index de la page pour la pagination (0 pour la première page).
     * @param size      Le nombre de résultats par page.
     * @param sort      Le champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ASC pour ascendant, DESC pour descendant).
     * @param term      Le terme de recherche à comparer avec le nom des ingénieurs du son (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} représentant les ingénieurs du son.
     */
    /*public Uni<List<PersonDTO>> getSoundEditorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                soundEditorRepository.findSoundEditorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(soundEditorList ->
                                soundEditorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de superviseurs des effets spéciaux associés à un pays donné, avec pagination, tri et filtrage
     * par terme de recherche sur le nom. Les résultats sont retournés sous forme de DTO {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel récupérer les superviseurs des effets spéciaux.
     * @param pageIndex L'indice de la page à récupérer (pour la pagination).
     * @param size      Le nombre d'éléments à récupérer par page.
     * @param sort      Le nom du champ sur lequel trier les résultats.
     * @param direction La direction du tri (croissant ou décroissant).
     * @param term      Le terme de recherche à comparer avec les noms des superviseurs des effets spéciaux (insensible à la casse).
     * @return Un objet {@link Uni} contenant la liste des superviseurs des effets spéciaux sous forme de {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getVisualEffectsSupervisorsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                visualEffectsSupervisorRepository.findVisualEffectsSupervisorsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(visualEffectsSupervisorList ->
                                visualEffectsSupervisorList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère la liste des maquilleurs associés à un pays spécifique, avec pagination et tri. Le résultat est
     * transformé en une liste de DTOs (Data Transfer Object) {@link PersonDTO} pour chaque maquilleur,
     * après avoir filtré les résultats avec un terme de recherche insensible à la casse sur les noms des maquilleurs.
     *
     * @param id        L'identifiant du pays pour lequel récupérer les maquilleurs associés.
     * @param pageIndex L'index de la page pour la pagination des résultats.
     * @param size      Le nombre d'éléments par page pour la pagination.
     * @param sort      Le nom du champ sur lequel trier les résultats (ex. "name", "id").
     * @param direction La direction du tri (croissant ou décroissant).
     * @param term      Le terme de recherche à appliquer sur les noms des maquilleurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant la liste paginée des maquilleurs sous forme de DTOs.
     */
    /*public Uni<List<PersonDTO>> getMakeupArtistsByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                makeupArtistRepository.findMakeupArtistsByCountry(id, pageIndex, size, sort, direction, term)
                        .map(makeupArtistList ->
                                makeupArtistList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste de coiffeurs associés à un pays spécifique, avec des options de pagination, de tri et
     * de filtrage par un terme de recherche insensible à la casse sur le nom des coiffeurs.
     *
     * @param id        L'identifiant du pays pour lequel récupérer les coiffeurs associés.
     * @param pageIndex L'indice de la page pour la pagination.
     * @param size      Le nombre d'éléments par page pour la pagination.
     * @param sort      Le nom du champ sur lequel effectuer le tri.
     * @param direction La direction du tri (ascendant ou descendant).
     * @param term      Le terme de recherche à appliquer sur les noms des coiffeurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant la liste des coiffeurs sous forme de DTO {@link PersonDTO}.
     */
    /*public Uni<List<PersonDTO>> getHairDressersByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                hairDresserRepository.findHairDressersByCountry(id, pageIndex, size, sort, direction, term)
                        .map(hairDresserList ->
                                hairDresserList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    /**
     * Récupère une liste paginée de cascadeurs associés à un pays spécifique et les convertit en objets {@link PersonDTO}.
     *
     * @param id        L'identifiant du pays pour lequel récupérer les cascadeurs associés.
     * @param pageIndex L'index de la page à récupérer (basé sur la pagination).
     * @param size      Le nombre d'éléments par page.
     * @param sort      Le champ selon lequel trier les résultats.
     * @param direction La direction du tri (ASC ou DESC).
     * @param term      Le terme de recherche appliqué au nom des cascadeurs (insensible à la casse).
     * @return Un objet {@link Uni} contenant une liste paginée de {@link PersonDTO} correspondant aux critères.
     */
    /*public Uni<List<PersonDTO>> getStuntmenByCountry(Long id, int pageIndex, int size, String sort, Sort.Direction direction, String term) {
        return
                stuntmanRepository.findStuntmenByCountry(id, pageIndex, size, sort, direction, term)
                        .map(stuntmanList ->
                                stuntmanList
                                        .stream()
                                        .map(PersonDTO::fromEntity)
                                        .toList()
                        )
                ;
    }*/

    public Uni<Country> removeMovie(Long countryId, Long movieId) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(countryId)
                                        .onItem().ifNotNull()
                                        .call(country -> country.removeMovie(movieId))
                        )
                ;
    }

    public Uni<Country> update(Long id, CountryDTO countryDTO) {
        return
                Panache
                        .withTransaction(() ->
                                countryRepository.findById(id)
                                        .onItem().ifNotNull().invoke(
                                                entity -> {
                                                    entity.setCode(countryDTO.getCode());
                                                    entity.setAlpha2(countryDTO.getAlpha2());
                                                    entity.setAlpha3(countryDTO.getAlpha3());
                                                    entity.setNomEnGb(countryDTO.getNomEnGb());
                                                    entity.setNomFrFr(countryDTO.getNomFrFr());
                                                }
                                        )
                        )
                ;
    }
}
