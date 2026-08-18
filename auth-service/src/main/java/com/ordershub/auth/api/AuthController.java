package com.ordershub.auth.api;

import com.ordershub.auth.domain.User;
import com.ordershub.auth.repository.UserRepository;
import com.ordershub.auth.security.JwtService;
import com.ordershub.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final RefreshTokenService refresh;

    public AuthController(UserRepository users, PasswordEncoder encoder,
                          JwtService jwt, RefreshTokenService refresh) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.refresh = refresh;
    }

    record RegisterRequest(@Email @NotBlank String email, @NotBlank String password) {}
    record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    record RefreshRequest(@NotBlank String token) {}
    record AuthResponse(String accessToken, String refreshToken) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest body) {
        User user = users.save(new User(body.email(), encoder.encode(body.password())));
        return tokens(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest body) {
        User user = users.findByEmail(body.email())
                .filter(u -> encoder.matches(body.password(), u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        return tokens(user);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest body) {
        Long userId = refresh.validateAndRotate(body.token());
        User user = users.findById(userId).orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        return tokens(user);
    }

    private AuthResponse tokens(User user) {
        return new AuthResponse(
                jwt.generate(user.getId(), user.getEmail()),
                refresh.create(user.getId()));
    }
}