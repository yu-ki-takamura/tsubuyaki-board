package com.example.tsubuyaki.web.dto;

import java.time.LocalDateTime;

public record PostView(String author, String body, LocalDateTime createdAt) {
}
