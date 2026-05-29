package com.mittal.shivam.assisment.service;

import com.mittal.shivam.assisment.Entities.Booking;
import com.mittal.shivam.assisment.Entities.Offering;
import com.mittal.shivam.assisment.Entities.Session;
import com.mittal.shivam.assisment.Entities.User;
import com.mittal.shivam.assisment.dto.ResponseDtos.*;
import com.mittal.shivam.assisment.model.BookingStatus;
import com.mittal.shivam.assisment.model.OfferingStatus;
import com.mittal.shivam.assisment.repository.BookingRepository;
import com.mittal.shivam.assisment.repository.OfferingRepository;
import com.mittal.shivam.assisment.repository.SessionRepository;
import com.mittal.shivam.assisment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final LockService lockService;
    private final com.mittal.shivam.assisment.service.TimezoneService timezoneService;

    @Transactional(readOnly = true)
    public List<OfferingResponseDto> getAvailableOfferings(String parentEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<Offering> offerings = offeringRepository.findByStatus(OfferingStatus.PUBLISHED);

        return offerings.stream()
                .map(offering -> mapToOfferingResponse(offering, parent.getTimezone()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDetailResponseDto> getParentBookings(String parentEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<Booking> bookings = bookingRepository.findByParentIdAndStatus(parent.getId(), BookingStatus.CONFIRMED);

        return bookings.stream()
                .map(booking -> new BookingDetailResponseDto(
                        booking.getId(),
                        booking.getStatus().name(),
                        mapToOfferingResponse(booking.getOffering(), parent.getTimezone())
                )).collect(Collectors.toList());
    }

    private OfferingResponseDto mapToOfferingResponse(Offering offering, String timezone) {
        List<SessionResponseDto> sessionDtos = offering.getSessions().stream()
                .map(session -> new SessionResponseDto(
                        session.getId(),
                        timezoneService.convertToLocal(session.getStartTimeUtc(), timezone).toString(),
                        timezoneService.convertToLocal(session.getEndTimeUtc(), timezone).toString()
                )).collect(Collectors.toList());

        return new OfferingResponseDto(
                offering.getId(),
                offering.getTitle(),
                offering.getCourse().getTitle(),
                offering.getStatus().name(),
                sessionDtos
        );
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking bookOffering(String parentEmail, UUID offeringId) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        String lockKey = "booking_lock:parent:" + parent.getId();
        if (!lockService.acquireLock(lockKey, Duration.ofSeconds(10))) {
            throw new IllegalStateException("Could not acquire booking lock. Please try again.");
        }

        try {
            Offering offering = offeringRepository.findById(offeringId)
                    .orElseThrow(() -> new RuntimeException("Offering not found"));

            List<Session> targetSessions = sessionRepository.findByOfferingId(offeringId);
            if (targetSessions.isEmpty()) {
                throw new IllegalStateException("Offering has no sessions");
            }

            for (Session session : targetSessions) {
                if (sessionRepository.existsOverlappingBooking(parent.getId(),
                        session.getStartTimeUtc(), session.getEndTimeUtc())) {
                    throw new IllegalStateException("Double-booking detected: You have an overlapping session.");
                }
            }

            Booking booking = Booking.builder()
                    .parent(parent)
                    .offering(offering)
                    .status(BookingStatus.CONFIRMED)
                    .build();

            return bookingRepository.save(booking);
        } finally {
            lockService.releaseLock(lockKey);
        }
    }
}
