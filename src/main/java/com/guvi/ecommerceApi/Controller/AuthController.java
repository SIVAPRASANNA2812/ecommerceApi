package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.LoginDTO;
import com.guvi.ecommerceApi.DTO.SignUpDTO;
import com.guvi.ecommerceApi.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Only AuthService is needed here
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/register", "/signup"})
    public ResponseEntity<String> register(@RequestBody SignUpDTO signUpDTO) {
        String response = this.authService.signup(signUpDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> response = this.authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
}