// package com.fitness.userservice.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fitness.userservice.dto.RegisterRequest;
// import com.fitness.userservice.dto.UserResponse;
// import com.fitness.userservice.service.UserService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.time.LocalDateTime;

// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(UserController.class)
// class UserControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private UserService userService;

//     private UserResponse createUserResponse() {
//         UserResponse response = new UserResponse();
//         response.setId("user-123");
//         response.setKeycloakId("keycloak-123");
//         response.setEmail("test@example.com");
//         response.setFirstName("John");
//         response.setLastName("Doe");
//         response.setCreatedAt(LocalDateTime.now());
//         response.setUpdatedAt(LocalDateTime.now());
//         return response;
//     }

//     @Test
//     void registerShouldReturnCreatedUser() throws Exception {
//         RegisterRequest request = new RegisterRequest();
//         request.setEmail("test@example.com");
//         request.setPassword("password123");
//         request.setKeycloakId("keycloak-123");
//         request.setFirstName("John");
//         request.setLastName("Doe");

//         when(userService.register(any(RegisterRequest.class)))
//                 .thenReturn(createUserResponse());

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(request)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value("user-123"))
//                 .andExpect(jsonPath("$.email").value("test@example.com"))
//                 .andExpect(jsonPath("$.firstName").value("John"))
//                 .andExpect(jsonPath("$.password").doesNotExist());

//         verify(userService).register(any(RegisterRequest.class));
//     }

//     @Test
//     void registerShouldRejectInvalidEmail() throws Exception {
//         String request = """
//                 {
//                     "email": "invalid-email",
//                     "password": "password123"
//                 }
//                 """;

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(request))
//                 .andExpect(status().isBadRequest());

//         verify(userService, never()).register(any());
//     }

//     @Test
//     void registerShouldRejectBlankEmail() throws Exception {
//         String request = """
//                 {
//                     "email": "",
//                     "password": "password123"
//                 }
//                 """;

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(request))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void registerShouldRejectShortPassword() throws Exception {
//         String request = """
//                 {
//                     "email": "test@example.com",
//                     "password": "123"
//                 }
//                 """;

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(request))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void registerShouldRejectMissingPassword() throws Exception {
//         String request = """
//                 {
//                     "email": "test@example.com"
//                 }
//                 """;

//         mockMvc.perform(post("/api/users/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(request))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void getUserProfileShouldReturnUser() throws Exception {
//         when(userService.getUserProfile("user-123"))
//                 .thenReturn(createUserResponse());

//         mockMvc.perform(get("/api/users/user-123"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value("user-123"))
//                 .andExpect(jsonPath("$.email").value("test@example.com"))
//                 .andExpect(jsonPath("$.password").doesNotExist());

//         verify(userService).getUserProfile("user-123");
//     }

//     @Test
//     void validateUserShouldReturnTrue() throws Exception {
//         when(userService.existByUserId("keycloak-123"))
//                 .thenReturn(true);

//         mockMvc.perform(get("/api/users/keycloak-123/validate"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("true"));
//     }

//     @Test
//     void validateUserShouldReturnFalse() throws Exception {
//         when(userService.existByUserId("unknown-user"))
//                 .thenReturn(false);

//         mockMvc.perform(get("/api/users/unknown-user/validate"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("false"));
//     }
// }



package com.fitness.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId("user-123");
        response.setKeycloakId("keycloak-123");
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    @Test
    void registerShouldReturnCreatedUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setKeycloakId("keycloak-123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(createUserResponse());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void registerShouldRejectInvalidEmail() throws Exception {
        String request = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void registerShouldRejectBlankEmail() throws Exception {
        String request = """
                {
                    "email": "",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerShouldRejectShortPassword() throws Exception {
        String request = """
                {
                    "email": "test@example.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerShouldRejectMissingPassword() throws Exception {
        String request = """
                {
                    "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserProfileShouldReturnUserForOwner() throws Exception {
        when(userService.getUserProfile("user-123", "keycloak-123"))
                .thenReturn(createUserResponse());

        mockMvc.perform(get("/api/users/user-123")
                        .header("X-User-ID", "keycloak-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).getUserProfile("user-123", "keycloak-123");
    }

    @Test
    void getUserProfileShouldRejectUnauthorizedUser() throws Exception {
        when(userService.getUserProfile("user-123", "another-user"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access another user's profile"
                ));

        mockMvc.perform(get("/api/users/user-123")
                        .header("X-User-ID", "another-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserProfileShouldRequireAuthenticatedUserHeader() throws Exception {
        mockMvc.perform(get("/api/users/user-123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void validateUserShouldReturnTrue() throws Exception {
        when(userService.existByUserId("keycloak-123"))
                .thenReturn(true);

        mockMvc.perform(get("/api/users/keycloak-123/validate"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validateUserShouldReturnFalse() throws Exception {
        when(userService.existByUserId("unknown-user"))
                .thenReturn(false);

        mockMvc.perform(get("/api/users/unknown-user/validate"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
