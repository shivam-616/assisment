package com.mittal.shivam.assisment.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String role,
        String timezone
) {}