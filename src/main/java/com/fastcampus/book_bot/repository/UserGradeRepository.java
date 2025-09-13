package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.user.UserGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGradeRepository extends JpaRepository<UserGrade, Integer> {
    Optional<UserGrade> findByGradeName(String gradeName);
}
