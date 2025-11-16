package com.schoolmanagement.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "Test@123";
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("=== PASSWORD HASH GENERATOR ===");
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("BCrypt Hash: " + hashedPassword);
        System.out.println("");
        System.out.println("Copy this hash and use it in SQL UPDATE:");
        System.out.println("UPDATE users SET password = '" + hashedPassword + "' WHERE username = 'admin';");

        // Test the hash
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("");
        System.out.println("Hash verification: " + (matches ? "SUCCESS" : "FAILED"));

        // Also test with the standard hash
        String standardHash = "$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm";
        boolean standardMatches = encoder.matches(rawPassword, standardHash);
        System.out.println("Standard hash verification: " + (standardMatches ? "SUCCESS" : "FAILED"));
    }
}

