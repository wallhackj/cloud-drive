package com.wallhack.clouddrive.controller;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import com.wallhack.clouddrive.authentication.repository.exception.UserAlreadyExistException;
import com.wallhack.clouddrive.authentication.UsersService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    @MockBean
    UsersRepository usersRepository;

    @Autowired
    @MockBean
    UsersService usersService;
    
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:10.5"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @SneakyThrows
    void testNewRegistrationPage() {
        mvc.perform(get("http://localhost:8081/sign-up"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("registration"));
    }

    @Test
    @SneakyThrows
    void testNewLoginPage() {
        mvc.perform(get("http://localhost:8081/sign-in"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("login"));
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationSuccessfully() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        when(usersService.saveUser(user)).thenReturn(true);

        mvc.perform(post("http://localhost:8081/sign-up")
                .flashAttr("registerRequest", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        when(usersRepository.findByUsername("mike")).thenReturn(Optional.of(user));

        UsersPOJO currentUser = usersRepository.findByUsername("mike").orElse(null);
        assertNotNull(currentUser);
        assertEquals(currentUser, user);
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationSameUsernameError() {
        UsersPOJO validUser = new UsersPOJO();
        validUser.setUsername("mike");
        validUser.setPassword("123");

        UsersPOJO duplicateUser = new UsersPOJO();
        duplicateUser.setUsername("mike");
        duplicateUser.setPassword("123");

        when(usersService.saveUser(validUser)).thenReturn(true);
        when(usersService.saveUser(duplicateUser)).thenThrow(new UserAlreadyExistException("User already exists"));

        mvc.perform(post("/sign-up")
                        .flashAttr("registerRequest", validUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        mvc.perform(post("/sign-up")
                        .flashAttr("registerRequest", duplicateUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-up?error=1"));
    }

    @Test
    @SneakyThrows
    void testNewUserRegistrationValidationError() {
        UsersPOJO invalidUser = new UsersPOJO();

        when(usersService.saveUser(invalidUser)).thenThrow(new IllegalArgumentException("Username and password must not be empty"));

        mvc.perform(post("http://localhost:8081/sign-up")
                        .flashAttr("registerRequest", invalidUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-up?error=2"));
    }

    @Test
    @SneakyThrows
    void testUserLogin() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        when(usersService.saveUser(user)).thenReturn(true);

        mvc.perform(post("/sign-up").flashAttr("registerRequest", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        when(usersRepository.findByUsername("mike")).thenReturn(Optional.of(user));

        mvc.perform(post("/sign-in").flashAttr("loginRequest", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @SneakyThrows
    void testUserLoginValidationError() {
        UsersPOJO user = new UsersPOJO();

        when(usersService.loginUser(user.getUsername(), user.getPassword())).thenThrow(new IllegalArgumentException("Username and password must not be empty"));

        mvc.perform(post("/sign-in")
                        .flashAttr("loginRequest", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in?error=2"));
    }

    @Test
    @SneakyThrows
    void testUserLoginUsernameNotFoundError() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        when(usersService.loginUser(user.getUsername(), user.getPassword())).thenThrow(new UsernameNotFoundException("Username not found"));

        mvc.perform(post("/sign-in")
                        .flashAttr("loginRequest", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in?error=1"));
    }
}