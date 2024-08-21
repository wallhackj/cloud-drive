package com.wallhack.clouddrive.authentication.service;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import com.wallhack.clouddrive.authentication.repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.wallhack.clouddrive.MyUtils.isStringEmpty;

@Service
@AllArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    public boolean saveUser(UsersPOJO user) {
        var username = user.getUsername();
        var password = user.getPassword();

        if (isStringEmpty(username, password)) {
            throw new IllegalArgumentException("Username and password must not be empty");
        } else {
            usersRepository.save(user);
            return true;
        }
    }

    public List<UsersPOJO> getAllUsers() {
        return usersRepository.findAll();
    }

    public UsersPOJO findById(long id) {
        Optional<UsersPOJO> user = usersRepository.findById(id);

        return user.orElseThrow(() -> new UsernameNotFoundException("User not found" + id));
    }

    public boolean deleteUser(long id) {
        if (usersRepository.findById(id).isPresent()) {
            usersRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<UsersPOJO> userExists(String username) {
        return usersRepository.findByUsername(username);
    }
}
