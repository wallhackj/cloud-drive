package com.wallhack.clouddrive.authentication.service;

import com.wallhack.clouddrive.authentication.entity.UserDetail;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DetailService implements UserDetailsService {
    private final UsersService usersService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.usersService
                .userExists(username)
                .map(UserDetail::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
