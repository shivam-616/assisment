package com.mittal.shivam.assisment.dto;

import java.util.UUID;
import lombok.Builder;
import java.time.LocalDateTime;

public class TeacherDtos {

    @Builder
    public record CourseRequest(String title, String description) {}

    @Builder
    public record OfferingRequest(UUID courseId, String title) {}

    @Builder
    public record SessionRequest(
            LocalDateTime startTime, // e.g., "2025-10-16T18:00:00"
            LocalDateTime endTime,
            String teacherTimezone   // e.g., "America/New_York"
    ) {}
}