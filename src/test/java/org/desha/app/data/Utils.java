package org.desha.app.data;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Utils {

    private final Random random = new Random();
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Long randomLong(int origin, int bound) {
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    public Integer randomInteger(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    public String generateAlphabeticString(int length) {
        return RandomStringUtils.random(length, 'a', 'z' + 1, true, false, null, random);
    }

    public String generateAlphanumericString(int length) {
        return RandomStringUtils.random(length, 48, 122 + 1, true, true, null, random);
        // 48='0', 122='z' pour couvrir lettres et chiffres
    }

    public LocalDate generateRandomDate() {
        return
                LocalDate.of(
                        ThreadLocalRandom.current().nextInt(1900, 2000),
                        ThreadLocalRandom.current().nextInt(1, 13),
                        ThreadLocalRandom.current().nextInt(1, 28)
                );
    }

    public LocalDateTime generateRandomDateTime() {
        return
                LocalDateTime.of(
                        generateRandomDate(),
                        LocalTime.of(
                                ThreadLocalRandom.current().nextInt(0, 24),
                                ThreadLocalRandom.current().nextInt(0, 60),
                                ThreadLocalRandom.current().nextInt(0, 60)
                        )
                );
    }
}
