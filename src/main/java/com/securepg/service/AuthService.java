package com.securepg.service;

import com.securepg.dto.AuthResponse;
import com.securepg.dto.LoginRequest;
import com.securepg.entity.AccountStatus;
import com.securepg.entity.User;
import com.securepg.exception.BusinessException;
import com.securepg.repository.TenantProfileRepository;
import com.securepg.repository.UserRepository;
import com.securepg.security.JwtTokenProvider;
import com.securepg.security.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var tenantProfileId = tenantProfileRepository.findByUser(user)
                .map(tp -> tp.getId())
                .orElse(null);

        JwtUserPrincipal principal = JwtUserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantProfileId(tenantProfileId)
                .build();

        String token = jwtTokenProvider.generateToken(principal);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
    }
}
