package io.basat.auth.authentication;

import io.basat.auth.authentication.dto.RegisterRequest;
import io.basat.auth.authentication.dto.RegisterResponse;
import io.basat.auth.tenant.Tenant;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.user.Role;
import io.basat.auth.user.User;
import io.basat.auth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_createsTenantAndAdmin_withNormalizedEmailAndHashedPassword() {
        // given
        RegisterRequest request = new RegisterRequest(
                " Deniz Otelcilik ", "Ayşe Yılmaz", " Ayse@Deniz.com ", "GucluSifre123");
        when(userRepository.existsByEmail("ayse@deniz.com")).thenReturn(false);
        when(passwordEncoder.encode("GucluSifre123")).thenReturn("hashed-password");
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        RegisterResponse response = authService.register(request);

        // then
        assertThat(response.email()).isEqualTo("ayse@deniz.com");
        assertThat(response.role()).isEqualTo("ADMIN");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedUser.getTenant().getName()).isEqualTo("Deniz Otelcilik");
    }

    @Test
    void register_throwsAndSavesNothing_whenEmailIsAlreadyUsed() {
        // given
        RegisterRequest request = new RegisterRequest(
                "Deniz Otelcilik", "Ayşe Yılmaz", "ayse@deniz.com", "GucluSifre123");
        when(userRepository.existsByEmail("ayse@deniz.com")).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}