package com.traffic.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLinkRequest(@NotBlank String targetUrl, String source) {}
