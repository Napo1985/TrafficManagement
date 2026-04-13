package com.traffic.service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.traffic.config.PublicBaseUrlResolver;
import com.traffic.web.dto.CreateLinkResponse;

@Service
public class LinkService {

    private static final String BASE62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int MAX_SLUG_RETRIES = 64;
    private static final Set<String> RESERVED = Set.of(
            "api",
            "health",
            "actuator",
            "swagger",
            "openapi",
            "docs",
            "static",
            "links");

    private final ConcurrentHashMap<String, String> slugToTarget = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final PublicBaseUrlResolver publicBaseUrlResolver;

    public LinkService(PublicBaseUrlResolver publicBaseUrlResolver) {
        this.publicBaseUrlResolver = publicBaseUrlResolver;
    }

    public CreateLinkResponse createLink(String targetUrl) {
        String target = UrlSafety.validateAndNormalize(targetUrl);
        String slug = registerSlug(target);
        String base = publicBaseUrlResolver.baseUrlWithoutTrailingSlash();
        String shortPath = "/" + slug;
        String shortUrl = base + shortPath;
        return new CreateLinkResponse(slug, shortPath, shortUrl);
    }

    public Optional<String> resolve(String slug) {
        if (isReserved(slug)) {
            return Optional.empty();
        }
        return Optional.ofNullable(slugToTarget.get(slug));
    }

    private boolean isReserved(String slug) {
        return RESERVED.contains(slug.toLowerCase(Locale.ROOT));
    }

    private String registerSlug(String target) {
        for (int attempt = 0; attempt < MAX_SLUG_RETRIES; attempt++) {
            String candidate = randomSlug();
            if (isReserved(candidate)) {
                continue;
            }
            if (slugToTarget.putIfAbsent(candidate, target) == null) {
                return candidate;
            }
        }
        throw new SlugGenerationException("Could not allocate a unique slug; try again");
    }

    private String randomSlug() {
        int len = 6 + random.nextInt(3);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
