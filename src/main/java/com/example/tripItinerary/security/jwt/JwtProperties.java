package com.example.tripItinerary.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64 encoded secret key.
     */
    private String secret;

    /**
     * Access token validity in milliseconds.
     */
    private long expiration;

}