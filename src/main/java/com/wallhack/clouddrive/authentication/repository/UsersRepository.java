package com.wallhack.clouddrive.authentication.repository;

import com.wallhack.clouddrive.authentication.entity.UsersPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersPOJO, Long> {
    Optional<UsersPOJO> findByUsername(String username);
}
