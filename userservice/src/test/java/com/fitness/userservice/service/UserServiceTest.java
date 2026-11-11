// package com.fitness.userservice.service;

// import com.fitness.userservice.dto.RegisterRequest;
// import com.fitness.userservice.dto.UserResponse;
// import com.fitness.userservice.model.User;
// import com.fitness.userservice.repository.UserRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDateTime;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class UserServiceTest {

//     @Mock
//     private UserRepository repository;

//     @InjectMocks
//     private UserService userService;

//     private RegisterRequest request;
//     private User user;

//     @BeforeEach
//     void setUp() {
//         request = new RegisterRequest();
//         request.setEmail("test@example.com");
//         request.setPassword("password123");
//         request.setKeycloakId("keycloak-123");
//         request.setFirstName("John");
//         request.setLastName("Doe");

//         user = new User();
//         user.setId("user-123");
//         user.setEmail("test@example.com");
//         user.setPassword("password123");
//         user.setKeycloakId("keycloak-123");
//         user.setFirstName("John");
//         user.setLastName("Doe");
//         user.setCreatedAt(LocalDateTime.now());
//         user.setUpdatedAt(LocalDateTime.now());
//     }

//     @Test
//     void registerShouldCreateNewUser() {
//         when(repository.existsByEmail(request.getEmail()))
//                 .thenReturn(false);

//         when(repository.save(any(User.class)))
//                 .thenReturn(user);

//         UserResponse response = userService.register(request);

//         assertNotNull(response);
//         assertEquals("user-123", response.getId());
//         assertEquals("test@example.com", response.getEmail());
//         assertEquals("keycloak-123", response.getKeycloakId());
//         assertEquals("John", response.getFirstName());
//         assertEquals("Doe", response.getLastName());

//         verify(repository).save(any(User.class));
//     }

//     @Test
//     void registerShouldReturnExistingUserWhenEmailAlreadyExists() {
//         when(repository.existsByEmail(request.getEmail()))
//                 .thenReturn(true);

//         when(repository.findByEmail(request.getEmail()))
//                 .thenReturn(user);

//         UserResponse response = userService.register(request);

//         assertNotNull(response);
//         assertEquals("user-123", response.getId());
//         assertEquals("test@example.com", response.getEmail());
//         assertEquals("keycloak-123", response.getKeycloakId());

//         verify(repository).findByEmail(request.getEmail());
//         verify(repository, never()).save(any(User.class));
//     }

//     @Test
//     void registerShouldCopyAllUserDetails() {
//         when(repository.existsByEmail(request.getEmail()))
//                 .thenReturn(false);

//         when(repository.save(any(User.class)))
//                 .thenAnswer(invocation -> {
//                     User savedUser = invocation.getArgument(0);
//                     savedUser.setId("generated-user-id");
//                     return savedUser;
//                 });

//         UserResponse response = userService.register(request);

//         assertEquals("generated-user-id", response.getId());
//         assertEquals(request.getEmail(), response.getEmail());
//         assertEquals(request.getKeycloakId(), response.getKeycloakId());
//         assertEquals(request.getFirstName(), response.getFirstName());
//         assertEquals(request.getLastName(), response.getLastName());
//     }

//     @Test
//     void getUserProfileShouldReturnUserDetails() {
//         when(repository.findById("user-123"))
//                 .thenReturn(Optional.of(user));

//         UserResponse response = userService.getUserProfile("user-123");

//         assertNotNull(response);
//         assertEquals("user-123", response.getId());
//         assertEquals("test@example.com", response.getEmail());
//         assertEquals("John", response.getFirstName());
//         assertEquals("Doe", response.getLastName());

//         verify(repository).findById("user-123");
//     }

//     @Test
//     void getUserProfileShouldThrowExceptionWhenUserDoesNotExist() {
//         when(repository.findById("missing-user"))
//                 .thenReturn(Optional.empty());

//         RuntimeException exception = assertThrows(
//                 RuntimeException.class,
//                 () -> userService.getUserProfile("missing-user")
//         );

//         assertEquals("User Not Found", exception.getMessage());
//     }

//     @Test
//     void existByUserIdShouldReturnTrueWhenUserExists() {
//         when(repository.existsByKeycloakId("keycloak-123"))
//                 .thenReturn(true);

//         Boolean result = userService.existByUserId("keycloak-123");

//         assertTrue(result);
//         verify(repository).existsByKeycloakId("keycloak-123");
//     }

//     @Test
//     void existByUserIdShouldReturnFalseWhenUserDoesNotExist() {
//         when(repository.existsByKeycloakId("unknown-user"))
//                 .thenReturn(false);

//         Boolean result = userService.existByUserId("unknown-user");

//         assertFalse(result);
//         verify(repository).existsByKeycloakId("unknown-user");
//     }
// }



package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setKeycloakId("keycloak-123");
        request.setFirstName("John");
        request.setLastName("Doe");

        user = new User();
        user.setId("user-123");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setKeycloakId("keycloak-123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerShouldCreateNewUserWithHashedPassword() {
        when(repository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId("user-123");
                    return savedUser;
                });

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("keycloak-123", response.getKeycloakId());

        verify(repository).save(argThat(savedUser ->
                savedUser.getPassword() != null
                        && !savedUser.getPassword().equals("password123")
                        && new BCryptPasswordEncoder().matches(
                                "password123",
                                savedUser.getPassword()
                        )
        ));

        //assertNull(response.getPassword());
    }

    @Test
    void registerShouldReturnExistingUserForSameKeycloakAccount() {
        when(repository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(user);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("keycloak-123", response.getKeycloakId());
        //assertNull(response.getPassword());

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void registerShouldRejectEmailLinkedToAnotherKeycloakAccount() {
        when(repository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        user.setKeycloakId("another-keycloak-user");

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(user);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void registerShouldCopyAllUserDetails() {
        when(repository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId("generated-user-id");
                    return savedUser;
                });

        UserResponse response = userService.register(request);

        assertEquals("generated-user-id", response.getId());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getKeycloakId(), response.getKeycloakId());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
    }

    @Test
    void getUserProfileShouldReturnUserDetailsForOwner() {
        when(repository.findById("user-123"))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUserProfile(
                "user-123",
                "keycloak-123"
        );

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("keycloak-123", response.getKeycloakId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        //assertNull(response.getPassword());

        verify(repository).findById("user-123");
    }

    @Test
    void getUserProfileShouldRejectAnotherUsersRequest() {
        when(repository.findById("user-123"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserProfile(
                        "user-123",
                        "another-keycloak-user"
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void getUserProfileShouldReturnNotFoundForMissingUser() {
        when(repository.findById("missing-user"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserProfile(
                        "missing-user",
                        "keycloak-123"
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void existByUserIdShouldReturnTrueWhenUserExists() {
        when(repository.existsByKeycloakId("keycloak-123"))
                .thenReturn(true);

        Boolean result = userService.existByUserId("keycloak-123");

        assertTrue(result);
        verify(repository).existsByKeycloakId("keycloak-123");
    }

    @Test
    void existByUserIdShouldReturnFalseWhenUserDoesNotExist() {
        when(repository.existsByKeycloakId("unknown-user"))
                .thenReturn(false);

        Boolean result = userService.existByUserId("unknown-user");

        assertFalse(result);
        verify(repository).existsByKeycloakId("unknown-user");
    }
}
