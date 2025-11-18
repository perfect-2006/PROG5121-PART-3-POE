/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.chatapp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Perfect Mosoma
 */
public class login {
    // Single-user storage for the POE
    private user registeredUser = null;

    // Username rule: contains underscore and is no more than five characters long.
    public boolean checkUsername(String username) {
        if (username == null) return false;
        // contains underscore and length <= 5
        return username.contains("_") && username.length() <= 5;
    }

    // Password complexity rules:
    // at least 8 chars, contains a capital, contains a digit, contains a special char
    public boolean checkPasswordComplexity(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    
    public boolean checkCellphone(String cellphone) {
        if (cellphone == null) return false;
        // SA-specific pattern: +27 followed by 9 OR 10 digits
        String saPattern = "^\\+27\\d{9}$";
        return cellphone.matches(saPattern);
    }

/**
     * registerUser: returns messages depending on validation of username/password/cellphone.
     * If all pass, saves the single user and returns a success message.
     */
    public String registerUser(String username, String password, String cellphone) {
        if (!checkUsername(username)) {
            return "Username is not correctly formatted. Please ensure that username has a underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted. Must be eight characters long, contain a capital letter, number and special character.";
        }
        if (!checkCellphone(cellphone)) {
            return "Cellphone number incorrectly formatted or doesn't contain international code.";
        }
        // all good -> register
        this.registeredUser = new user(username, password, cellphone);
        return "Registration successful.";
    }
 /**
     * loginUser: check entered credentials against stored registeredUser (single-user).
     */
    public boolean loginUser(String username, String password) {
        if (this.registeredUser == null) return false;
        return this.registeredUser.getUsername().equals(username)
                && this.registeredUser.getPassword().equals(password);
    }

    /**
     * returnLoginStatus: returns message for successful / failed login.
     * If login is successful, we extract first and last name from username split by underscore.
     */
    public String returnLoginStatus(boolean loginResult, String username) {
        if (!loginResult) {
            return "Username or password incorrect, please try again.";
        }
        // successful: extract first and last name from username parts
        String firstName = "";
        String lastName = "";
        if (username != null && username.contains("_")) {
            String[] parts = username.split("_", 2);
            firstName = parts.length > 0 ? parts[0] : "";
            lastName = parts.length > 1 ? parts[1] : "";
        } else {
            firstName = username;
            lastName = "";
        }
     return String.format("Welcome %s ,%s it is great to see you again.", firstName, lastName);
    }

    // For tests or GUI to access the stored user (nullable)
    public user getRegisteredUser() {
        return registeredUser;
    }
}
