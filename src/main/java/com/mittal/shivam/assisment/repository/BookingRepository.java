package com.mittal.shivam.assisment.repository;

import com.mittal.shivam.assisment.Entities.Booking;
import com.mittal.shivam.assisment.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByParentIdAndStatus(UUID parentId, BookingStatus status);
}
