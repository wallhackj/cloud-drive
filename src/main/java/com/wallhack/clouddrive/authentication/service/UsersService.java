package com.wallhack.clouddrive.authentication.service;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.wallhack.clouddrive.file_and_folder_manager.rest.MyUtils.isStringEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    public boolean saveUser(UsersPOJO user) {
        var username = user.getUsername();
        var password = user.getPassword();

        if (isStringEmpty(username, password)) {
            log.info("Username and password must not be empty");
            throw new IllegalArgumentException("Username and password must not be empty");
        } else {
            usersRepository.save(user);
            return true;
        }
    }

    public List<UsersPOJO> getAllUsers() {
        return usersRepository.findAll();
    }

    public Optional<UsersPOJO> userExists(String username) {
        return usersRepository.findByUsername(username);
    }

    public boolean deleteUser(long id) {
        if (usersRepository.findById(id).isPresent()) {
            usersRepository.deleteById(id);
            return true;
        }
        log.info("User with id {} not found", id);
        return false;
    }
}
