package com.mittal.shivam.assisment.repository;

import com.mittal.shivam.assisment.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByOfferingId(UUID offeringId);
}
