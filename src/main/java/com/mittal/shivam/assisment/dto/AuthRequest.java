package com.mittal.shivam.assisment.dto;

import com.mittal.shivam.assisment.model.UserRole;
import lombok.Builder;

@Builder
public record AuthRequest(
        String email,
        UserRole role,
        String timezone
) {}