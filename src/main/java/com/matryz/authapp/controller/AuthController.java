package com.matryz.authapp.controller;

import com.matryz.authapp.dto.LoginRequest;
import com.matryz.authapp.dto.LoginResponse;
import com.matryz.authapp.dto.RegisterRequest;
import com.matryz.authapp.entity.User;
import com.matryz.authapp.repository.UserRepository;
import com.matryz.authapp.service.JwtService;
import com.matryz.authapp.service.MailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MailService mailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtService jwtService, MailService mailService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mail já cadastrado"));
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setConfirmationToken(UUID.randomUUID().toString());

        userRepository.save(user);

        mailService.sendConfirmationEmail(user.getEmail(), user.getName(), user.getConfirmationToken());

        return ResponseEntity.ok(Map.of("message", "Cadastro realizado! Verifique seu e-mail para ativar a conta."));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam String token) {
        var userOpt = userRepository.findByConfirmationToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token inválido ou já utilizado"));
        }

        User user = userOpt.get();
        user.setConfirmed(true);
        user.setConfirmationToken(null); // token é de uso único
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Conta confirmada com sucesso! Você já pode fazer login."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var userOpt = userRepository.findByEmail(request.email());

        // 1ª verificação: e-mail existe e senha bate? (mesmo erro pros dois casos)
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "E-mail ou senha inválidos"));
        }

        // 2ª verificação: a conta foi confirmada pelo e-mail?
        if (!userOpt.get().isConfirmed()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Conta não confirmada. Verifique seu e-mail."));
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail());

        return ResponseEntity.ok(new LoginResponse(token, user.getName()));
    }
}