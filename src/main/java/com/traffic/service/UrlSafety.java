package com.traffic.service;

import java.net.URI;

public final class UrlSafety {

    private UrlSafety() {}

    /**
     * Validates {@code raw} as an absolute http(s) URL and returns a normalized string suitable for redirects.
     */
    public static String validateAndNormalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new UrlValidationException("targetUrl is required");
        }
        String trimmed = raw.strip();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new UrlValidationException("Invalid URL");
        }
        if (!uri.isAbsolute()) {
            throw new UrlValidationException("URL must be absolute (include scheme and host)");
        }
        if (uri.isOpaque()) {
            throw new UrlValidationException("Opaque URLs are not allowed");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new UrlValidationException("Only http and https URLs are allowed");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new UrlValidationException("URL must include a host");
        }
        if (uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) {
            throw new UrlValidationException("URLs with user info are not allowed");
        }
        return uri.normalize().toString();
    }
}
