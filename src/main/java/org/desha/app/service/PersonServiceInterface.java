package org.desha.app.service;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.CountryDTO;
import org.desha.app.domain.dto.CriteriasDTO;
import org.desha.app.domain.dto.MovieDTO;
import org.desha.app.domain.dto.PersonDTO;
import org.desha.app.domain.entity.Person;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;
import java.util.Set;

public interface PersonServiceInterface {

    Uni<Long> countPersons(CriteriasDTO criteriasDTO);

    Uni<Long> countCountries(String term, String lang);

    Uni<Long> countMovies(Long personId, CriteriasDTO criteriasDTO);

    Uni<PersonDTO> getById(Long id);

    Uni<List<Person>> getByIds(List<Long> ids);

    Uni<List<CountryDTO>> getCountries(Page page, String sort, Sort.Direction direction, String term, String lang);

    Uni<List<PersonDTO>> getPersons(Page page, String sort, Sort.Direction direction, CriteriasDTO criteriasDTO);

    Uni<List<PersonDTO>> getAll();

    Uni<List<MovieDTO>> getMovies(Long id, Page page, String sort, Sort.Direction sortDirection, CriteriasDTO criteriasDTO);

//    Uni<List<Movie>> addMovie(Long personId, Movie movie);

//    Uni<List<Movie>> removeMovie(Long personId, Long movieId);

    Uni<PersonDTO> save(PersonDTO personDTO);

    Uni<PersonDTO> update(Long id, FileUpload file, PersonDTO personDTO);

    /**
     * Remplace les pays associés à une personne par un nouvel ensemble de pays.
     * <p>
     * Cette méthode effectue les opérations suivantes :
     * <ul>
     *   <li>Récupère la personne via son identifiant</li>
     *   <li>Lève une exception si la personne n'existe pas</li>
     *   <li>Récupère les entités {@link org.desha.app.domain.entity.Country} correspondantes aux identifiants fournis dans les {@link CountryDTO}</li>
     *   <li>Met à jour la liste des pays de la personne et sa date de mise à jour</li>
     *   <li>Persiste la personne mise à jour</li>
     *   <li>Retourne l'ensemble des pays associés sous forme de {@link CountryDTO}</li>
     * </ul>
     * </p>
     *
     * @param id            l'identifiant de la personne à mettre à jour
     * @param countryDTOSet l'ensemble des pays (DTO) à associer à la personne
     * @return un {@link Uni} contenant l'ensemble des pays mis à jour sous forme de DTO
     * @throws IllegalArgumentException si la personne avec l'identifiant donné n'est pas trouvée
     */
    Uni<Set<CountryDTO>> updateCountries(Long id, Set<CountryDTO> countryDTOSet);

    /**
     * Ajoute des pays à une personne identifiée par son ID.
     * <p>
     * Cette méthode effectue les opérations suivantes :
     * <ul>
     *   <li>Récupère la personne via son identifiant</li>
     *   <li>Lève une exception si la personne n'existe pas</li>
     *   <li>Récupère les entités {@link org.desha.app.domain.entity.Country} correspondantes aux identifiants des {@link CountryDTO}</li>
     *   <li>Lève une exception si un ou plusieurs pays ne sont pas trouvés</li>
     *   <li>Ajoute les pays trouvés à la personne</li>
     *   <li>Persiste la personne mise à jour</li>
     *   <li>Retourne l'ensemble des pays associés sous forme de {@link CountryDTO}</li>
     * </ul>
     * </p>
     *
     * @param id            l'identifiant de la personne à mettre à jour
     * @param countryDTOSet l'ensemble des pays (DTO) à ajouter à la personne
     * @return un {@link Uni} contenant l'ensemble mis à jour des pays sous forme de DTO
     * @throws IllegalArgumentException si la personne n'est pas trouvée ou si un ou plusieurs pays sont introuvables
     */
    Uni<Set<CountryDTO>> addCountries(Long id, Set<CountryDTO> countryDTOSet);

    /**
     * Supprime un pays associé à une personne à partir de leurs identifiants.
     * <p>
     * Cette méthode effectue les étapes suivantes :
     * <ul>
     *   <li>Recherche de la personne à partir de son identifiant</li>
     *   <li>Lève une exception si la personne n'est pas trouvée</li>
     *   <li>Supprime le pays correspondant à l'identifiant fourni de l'ensemble des pays associés</li>
     *   <li>Persiste la personne mise à jour</li>
     *   <li>Retourne la liste actualisée des pays associés sous forme de {@link CountryDTO}</li>
     * </ul>
     * </p>
     *
     * @param personId  l'identifiant de la personne concernée
     * @param countryId l'identifiant du pays à retirer
     * @return un {@link Uni} contenant la liste mise à jour des pays sous forme de DTO
     * @throws IllegalArgumentException si la personne n'est pas trouvée
     */
    Uni<Set<CountryDTO>> removeCountry(Long personId, Long countryId);

    /**
     * Supprime une personne à partir de son identifiant.
     * <p>
     * Cette méthode effectue la suppression dans une transaction. Si aucune personne ne correspond à l'identifiant fourni,
     * une exception {@link IllegalArgumentException} est levée avec le message "Personne introuvable".
     * </p>
     *
     * @param id l'identifiant de la personne à supprimer
     * @return un {@link Uni} contenant {@code true} si la personne a été supprimée
     * @throws IllegalArgumentException si aucune personne ne correspond à l'identifiant
     */
    Uni<Boolean> deletePerson(Long id);

    /**
     * Supprime tous les pays associés à une personne donnée.
     * <p>
     * Cette méthode effectue les opérations suivantes :
     * <ul>
     *   <li>Recherche de la personne par son identifiant</li>
     *   <li>Lève une exception si la personne est introuvable</li>
     *   <li>Vide l'ensemble des pays associés à la personne</li>
     *   <li>Persiste les modifications</li>
     *   <li>Retourne {@code true} si l'opération s'est déroulée avec succès</li>
     * </ul>
     * En cas d'erreur, une exception {@link jakarta.ws.rs.WebApplicationException} est levée avec un message explicite.
     * </p>
     *
     * @param id l'identifiant de la personne concernée
     * @return un {@link Uni} contenant {@code true} si les pays ont été supprimés avec succès
     * @throws jakarta.ws.rs.WebApplicationException si une erreur survient lors de la suppression
     */
    Uni<Boolean> clearCountries(Long id);
}
