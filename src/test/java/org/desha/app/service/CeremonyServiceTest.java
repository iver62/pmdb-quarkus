package org.desha.app.service;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CeremonyServiceTest {

//    @InjectMocks
//    CeremonyService ceremonyService;
//
//    @Mock
//    CeremonyRepository ceremonyRepository;
//
//    @Test
//    void testGetCeremonies_shouldReturnExpectedList() {
//        // GIVEN
//        Page page = Page.of(0, 10);
//        Sort.Direction direction = Sort.Direction.Ascending;
//        String term = "oscars";
//
//        List<CeremonyDTO> expectedCeremonies = List.of(
//                CeremonyDTO.build(1L, "Oscars"),
//                CeremonyDTO.build(2L, "Césars")
//        );

//        when(ceremonyRepository.findCeremonies(page, direction, term))
//                .thenReturn(Uni.createFrom().item(expectedCeremonies));
//
//        // WHEN
//        Uni<Set<CeremonyDTO>> resultUni = ceremonyService.getCeremonies(page, direction, term);
//
//        // THEN
//        Set<CeremonyDTO> result = resultUni.await().indefinitely();
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals(expectedCeremonies, result);
//    }
}
