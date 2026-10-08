package io.basat.auth.authentication;

import io.basat.auth.tenant.Tenant;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.user.Role;
import io.basat.auth.user.User;
import io.basat.auth.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

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

        return new RegisterResponse(
                tenant.getId(),
                admin.getId(),
                admin.getEmail(),
                admin.getRole().name()
        );


    }


}
