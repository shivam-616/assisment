package com.mittal.shivam.assisment.repository;

import com.mittal.shivam.assisment.Entities.Offering;
import com.mittal.shivam.assisment.model.OfferingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {
    List<Offering> findByTeacherId(UUID teacherId);
    List<Offering> findByStatus(OfferingStatus status);
}
