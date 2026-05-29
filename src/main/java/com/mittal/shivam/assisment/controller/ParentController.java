package com.mittal.shivam.assisment.controller;

import com.mittal.shivam.assisment.Entities.Booking;
import com.mittal.shivam.assisment.dto.ParentDtos.*;
import com.mittal.shivam.assisment.dto.ResponseDtos.BookingDetailResponseDto;
import com.mittal.shivam.assisment.dto.ResponseDtos.OfferingResponseDto;
import com.mittal.shivam.assisment.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final BookingService bookingService;

    @GetMapping("/offerings")
    public ResponseEntity<List<OfferingResponseDto>> getAvailableOfferings(Principal principal) {
        return ResponseEntity.ok(bookingService.getAvailableOfferings(principal.getName()));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDetailResponseDto>> getMyBookings(Principal principal) {
        return ResponseEntity.ok(bookingService.getParentBookings(principal.getName()));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> bookOffering(Principal principal, @RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.bookOffering(principal.getName(), request.offeringId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BookingResponse(booking.getId(), booking.getStatus().name()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
