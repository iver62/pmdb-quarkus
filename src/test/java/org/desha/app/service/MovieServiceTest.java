package org.desha.app.service;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.desha.app.domain.dto.CriteriaDTO;
import org.desha.app.repository.MovieRepository;
import org.desha.app.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private MovieService movieService; // La classe qui contient la méthode count()

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCount() {
        // GIVEN
        CriteriaDTO criteriaDTO = new CriteriaDTO();
        Long expectedCount = 5L;

        when(movieRepository.countMovies(criteriaDTO))
                .thenReturn(Uni.createFrom().item(expectedCount));

        Long result = movieService.count(criteriaDTO).await().indefinitely(); // On "bloque" pour obtenir le résultat
        assertThat(result).isEqualTo(expectedCount);

        verify(movieRepository, times(1)).countMovies(criteriaDTO);
    }

    @Test
    void testCountPersonsByMovie() {
        // GIVEN
        Long movieId = 1L;
        CriteriaDTO criteriaDTO = new CriteriaDTO();
        Long expectedCount = 10L;

        when(personRepository.countPersonsByMovie(movieId, criteriaDTO))
                .thenReturn(Uni.createFrom().item(expectedCount));

        // THEN
        Long result = movieService.countPersonsByMovie(movieId, criteriaDTO).await().indefinitely();
        assertThat(result).isEqualTo(expectedCount);

        verify(personRepository, times(1)).countPersonsByMovie(movieId, criteriaDTO);
    }
}
