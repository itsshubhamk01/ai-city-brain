package com.aicitybrain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * One shared {@link HttpClient} for every outbound call this backend makes (weather,
 * geocoding, Mumbai infrastructure, the optional LLM layer). Each {@code HttpClient}
 * instance owns its own internal thread pool; on a free-tier container with 0.1 CPU
 * and 512MB RAM, four separate clients is real, avoidable overhead for no benefit —
 * consolidating to one is both leaner and simpler.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient sharedHttpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    }
}
