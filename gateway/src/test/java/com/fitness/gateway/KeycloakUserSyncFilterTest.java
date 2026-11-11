
package com.fitness.gateway;

import com.fitness.gateway.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class KeycloakUserSyncFilterTest {

    @org.mockito.Mock
    private UserService userService;

    private KeycloakUserSyncFilter filter;

    private JwtAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        filter = new KeycloakUserSyncFilter(userService);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("keycloak-user-123")
                .claim("email", "test@example.com")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        authentication = new JwtAuthenticationToken(jwt);
    }

    @Test
    void filterShouldRegisterNewUserAndSetAuthenticatedUserId() {
        when(userService.validateUser("keycloak-user-123"))
                .thenReturn(Mono.just(false));

        when(userService.registerUser(any()))
                .thenReturn(Mono.empty());

        AtomicReference<String> forwardedUserId = new AtomicReference<>();

        ServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/activities")
                        .header("X-User-ID", "spoofed-user")
                        .build()
        );

        Mono<Void> result = filter.filter(exchange, forwardedExchange -> {
            forwardedUserId.set(
                    forwardedExchange.getRequest()
                            .getHeaders()
                            .getFirst("X-User-ID")
            );
            return Mono.empty();
        }).contextWrite(
                ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(authentication))
                )
        );

        result.block();

        assertEquals("keycloak-user-123", forwardedUserId.get());

        verify(userService).validateUser("keycloak-user-123");
        verify(userService).registerUser(any());
    }

    @Test
    void filterShouldSkipRegistrationForExistingUser() {
        when(userService.validateUser("keycloak-user-123"))
                .thenReturn(Mono.just(true));

        AtomicReference<String> forwardedUserId = new AtomicReference<>();

        ServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/activities")
                        .build()
        );

        Mono<Void> result = filter.filter(exchange, forwardedExchange -> {
            forwardedUserId.set(
                    forwardedExchange.getRequest()
                            .getHeaders()
                            .getFirst("X-User-ID")
            );
            return Mono.empty();
        }).contextWrite(
                ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(authentication))
                )
        );

        result.block();

        assertEquals("keycloak-user-123", forwardedUserId.get());

        verify(userService).validateUser("keycloak-user-123");
        verify(userService, never()).registerUser(any());
    }

    @Test
    void filterShouldPropagateValidationFailure() {
        when(userService.validateUser("keycloak-user-123"))
                .thenReturn(Mono.error(
                        new RuntimeException("User validation failed")
                ));

        ServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/activities")
                        .build()
        );

        Mono<Void> result = filter.filter(exchange, ignored -> Mono.empty())
                .contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication))
                        )
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                result::block
        );

        assertEquals("User validation failed", exception.getMessage());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void filterShouldPropagateRegistrationFailure() {
        when(userService.validateUser("keycloak-user-123"))
                .thenReturn(Mono.just(false));

        when(userService.registerUser(any()))
                .thenReturn(Mono.error(
                        new RuntimeException("Registration failed")
                ));

        ServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/activities")
                        .build()
        );

        Mono<Void> result = filter.filter(exchange, ignored -> Mono.empty())
                .contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication))
                        )
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                result::block
        );

        assertEquals("Registration failed", exception.getMessage());
    }

    @Test
    void filterShouldContinueWithoutJwtAuthentication() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/activities")
                        .build()
        );

        AtomicReference<Boolean> forwarded = new AtomicReference<>(false);

        Mono<Void> result = filter.filter(exchange, ignored -> {
            forwarded.set(true);
            return Mono.empty();
        });

        result.block();

        assertTrue(forwarded.get());
        verifyNoInteractions(userService);
    }
}
