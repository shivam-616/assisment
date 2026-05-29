package com.mittal.shivam.assisment.dto;

import java.util.List;
import java.util.UUID;

public class ResponseDtos {

    public record SessionResponseDto(
            UUID id,
            String localStartTime,
            String localEndTime
    ) {}

    public record OfferingResponseDto(
            UUID id,
            String title,
            String courseTitle,
            String status,
            List<SessionResponseDto> sessions
    ) {}

    public record BookingDetailResponseDto(
            UUID bookingId,
            String status,
            OfferingResponseDto offering
    ) {}
}
