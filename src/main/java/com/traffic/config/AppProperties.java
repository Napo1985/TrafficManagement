package com.traffic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String publicBaseUrl) {

    public String baseUrlWithoutTrailingSlash() {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return "";
        }
        String s = publicBaseUrl.strip();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
