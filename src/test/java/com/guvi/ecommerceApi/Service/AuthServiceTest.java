package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.LoginDTO;
import com.guvi.ecommerceApi.DTO.SignUpDTO;
import com.guvi.ecommerceApi.Entity.Role;
import com.guvi.ecommerceApi.Exception.UserAlreadyExistsException;
import com.guvi.ecommerceApi.Model.User;
import com.guvi.ecommerceApi.Repository.UserRepository;
import com.guvi.ecommerceApi.Security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // -------------------------------------------------------------
    // TEST 1: Signup - Username Already Exists (409 Conflict)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username is taken")
    void signup_UsernameAlreadyExists_ThrowsException() {
        SignUpDTO dto = new SignUpDTO();
        dto.setUsername("john_doe");
        dto.setPassword("secret123");

        // Mock repo reporting username already taken
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.signup(dto)
        );

        assertEquals("Username 'john_doe' is already taken", ex.getMessage());

        // Verify that password was never hashed and user was never saved
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------------------------------------------------
    // TEST 2: Signup - Customer Role (Happy Path + ArgumentCaptor)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should register new user with hashed password and default USER role")
    void signup_CustomerRole_Success() {
        SignUpDTO dto = new SignUpDTO();
        dto.setUsername("alice");
        dto.setPassword("plainPassword");
        dto.setRole("USER");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("bcrypt_hashed_value");

        String result = authService.signup(dto);

        assertEquals("User registered successfully!", result);

        // Use ArgumentCaptor to inspect the User object that was passed to save()
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("alice", savedUser.getUsername());
        assertEquals("bcrypt_hashed_value", savedUser.getPassword()); // Verifies password was encrypted
        assertEquals(Role.USER, savedUser.getRole());                 // Verifies role assignment
    }

    // -------------------------------------------------------------
    // TEST 3: Signup - ADMIN Role
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should assign Role.ADMIN when ADMIN role is requested in DTO")
    void signup_AdminRole_Success() {
        SignUpDTO dto = new SignUpDTO();
        dto.setUsername("admin_bob");
        dto.setPassword("adminSecret");
        dto.setRole("ADMIN");

        when(userRepository.existsByUsername("admin_bob")).thenReturn(false);
        when(passwordEncoder.encode("adminSecret")).thenReturn("hashed_admin_secret");

        authService.signup(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(Role.ADMIN, userCaptor.getValue().getRole());
    }

    // -------------------------------------------------------------
    // TEST 4: Login - Successful Authentication (JWT Generation)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should authenticate user and return Bearer JWT token map upon valid credentials")
    void login_ValidCredentials_ReturnsJwtToken() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("plainPassword");

        // Mock Spring Security Authentication & UserDetails
        Authentication authMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);

        when(userDetailsMock.getUsername()).thenReturn("alice");
        when(authMock.getPrincipal()).thenReturn(userDetailsMock);

        // Tell AuthenticationManager that credentials are valid
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        // Tell JwtUtil to generate token
        when(jwtUtil.generateToken(userDetailsMock)).thenReturn("mocked.jwt.token.abc");

        Map<String, Object> response = authService.login(dto);

        assertNotNull(response);
        assertEquals("mocked.jwt.token.abc", response.get("token"));
        assertEquals("Bearer", response.get("type"));
        assertEquals("alice", response.get("username"));

        verify(jwtUtil, times(1)).generateToken(userDetailsMock);
    }

    // -------------------------------------------------------------
    // TEST 5: Login - Invalid Password (BadCredentialsException)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw BadCredentialsException when password does not match")
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("wrongPassword");

        // Tell AuthenticationManager to simulate bad password
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(dto)
        );

        // Verify that JWT token was NEVER generated for an unauthenticated user!
        verify(jwtUtil, never()).generateToken(any());
    }
}