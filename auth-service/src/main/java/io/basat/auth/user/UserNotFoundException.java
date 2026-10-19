package io.basat.auth.user;

import io.basat.auth.error.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}