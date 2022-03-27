package com.javeriana.user;

import com.javeriana.shared.exceptions.BusinessRuleException;
import com.javeriana.user.models.User;

import java.util.Optional;

public class UserService {
    public boolean signIn(String username, String password) {
        User foundUser = Optional.of(getUserByUsername(username))
                .orElseThrow(() -> new BusinessRuleException("User doesn't exist"));

        return password.equalsIgnoreCase(foundUser.getPassword());
    }

    public User getUserByUsername(String username) {
        return User.builder()
                .username("Test")
                .password("")
                .build();
    }
}
