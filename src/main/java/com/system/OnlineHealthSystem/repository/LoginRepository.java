package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Integer> {

    Optional<Login> findByUserName(String userName);

    boolean existsByUserName(String userName);
}
