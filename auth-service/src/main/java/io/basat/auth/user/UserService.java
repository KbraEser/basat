package io.basat.auth.user;

import java.util.UUID;

public interface UserService {
    UserProfileResponse getProfile(UUID userId);
}
