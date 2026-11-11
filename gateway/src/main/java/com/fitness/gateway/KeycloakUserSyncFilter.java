// package com.fitness.gateway;

// import com.fitness.gateway.user.RegisterRequest;
// import com.fitness.gateway.user.UserService;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.core.Ordered;
// import org.springframework.core.annotation.Order;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
// import org.springframework.security.core.context.ReactiveSecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.web.server.WebFilter;
// import org.springframework.web.server.WebFilterChain;
// import reactor.core.publisher.Mono;

// @Component
// @Slf4j
// @RequiredArgsConstructor
// @Order(Ordered.LOWEST_PRECEDENCE)
// public class KeycloakUserSyncFilter implements WebFilter {

//     private final UserService userService;

//     @Override
//     public Mono<Void> filter(
//             ServerWebExchange exchange,
//             WebFilterChain chain) {

//         return ReactiveSecurityContextHolder.getContext()
//                 .map(SecurityContext::getAuthentication)
//                 .filter(JwtAuthenticationToken.class::isInstance)
//                 .cast(JwtAuthenticationToken.class)
//                 .flatMap(authentication -> {

//                     var jwt = authentication.getToken();

//                     // Obtain identity from the validated JWT.
//                     String userId = jwt.getSubject();

//                     RegisterRequest request = new RegisterRequest();
//                     request.setKeycloakId(userId);
//                     request.setEmail(jwt.getClaimAsString("email"));
//                     request.setFirstName(
//                             jwt.getClaimAsString("given_name"));
//                     request.setLastName(
//                             jwt.getClaimAsString("family_name"));
//                     request.setPassword("dummy@123123");

//                     return userService.validateUser(userId)
//                             .flatMap(exists -> {
//                                 if (Boolean.TRUE.equals(exists)) {
//                                     log.debug(
//                                             "User already synchronized: {}",
//                                             userId
//                                     );
//                                     return Mono.empty();
//                                 }

//                                 log.info(
//                                         "Synchronizing authenticated user: {}",
//                                         userId
//                                 );

//                                 return userService
//                                         .registerUser(request)
//                                         .then();
//                             })
//                             .then(Mono.defer(() -> {

//                                 ServerHttpRequest mutatedRequest =
//                                         exchange.getRequest()
//                                                 .mutate()
//                                                 .headers(headers -> {
//                                                     headers.remove("X-User-ID");
//                                                     headers.set(
//                                                             "X-User-ID",
//                                                             userId
//                                                     );
//                                                 })
//                                                 .build();

//                                 return chain.filter(
//                                         exchange.mutate()
//                                                 .request(mutatedRequest)
//                                                 .build()
//                                 );
//                             }));
//                 })
//                 .switchIfEmpty(
//                         Mono.defer(() -> chain.filter(exchange))
//                 );
//     }
// }



package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .hasElement()
                .flatMap(hasJwt -> {
                    if (!hasJwt) {
                        return chain.filter(exchange);
                    }

                    return ReactiveSecurityContextHolder.getContext()
                            .map(SecurityContext::getAuthentication)
                            .cast(JwtAuthenticationToken.class)
                            .flatMap(authentication ->
                                    processAuthenticatedUser(
                                            exchange,
                                            chain,
                                            authentication
                                    )
                            );
                });
    }

    private Mono<Void> processAuthenticatedUser(
            ServerWebExchange exchange,
            WebFilterChain chain,
            JwtAuthenticationToken authentication) {

        var jwt = authentication.getToken();

        String userId = jwt.getSubject();

        RegisterRequest request = new RegisterRequest();
        request.setKeycloakId(userId);
        request.setEmail(jwt.getClaimAsString("email"));
        request.setFirstName(jwt.getClaimAsString("given_name"));
        request.setLastName(jwt.getClaimAsString("family_name"));
        request.setPassword("dummy@123123");

        return userService.validateUser(userId)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.debug(
                                "User already synchronized: {}",
                                userId
                        );
                        return Mono.empty();
                    }

                    log.info(
                            "Synchronizing authenticated user: {}",
                            userId
                    );

                    return userService.registerUser(request).then();
                })
                .then(Mono.defer(() -> {

                    ServerHttpRequest mutatedRequest =
                            exchange.getRequest()
                                    .mutate()
                                    .headers(headers -> {
                                        headers.remove("X-User-ID");
                                        headers.set("X-User-ID", userId);
                                    })
                                    .build();

                    ServerWebExchange mutatedExchange =
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build();

                    return chain.filter(mutatedExchange);
                }));
    }
}
