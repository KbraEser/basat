package io.basat.auth.authentication;

import io.basat.auth.authentication.dto.*;
import io.basat.auth.tenant.Tenant;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.token.RefreshTokenService;
import io.basat.auth.token.RotationResult;
import io.basat.auth.token.TokenService;
import io.basat.auth.user.Role;
import io.basat.auth.user.User;
import io.basat.auth.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TenantRepository  tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request){
        // Locale.ROOT avoids the Turkish dotless-i problem: "I".toLowerCase() would give "ı"
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmail(email)){
            log.warn("Registration rejected: email is already in use");
            throw new EmailAlreadyUsedException(email);
        }

        Tenant tenant = tenantRepository.save(new Tenant(request.companyName().trim()));

        User admin =userRepository.save(new User(
                tenant,
                email,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                Role.ADMIN
        ));

        // Log IDs only; emails are personal data (KVKK/GDPR)
        log.info("Registered tenant {} with admin user {}", tenant.getId(), admin.getId());

        return new RegisterResponse(
                tenant.getId(),
                admin.getId(),
                admin.getEmail(),
                admin.getRole().name()
        );


    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Login rejected: invalid credentials");
                    return new InvalidCredentialsException();
                });

        log.info("User {} logged in", user.getId());

        LoginResponse response = new LoginResponse(user.getId(), user.getTenant().getId(), user.getRole().name());
        return new LoginResult(tokenService.createAccessToken(user),refreshTokenService.create(user), response);
    }

    @Override
    @Transactional
    public LoginResult refresh(String refreshToken) {
        RotationResult rotation = refreshTokenService.rotate(refreshToken);
        User user = rotation.user();

        log.info("User {} refreshed tokens", user.getId());

        LoginResponse response = new LoginResponse(user.getId(), user.getTenant().getId(), user.getRole().name());

        return new LoginResult(tokenService.createAccessToken(user),rotation.refreshToken(),response);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
    }


}
