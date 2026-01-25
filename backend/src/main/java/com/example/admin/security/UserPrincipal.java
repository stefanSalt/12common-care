package com.example.admin.security;

import java.util.List;

public record UserPrincipal(Long userId, String username, List<String> roles) {}

