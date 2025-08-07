package com.fastcampus.book_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BookBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookBotApplication.class, args);
    }

}
