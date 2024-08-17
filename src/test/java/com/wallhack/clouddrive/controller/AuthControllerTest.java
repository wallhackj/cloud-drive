package com.wallhack.clouddrive.controller;

import com.redis.testcontainers.RedisContainer;
import com.wallhack.clouddrive.authentication.config.SecurityConfig;
import com.wallhack.clouddrive.authentication.dto.AuthDTO;
import com.wallhack.clouddrive.authentication.exception.UserAlreadyExistException;
import com.wallhack.clouddrive.authentication.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers @SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    AuthService authService;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName
            .parse("postgres:latest"));
    @Container
    private static final RedisContainer redisContainer = new RedisContainer(DockerImageName
            .parse("redis:alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @BeforeEach
    @Test
    void givenRedisContainerConfiguredWithDynamicProperties_whenCheckingRunningStatus_thenStatusIsRunning() {
        assertTrue(redisContainer.isRunning());
    }

    @Test
    @SneakyThrows
    void testNewRegistrationPage() {
        mvc.perform(get("/sign-up"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attributeExists("authDTO"));
    }

    @Test
    @SneakyThrows
    void testNewLoginPage() {
        mvc.perform(get("/sign-in"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("authDTO"));
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationSuccessfully() {
        AuthDTO user = new AuthDTO("mike", "123");

        when(authService.register(user)).thenReturn("login");

        mvc.perform(post("/sign-up")
                        .param("username", "mike") // Simulate form field for username
                        .param("password", "123")) // Simulate form field for password
                .andExpect(status().is3xxRedirection()) // Expecting a redirection
                .andExpect(redirectedUrl("/sign-in")); // Expect redirection to /sign-in

        verify(authService).register(user);
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationSameUsernameError() {
        AuthDTO user = new AuthDTO("mike", "123");

        // Mock the case where user already exists
        when(authService.register(user)).thenThrow(new UserAlreadyExistException("User already exists"));

        // Perform the POST request
        mvc.perform(post("/sign-up")
                        .param("username", "mike")
                        .param("password", "123"))
                .andExpect(status().isOk()) // Expecting a successful response as the page is rendered
                .andExpect(view().name("auth/registration")) // Check that the view is the registration page
                .andExpect(model().attribute("error", "Eroare neașteptată: User already exists")); // Check for the error message

        // Verify that the register method was called with the correct argument
        verify(authService).register(user);
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationValidationError() {
        AuthDTO user = new AuthDTO();

        when(authService.register(user)).thenThrow(new IllegalArgumentException("Username and password must not be empty"));

        mvc.perform(post("/sign-up")
                        .flashAttr("authDTO", user)) // Note the corrected attribute name
                .andExpect(status().isOk()) // Expecting a 200 OK as the page is rendered with errors
                .andExpect(view().name("auth/registration")) // Check that the view is the registration page
                .andExpect(model().attribute("error", "Eroare de validare")); // Check for the validation error message
    }

    @Test
    @SneakyThrows
    public void testRegistrationUsernameLess3SymbolsFails() {
        AuthDTO user = new AuthDTO("me", "mimi");

        when(authService.register(user)).thenReturn("login");

        mvc.perform(post("/sign-up")
                        .param("username", "me")
                        .param("password", "mimi"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attribute("error", "Eroare de validare"));
    }

    @Test
    @SneakyThrows
    void testUserLogin() {
        AuthDTO user = new AuthDTO("mike", "123");

        when(authService.login(eq(user), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn("file");

        mvc.perform(post("/sign-in").flashAttr("authDTO", user)
                .param("username", "mike")
                .param("password", "123"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("file"));
    }

    @Test
    @SneakyThrows
    void testUserLoginWithEmptyCredentials() {
        AuthDTO user = new AuthDTO();

        mvc.perform(post("/sign-in").flashAttr("authDTO", user))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("authDTO", "username", "password"));
    }


    @Test
    @SneakyThrows
    void testUserLoginWithBadCredentials() {
        AuthDTO user = new AuthDTO("mik", "123");

        when(authService.login(eq(user), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mvc.perform(post("/sign-in").flashAttr("authDTO", user)
                        .param("username", "mik")
                        .param("password", "123"))
                .andExpect(status().isOk())  // is2xxSuccessful() poate fi înlocuit cu isOk()
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "Bad Credentials"));
    }

}