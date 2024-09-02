package com.wallhack.clouddrive.controller;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import com.wallhack.clouddrive.authentication.service.UsersService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@Testcontainers @SpringBootTest
public class UserServiceTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName
            .parse("postgres:latest"));

    @MockBean
    UsersService usersService;

    @MockBean
    UsersRepository usersRepository;

    @DynamicPropertySource
    static void configureProperties(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    }

    @Test
    void testSaveUserToDB() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        when(usersService.saveUser(user)).thenReturn(true);

        when(usersRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        UsersPOJO getUser = usersRepository.findByUsername(user.getUsername()).orElse(null);

        Assertions.assertNotNull(getUser);
        Assertions.assertEquals(user.getUsername(), getUser.getUsername());
        Assertions.assertEquals(user.getPassword(), getUser.getPassword());
    }

    @Test
    void testSaveUserValidationError() {
        UsersPOJO user = new UsersPOJO();

        when(usersService.saveUser(user))
                .thenThrow(new IllegalArgumentException("Username and password must not be empty"));
    }

    @Test
    void getAllUsersFromRepository() {
        UsersPOJO user = new UsersPOJO();
        user.setUsername("mike");
        user.setPassword("123");

        when(usersRepository.findByUsername("mike")).thenReturn(Optional.of(user));
        UsersPOJO user1 = usersRepository.findByUsername("mike").orElse(null);

        Assertions.assertNotNull(user1);
        Assertions.assertEquals(user1.getUsername(), "mike");
        Assertions.assertEquals(user1.getPassword(), "123");

        UsersPOJO user2 = new UsersPOJO();
        user2.setUsername("mike2");
        user2.setPassword("456");
        when(usersService.saveUser(user2)).thenReturn(true);

        List<UsersPOJO> users = List.of(user1, user2);
        when(usersService.getAllUsers()).thenReturn(users);

        List<UsersPOJO> retrievedUsers = usersService.getAllUsers();
        Assertions.assertNotNull(retrievedUsers);
        Assertions.assertEquals(retrievedUsers.size(), 2);
        Assertions.assertEquals(retrievedUsers.get(0).getUsername(), "mike");
        Assertions.assertEquals(retrievedUsers.get(1).getUsername(), "mike2");
    }

    @Test
    void deleteUserFromRepository() {
        UsersPOJO user = new UsersPOJO();
        user.setId(1L);
        user.setUsername("mike");
        user.setPassword("123");

        when(usersRepository.findByUsername("mike")).thenReturn(Optional.of(user));

        UsersPOJO foundUser = usersRepository.findByUsername("mike").orElse(null);

        Assertions.assertNotNull(foundUser);
        Assertions.assertEquals("mike", foundUser.getUsername());
        Assertions.assertEquals("123", foundUser.getPassword());

        when(usersService.deleteUser(foundUser.getId())).thenReturn(true);

        boolean isDeleted = usersService.deleteUser(foundUser.getId());

        Assertions.assertTrue(isDeleted);

        when(usersRepository.findByUsername("mike")).thenReturn(Optional.empty());

        UsersPOJO deletedUser = usersRepository.findByUsername("mike").orElse(null);
        Assertions.assertNull(deletedUser);
    }
}
