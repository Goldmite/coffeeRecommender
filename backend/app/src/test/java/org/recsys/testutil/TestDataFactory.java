package org.recsys.testutil;

import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.User;
import org.recsys.model.UserRole;

public class TestDataFactory {

    public static String testName = "John Pork";
    public static String testEmail = "john.pork@test.com";
    public static String testPassword = "AS()*J#M@IHAKa40k";

    public static User createUser() {
        return createUser(testName, testEmail, testPassword);
    }

    public static User createUser(String email, String pw_hash) {
        return createUser(testName, email, pw_hash);
    }

    public static User createUser(String name, String email, String pw_hash) {
        return new User(null, name, email, pw_hash, UserRole.USER);
    }

    public static UserSignupRequest validSignup() {
        return new UserSignupRequest(testName, testEmail, testPassword);
    }

    // signup with empty name, bad email format and short password
    public static UserSignupRequest invalidSignup() {
        return new UserSignupRequest("", "bad-email", "1234");
    }

    public static UserLoginRequest validLogin() {
        return new UserLoginRequest(testEmail, testPassword);
    }

    // login with empty password, bad email format
    public static UserLoginRequest invalidLogin() {
        return new UserLoginRequest("bad-email", "");
    }
}
