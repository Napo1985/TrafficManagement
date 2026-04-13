package com.traffic.config;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PublicBaseUrlResolver {

    private final AppProperties appProperties;
    private volatile String runtimeLocalBaseUrl;

    public PublicBaseUrlResolver(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @EventListener(WebServerInitializedEvent.class)
    public void captureLocalPort(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        this.runtimeLocalBaseUrl = "http://localhost:" + port;
    }

    /**
     * Uses {@code app.public-base-url} when set; otherwise the local URL derived from the bound port after the
     * embedded server starts ({@link WebServerInitializedEvent}).
     */
    public String baseUrlWithoutTrailingSlash() {
        String configured = appProperties.baseUrlWithoutTrailingSlash();
        if (!configured.isEmpty()) {
            return configured;
        }
        if (runtimeLocalBaseUrl != null) {
            return runtimeLocalBaseUrl;
        }
        throw new IllegalStateException(
                "app.public-base-url is not set and the server port is not known yet");
    }
}
