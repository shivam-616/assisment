package com.mittal.shivam.assisment.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimezoneServiceTest {

    private final TimezoneService timezoneService = new TimezoneService();

    @Test
    void convertToUtc_ShouldTranslateNewYorkToUtcCorrectly() {
        // New York is UTC-4 in May (Daylight Savings)
        LocalDateTime localDateTime = LocalDateTime.of(2026, 5, 28, 10, 0); // 10:00 AM NY
        String timezone = "America/New_York";

        Instant result = timezoneService.convertToUtc(localDateTime, timezone);

        // 10:00 AM NY (UTC-4) -> 2:00 PM UTC (14:00)
        Instant expected = Instant.parse("2026-05-28T14:00:00Z");
        assertEquals(expected, result);
    }

    @Test
    void convertToLocal_ShouldTranslateUtcToKolkataCorrectly() {
        // UTC Time
        Instant utcInstant = Instant.parse("2026-05-28T10:00:00Z");
        String targetTimezone = "Asia/Kolkata"; // UTC+5:30

        ZonedDateTime result = timezoneService.convertToLocal(utcInstant, targetTimezone);

        // 10:00 UTC -> 15:30 Kolkata (3:30 PM)
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(ZoneId.of("Asia/Kolkata"), result.getZone());
    }
}
