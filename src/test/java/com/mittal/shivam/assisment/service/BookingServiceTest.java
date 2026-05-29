package com.mittal.shivam.assisment.service;

import com.mittal.shivam.assisment.Entities.Booking;
import com.mittal.shivam.assisment.Entities.Offering;
import com.mittal.shivam.assisment.Entities.Session;
import com.mittal.shivam.assisment.Entities.User;
import com.mittal.shivam.assisment.model.BookingStatus;
import com.mittal.shivam.assisment.repository.BookingRepository;
import com.mittal.shivam.assisment.repository.OfferingRepository;
import com.mittal.shivam.assisment.repository.SessionRepository;
import com.mittal.shivam.assisment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private OfferingRepository offeringRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private LockService lockService;
    @Mock private TimezoneService timezoneService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void bookOffering_ShouldFail_WhenLockCannotBeAcquired() {
        String email = "parent@example.com";
        UUID offeringId = UUID.randomUUID();
        User parent = User.builder().id(UUID.randomUUID()).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(parent));
        when(lockService.acquireLock(anyString(), any(Duration.class))).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> bookingService.bookOffering(email, offeringId));
        verify(lockService, never()).releaseLock(anyString());
    }

    @Test
    void bookOffering_ShouldFail_WhenOverlapDetected() {
        String email = "parent@example.com";
        UUID offeringId = UUID.randomUUID();
        User parent = User.builder().id(UUID.randomUUID()).email(email).build();
        Offering offering = Offering.builder().id(offeringId).build();
        Session session = Session.builder()
                .startTimeUtc(Instant.now())
                .endTimeUtc(Instant.now().plus(Duration.ofHours(1)))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(parent));
        when(lockService.acquireLock(anyString(), any(Duration.class))).thenReturn(true);
        when(offeringRepository.findById(offeringId)).thenReturn(Optional.of(offering));
        when(sessionRepository.findByOfferingId(offeringId)).thenReturn(List.of(session));
        
        // Mock overlap detection
        when(sessionRepository.existsOverlappingBooking(eq(parent.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookingService.bookOffering(email, offeringId));
        
        // CRITICAL: Ensure lock is released even if overlap occurs
        verify(lockService).releaseLock(anyString());
    }

    @Test
    void bookOffering_ShouldSucceed_WhenNoOverlaps() {
        String email = "parent@example.com";
        UUID offeringId = UUID.randomUUID();
        User parent = User.builder().id(UUID.randomUUID()).email(email).build();
        Offering offering = Offering.builder().id(offeringId).build();
        Session session = Session.builder()
                .startTimeUtc(Instant.now())
                .endTimeUtc(Instant.now().plus(Duration.ofHours(1)))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(parent));
        when(lockService.acquireLock(anyString(), any(Duration.class))).thenReturn(true);
        when(offeringRepository.findById(offeringId)).thenReturn(Optional.of(offering));
        when(sessionRepository.findByOfferingId(offeringId)).thenReturn(List.of(session));
        when(sessionRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        Booking result = bookingService.bookOffering(email, offeringId);

        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
        verify(lockService).releaseLock(anyString());
    }
}
