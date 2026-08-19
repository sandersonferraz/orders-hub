package com.ordershub.auth.api;

import com.ordershub.auth.domain.User;
import com.ordershub.auth.repository.UserRepository;
import com.ordershub.auth.security.JwtService;
import com.ordershub.auth.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder encoder;

    @Mock
    JwtService jwt;

    @Mock
    RefreshTokenService refresh;

    AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(users, encoder, jwt, refresh);
    }

    @Test
    void shouldReturnTokensWithValidCredentials() {
        User user = new User("user@example.com", "hash");
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "hash")).thenReturn(true);
        when(jwt.generate(any(), eq("user@example.com"))).thenReturn("access");
        when(refresh.create(any())).thenReturn("refresh");

        var response = controller.login(new AuthController.LoginRequest("user@example.com", "password"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void shouldThrowBadCredentialsWithWrongPassword() {
        User user = new User("user@example.com", "hash");
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() ->
                controller.login(new AuthController.LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldThrowBadCredentialsWithNonexistentEmail() {
        when(users.findByEmail("x@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                controller.login(new AuthController.LoginRequest("x@example.com", "password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldSaveUserAndReturnTokens() {
        when(encoder.encode("password")).thenReturn("hash");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generate(any(), any())).thenReturn("access");
        when(refresh.create(any())).thenReturn("refresh");

        var response = controller.register(new AuthController.RegisterRequest("user@example.com", "password"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(users).save(any(User.class));
    }

    @Test
    void shouldReturnNewTokens() {
        User user = new User("user@example.com", "hash");
        when(refresh.validateAndRotate("rt")).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(jwt.generate(any(), eq("user@example.com"))).thenReturn("access2");
        when(refresh.create(any())).thenReturn("refresh2");

        var response = controller.refresh(new AuthController.RefreshRequest("rt"));

        assertThat(response.accessToken()).isEqualTo("access2");
        assertThat(response.refreshToken()).isEqualTo("refresh2");
    }

    @Test
    void shouldThrowBadCredentialsWhenUserDoesNotExist() {
        when(refresh.validateAndRotate("rt")).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.refresh(new AuthController.RefreshRequest("rt")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
