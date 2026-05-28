package com.mittal.shivam.assisment.service;

import com.mittal.shivam.assisment.Entities.Course;
import com.mittal.shivam.assisment.Entities.Offering;
import com.mittal.shivam.assisment.Entities.Session;
import com.mittal.shivam.assisment.Entities.User;
import com.mittal.shivam.assisment.dto.TeacherDtos.*;
import com.mittal.shivam.assisment.model.OfferingStatus;
import com.mittal.shivam.assisment.repository.CourseRepository;
import com.mittal.shivam.assisment.repository.OfferingRepository;
import com.mittal.shivam.assisment.repository.SessionRepository;
import com.mittal.shivam.assisment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherService {

    private final CourseRepository courseRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final com.mittal.shivam.assisment.service.TimezoneService timezoneService;

    public TeacherService(CourseRepository courseRepository, OfferingRepository offeringRepository,
                          SessionRepository sessionRepository, UserRepository userRepository,
                          com.mittal.shivam.assisment.service.TimezoneService timezoneService) {
        this.courseRepository = courseRepository;
        this.offeringRepository = offeringRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.timezoneService = timezoneService;
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .build();
        return courseRepository.save(course);
    }

    @Transactional
    public Offering createOffering(String teacherEmail, OfferingRequest request) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .title(request.title())
                .status(OfferingStatus.DRAFT) // Draft until sessions are added
                .build();

        return offeringRepository.save(offering);
    }

    @Transactional
    public void addSessionsToOffering(UUID offeringId, List<SessionRequest> sessionRequests) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new RuntimeException("Offering not found"));

        List<Session> sessions = sessionRequests.stream().map(req -> {
            // Validation: End time must be after start time
            if (req.endTime().isBefore(req.startTime())) {
                throw new IllegalArgumentException("End time cannot be before start time");
            }

            // Convert local times to absolute UTC Instants
            Instant startUtc = timezoneService.convertToUtc(req.startTime(), req.teacherTimezone());
            Instant endUtc = timezoneService.convertToUtc(req.endTime(), req.teacherTimezone());

            return Session.builder()
                    .offering(offering)
                    .startTimeUtc(startUtc)
                    .endTimeUtc(endUtc)
                    .teacherTimezone(req.teacherTimezone())
                    .build();
        }).toList();

        sessionRepository.saveAll(sessions);

        // Auto-publish the offering once it has sessions
        offering.setStatus(OfferingStatus.PUBLISHED);
        offeringRepository.save(offering);
    }
}