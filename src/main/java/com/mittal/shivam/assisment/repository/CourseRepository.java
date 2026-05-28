package com.mittal.shivam.assisment.repository;

import com.mittal.shivam.assisment.Entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
}
