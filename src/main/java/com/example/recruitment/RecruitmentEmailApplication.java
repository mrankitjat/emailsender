package com.example.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Main entry point for Recruitment Email Service Application.
 */
@SpringBootApplication
@EnableRetry
public class RecruitmentEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentEmailApplication.class, args);
    }
}
