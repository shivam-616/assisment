package com.mittal.shivam.assisment.dto;

import java.util.UUID;

public class ParentDtos {
    public record BookingRequest(UUID offeringId) {}
    public record BookingResponse(UUID bookingId, String status) {}
}
