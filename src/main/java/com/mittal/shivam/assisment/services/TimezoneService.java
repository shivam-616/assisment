package com.mittal.shivam.assisment.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimezoneService {

    /**
     * Converts a local wall-clock time from a specific timezone into an absolute UTC Instant.
     * Used when saving a Teacher's session to the DB.
     */
    public Instant convertToUtc(LocalDateTime localDateTime, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        return localDateTime.atZone(zoneId).toInstant();
    }

    /**
     * Converts a UTC Instant back to a specific local timezone.
     * Used when a Parent or Teacher is viewing a schedule.
     */
    public ZonedDateTime convertToLocal(Instant utcInstant, String targetTimezone) {
        ZoneId zoneId = ZoneId.of(targetTimezone);
        return utcInstant.atZone(zoneId);
    }
}