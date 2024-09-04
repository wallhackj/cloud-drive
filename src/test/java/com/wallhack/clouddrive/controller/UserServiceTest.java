package com.wallhack.clouddrive.controller;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import com.wallhack.clouddrive.authentication.service.UsersService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

@Testcontainers
@SpringBootTest
public class UserServiceTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName
            .parse("postgres:latest"));

    @Autowired
    UsersService usersService;

    @Autowired
    UsersRepository usersRepository;

    @DynamicPropertySource
    static void configureProperties(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
    }

    @Test
    void testSaveUserToDB() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        boolean isSaved = usersService.saveUser(user);
        Assertions.assertTrue(isSaved);

        Optional<UsersPOJO> retrievedUser = usersRepository.findByUsername("mike");
        Assertions.assertTrue(retrievedUser.isPresent());
        Assertions.assertEquals("mike", retrievedUser.get().getUsername());
        Assertions.assertEquals("123", retrievedUser.get().getPassword());
    }

    @Test
    void testSaveUserValidationError() {
        UsersPOJO user = new UsersPOJO();

        try {
            usersService.saveUser(user);
            Assertions.fail("Expected exception not thrown");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Username and password must not be empty", e.getMessage());
        }
    }

    @Test
    void getAllUsersFromRepository() {
        UsersPOJO user1 = new UsersPOJO();
        user1.setUsername("mike");
        user1.setPassword("123");

        UsersPOJO user2 = new UsersPOJO();
        user2.setUsername("mike2");
        user2.setPassword("456");

        usersService.saveUser(user1);
        usersService.saveUser(user2);

        List<UsersPOJO> users = usersService.getAllUsers();
        Assertions.assertNotNull(users);
        Assertions.assertEquals(2, users.size());
        Assertions.assertEquals("mike", users.get(0).getUsername());
        Assertions.assertEquals("mike2", users.get(1).getUsername());
    }

    @Test
    void deleteUserFromRepository() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        usersService.saveUser(user);
        Optional<UsersPOJO> foundUser = usersRepository.findByUsername("mike");
        Assertions.assertTrue(foundUser.isPresent());

        boolean isDeleted = usersService.deleteUser(foundUser.get().getId());
        Assertions.assertTrue(isDeleted);

        Optional<UsersPOJO> deletedUser = usersRepository.findByUsername("mike");
        Assertions.assertFalse(deletedUser.isPresent());
    }
}
