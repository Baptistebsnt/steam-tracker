package com.steamtracker.auth;

import com.steamtracker.auth.dto.AuthResponse;
import com.steamtracker.auth.dto.LoginRequest;
import com.steamtracker.auth.dto.RegisterRequest;
import com.steamtracker.domain.user.User;
import com.steamtracker.domain.user.UserRepository;
import com.steamtracker.error.ConflictException;
import com.steamtracker.error.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email déjà utilisé");
        }

        var user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setSteamId(request.steamId());

        userRepository.save(user);

        var token = jwtService.generateToken(user.getEmail());
        return AuthResponse.of(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        var token = jwtService.generateToken(user.getEmail());
        return AuthResponse.of(token, user);
    }
}
