package io.basat.auth.authentication;

import io.basat.auth.tenant.Tenant;
import io.basat.auth.tenant.TenantRepository;
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

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request){
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

        log.info("Registered tenant {} with admin user {} ", tenant.getId(), admin.getId());

        return new RegisterResponse(
                tenant.getId(),
                admin.getId(),
                admin.getEmail(),
                admin.getRole().name()
        );


    }


}
