package com.example.admin.dto.activity;

import lombok.Data;

@Data
public class MyActivityStateDto {
    /** NONE / SIGNED / CANCELED / CHECKED_IN */
    private String signupStatus;

    private boolean favorited;
}

