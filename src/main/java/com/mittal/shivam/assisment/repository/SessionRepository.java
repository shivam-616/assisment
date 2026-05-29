package com.mittal.shivam.assisment.repository;

import com.mittal.shivam.assisment.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByOfferingId(UUID offeringId);

    @Query("SELECT COUNT(s) > 0 FROM Session s " +
            "JOIN Booking b ON s.offering.id = b.offering.id " +
            "WHERE b.parent.id = :parentId " +
            "AND b.status = 'CONFIRMED' " +
            "AND s.startTimeUtc < :targetEnd " +
            "AND s.endTimeUtc > :targetStart")
    boolean existsOverlappingBooking(@Param("parentId") UUID parentId,
                                     @Param("targetStart") Instant targetStart,
                                     @Param("targetEnd") Instant targetEnd);
}
