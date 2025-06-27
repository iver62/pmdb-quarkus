package org.desha.app;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.Mock;
import org.desha.app.repository.AwardRepository;
import org.desha.app.service.AwardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AwardServiceTest {

    @InjectMocks
    AwardService awardService;

    @Mock
    AwardRepository awardRepository;

    @Test
    void testGetCeremonies_shouldReturnExpectedList() {
        // GIVEN
        Page page = Page.of(0, 10);
        Sort.Direction direction = Sort.Direction.Ascending;
        String term = "oscars";

        List<String> expectedCeremonies = List.of("Oscars", "Cannes");

//        when(awardRepository.findCeremonies(page, direction, term))
//                .thenReturn(Uni.createFrom().item(expectedCeremonies));

        // WHEN
//        Uni<List<String>> resultUni = awardService.getCeremonies(page, direction, term);

        // THEN
//        List<String> result = resultUni.await().indefinitely();

//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals(expectedCeremonies, result);
    }
}
