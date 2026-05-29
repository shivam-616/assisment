package com.mittal.shivam.assisment.controller;

import com.mittal.shivam.assisment.Entities.Course;
import com.mittal.shivam.assisment.Entities.Offering;
import com.mittal.shivam.assisment.dto.ResponseDtos.OfferingResponseDto;
import com.mittal.shivam.assisment.dto.TeacherDtos.*;
import com.mittal.shivam.assisment.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/offerings")
    public ResponseEntity<List<OfferingResponseDto>> getOfferings(Principal principal) {
        return ResponseEntity.ok(teacherService.getTeacherOfferings(principal.getName()));
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody CourseRequest request) {
        return ResponseEntity.ok(teacherService.createCourse(request));
    }

    @PostMapping("/offerings")
    public ResponseEntity<Offering> createOffering(Principal principal, @RequestBody OfferingRequest request) {
        // principal.getName() automatically extracts the 'subject' (email) from our JWT
        return ResponseEntity.ok(teacherService.createOffering(principal.getName(), request));
    }

    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<String> addSessions(
            @PathVariable UUID offeringId,
            @RequestBody List<SessionRequest> requests) {
        teacherService.addSessionsToOffering(offeringId, requests);
        return ResponseEntity.ok("Sessions successfully added and offering published.");
    }
}