package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUserEmail(String userEmail);
    boolean existsByUserNickname(String userNickname);

    User findByUserEmail(String userEmail);
}
