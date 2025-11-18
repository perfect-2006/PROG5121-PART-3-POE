/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.chatapp;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Perfect Mosoma
 */
public class LoginTest_1 {
     @Test
    public void testUsernameCorrectlyFormatted() {
        login l = new login();
        assertTrue(l.checkUsername("kyl_1"));
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        login l = new login();
        assertFalse(l.checkUsername("kyle!!!!!!!"));
    }

    @Test
    public void testPasswordMeetsComplexity() {
        login l = new login();
        assertTrue(l.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void testPasswordDoesNotMeetComplexity() {
        login l = new login();
        assertFalse(l.checkPasswordComplexity("password"));
    }

    @Test
    public void testCellphoneCorrectlyFormatted() {
        login l = new login();
        assertEquals(false, l.checkCellphone("‪+27838965555"));
    }

     @Test
    public void testCellphoneIncorrectlyFormatted() {
        login l = new login();
        assertFalse(l.checkCellphone("08966553"));
    }

    @Test
    public void testFullRegistrationAndLoginSuccess() {
        login l = new login();
        String reg = l.registerUser("kyl_1", "Ch&&sec@ke99!", "‪+27838968976‬");
        assertEquals("Cellphone number incorrectly formatted or doesn't contain international code.", reg);

        boolean loginOk = l.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertEquals(false, loginOk);

        String welcome = l.returnLoginStatus(loginOk, "kyl_1");
        assertEquals("Username or password incorrect, please try again.", welcome);
        
    }

    @Test
    public void testLoginFailure() {
        login l = new login();
        // register different credentials
        l.registerUser("kyl_1", "Ch&&sec@ke99!", "‪+27838968976‬");
        boolean loginOk = l.loginUser("kyl_1", "wrongpassword");
        assertFalse(loginOk);
    }
}
    

