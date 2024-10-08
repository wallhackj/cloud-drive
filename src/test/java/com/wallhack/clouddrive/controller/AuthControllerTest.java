package com.wallhack.clouddrive.controller;

import com.redis.testcontainers.RedisContainer;
import com.wallhack.clouddrive.authentication.config.SecurityConfig;
import com.wallhack.clouddrive.authentication.dto.AuthDTO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import com.wallhack.clouddrive.authentication.service.AuthService;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AuthControllerTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName
            .parse("postgres:latest"));
    @Container
    private static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName
            .parse("redis:alpine"))
            .withExposedPorts(6379);
    @Autowired
    private AuthService authService;
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void configureProperties(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);

        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @BeforeEach
    @Test
    void givenContainerConfiguredWithDynamicProperties_whenCheckingRunningStatus_thenStatusIsRunning() {
        assertTrue(REDIS_CONTAINER.isRunning());
        assertTrue(POSTGRE_SQL_CONTAINER.isRunning());
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
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

        mvc.perform(post("/sign-up")
                        .param("username", "mike")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        Assertions.assertNotNull(userRepository.findByUsername("mike"));
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationSameUsernameError() {
        AuthDTO user = new AuthDTO("mike", "123");

        authService.register(user);
        Assertions.assertNotNull(userRepository.findByUsername("mike"));

        mvc.perform(post("/sign-up")
                        .param("username", user.getUsername())
                        .param("password", user.getPassword()))
                .andExpect(status().isOk()) // Expecting a successful response as the page is rendered
                .andExpect(view().name("auth/registration")) // Check that the view is the registration page
                .andExpect(model().attribute("error", "Some error: mike exists")); // Check for the error message
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationValidationError() {
        AuthDTO user = new AuthDTO();

        mvc.perform(post("/sign-up")
                        .flashAttr("authDTO", user))
                .andExpect(status().isOk()) // Expecting a 200 OK as the page is rendered with errors
                .andExpect(view().name("auth/registration")) // Check that the view is the registration page
                .andExpect(model().attribute("error", "Username or password is wrong?")); // Check for the validation error message
    }

    @Test
    @SneakyThrows
    public void testRegistrationUsernameLess3SymbolsFails() {
        AuthDTO user = new AuthDTO("me", "mimi");

        mvc.perform(post("/sign-up")
                        .flashAttr("authDTO", user))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registration"))
                .andExpect(model().attribute("error", "Username or password is wrong?"));
    }

    @Test
    @SneakyThrows
    void testUserLogin() {
        AuthDTO user = new AuthDTO("mike", "123");

        mvc.perform(post("/sign-in").flashAttr("authDTO", user))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("auth/login"));
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
    @Transactional
    void testUserLoginWithBadCredentials() {
        AuthDTO user = new AuthDTO("mike", "123");

        // Register the user first
        authService.register(user);

        mvc.perform(post("/sign-in")
                        .param("username", "mike")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("error", "Bad Credentials"));
    }
}