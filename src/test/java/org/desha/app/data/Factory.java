package org.desha.app.data;

import lombok.experimental.UtilityClass;
import org.desha.app.domain.dto.*;
import org.desha.app.domain.entity.Category;
import org.desha.app.domain.record.Repartition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class Factory {

    public MovieDTO mockMovieDTO() {
        return
                MovieDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(10),
                        Utils.generateAlphabeticString(20),
                        Utils.generateAlphabeticString(200),
                        Utils.generateRandomDate(),
                        Utils.randomLong(1, 500),
                        Utils.randomLong(1, 1000000),
                        Utils.generateAlphabeticString(10),
                        Utils.randomLong(1, 1000000),
                        Utils.generateAlphabeticString(10),
                        Utils.generateAlphanumericString(20),
                        Utils.generateRandomDateTime(),
                        Utils.generateRandomDateTime()
                );
    }

    public LitePersonDTO mockLitePersonDTO() {
        return
                LitePersonDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(10),
                        Utils.generateAlphanumericString(20),
                        Utils.generateRandomDate(),
                        Utils.generateRandomDate()
                );
    }

    public TechnicalTeamDTO mockTechnicalTeamDTO() {
        return
                TechnicalTeamDTO.build(
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10)),
                        mockMovieTechnicianDTOList(Utils.randomInteger(1, 10))
                );
    }

    public MovieActorDTO mockMovieActorDTO() {
        return
                MovieActorDTO.build(
                        Utils.randomLong(1, 1000),
                        mockLitePersonDTO(),
                        Utils.generateAlphanumericString(20),
                        Utils.randomInteger(1, 100)
                );
    }

    public MovieTechnicianDTO mockMovieTechnicianDTO() {
        return
                MovieTechnicianDTO.build(
                        Utils.randomLong(1, 1000),
                        mockLitePersonDTO(),
                        Utils.generateAlphanumericString(20)
                );
    }

    public Category mockCategory() {
        return
                Category.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(10)
                );
    }

    public CategoryDTO mockCategoryDTO() {
        return
                CategoryDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(10),
                        Utils.generateRandomDateTime(),
                        Utils.generateRandomDateTime()
                );
    }

    public CountryDTO mockCountryDTO() {
        return
                CountryDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.randomInteger(100, 1000),
                        Utils.generateAlphabeticString(2),
                        Utils.generateAlphabeticString(3),
                        Utils.generateAlphabeticString(10),
                        Utils.generateAlphabeticString(10)
                );
    }

    public CeremonyAwardsDTO mockCeremonyAwardsDTO() {
        return
                CeremonyAwardsDTO.build(
                        Utils.randomLong(1, 1000),
                        mockCeremonyDTO(),
                        mockAwardDTOList(5)
                );
    }

    public CeremonyDTO mockCeremonyDTO() {
        return
                CeremonyDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(20)
                );
    }

    public AwardDTO mockAwardDTO() {
        return
                AwardDTO.build(
                        Utils.randomLong(1, 1000),
                        Utils.generateAlphabeticString(20),
                        mockLitePersonDTOSet(5),
                        Year.of(Utils.randomInteger(1900, 2000))
                );
    }

    public Repartition mockRepartition() {
        return new Repartition(
                Utils.generateAlphabeticString(10),
                Utils.randomLong(10, 100)
        );
    }

    public List<MovieDTO> mockMovieDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockMovieDTO())
                        .toList()
                ;
    }

    public List<LitePersonDTO> mockLitePersonDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockLitePersonDTO())
                        .toList()
                ;
    }

    public Set<LitePersonDTO> mockLitePersonDTOSet(int length) {
        return new HashSet<>(mockLitePersonDTOList(length));
    }

    public List<MovieActorDTO> mockMovieActorDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockMovieActorDTO())
                        .toList()
                ;
    }

    public List<MovieTechnicianDTO> mockMovieTechnicianDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockMovieTechnicianDTO())
                        .toList()
                ;
    }

    public List<CategoryDTO> mockCategoryDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockCategoryDTO())
                        .toList()
                ;
    }

    public Set<CategoryDTO> mockCategoryDTOSet(int length) {
        return new HashSet<>(mockCategoryDTOList(length));
    }

    public List<CountryDTO> mockCountryDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockCountryDTO())
                        .toList()
                ;
    }

    public Set<CountryDTO> mockCountryDTOSet(int length) {
        return new HashSet<>(mockCountryDTOList(length));
    }

    public Set<CeremonyDTO> mockCeremonyDTOSet(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockCeremonyDTO())
                        .collect(Collectors.toSet())
                ;
    }

    public List<CeremonyAwardsDTO> mockCeremonyAwardsDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockCeremonyAwardsDTO())
                        .toList()
                ;
    }

    public Set<CeremonyAwardsDTO> mockCeremonyAwardsDTOSet(int length) {
        return new HashSet<>(mockCeremonyAwardsDTOList(length));
    }

    public List<AwardDTO> mockAwardDTOList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockAwardDTO())
                        .toList()
                ;
    }

    public List<Repartition> mockRepartitionList(int length) {
        return
                IntStream.range(0, length)
                        .mapToObj(i -> mockRepartition())
                        .toList()
                ;
    }

    public File mockFile() throws IOException {
        File tempFile = File.createTempFile("test-upload", ".jpg");
        Files.writeString(tempFile.toPath(), "fake image data");
        return tempFile;
    }
}
