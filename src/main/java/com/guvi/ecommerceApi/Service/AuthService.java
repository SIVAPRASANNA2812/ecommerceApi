package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.LoginDTO;
import com.guvi.ecommerceApi.DTO.SignUpDTO;
import com.guvi.ecommerceApi.Entity.Role;
import com.guvi.ecommerceApi.Exception.UserAlreadyExistsException;
import com.guvi.ecommerceApi.Model.User;
import com.guvi.ecommerceApi.Repository.UserRepository;
import com.guvi.ecommerceApi.Security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Constructor Injection for all dependencies
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public String signup(SignUpDTO signUpDTO) {
        if (userRepository.existsByUsername(signUpDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + signUpDTO.getUsername() + "' is already taken");
        }

        User user = new User();
        user.setUsername(signUpDTO.getUsername());
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));

        // set role from DTO, default to USER if not provided
        if ("ADMIN".equalsIgnoreCase(signUpDTO.getRole())) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);

        return "User registered successfully!";
    }

    // New Login Method
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. Authenticate against database
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 2. Extract authenticated user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

        // 4. Return token payload
        return Map.of(
                "token", token,
                "type", "Bearer",
                "username", userDetails.getUsername()
        );
    }
}