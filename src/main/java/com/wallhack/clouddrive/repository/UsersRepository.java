package com.wallhack.clouddrive.repository;

import com.wallhack.clouddrive.entity.UsersPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersPOJO, Long> {
    Optional<UsersPOJO> findByUsername(String username);
}
