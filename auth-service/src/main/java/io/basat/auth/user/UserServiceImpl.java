package io.basat.auth.user;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return new UserProfileResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getTenant().getName(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()

        );
    }
}
