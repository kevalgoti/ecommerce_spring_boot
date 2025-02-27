package com.ecommerce.ecommerce.controller.user;

import com.ecommerce.ecommerce.dto.user.ForgetPasswordDTO;
import com.ecommerce.ecommerce.dto.user.ResetPasswordDTO;
import com.ecommerce.ecommerce.model.user.AuthResponse;
import com.ecommerce.ecommerce.dto.user.UserRegisterDTO;
import com.ecommerce.ecommerce.dto.user.UserLoginDTO;
import com.ecommerce.ecommerce.model.user.User;
import com.ecommerce.ecommerce.service.user.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegisterDTO userDTO) {
        User user = authService.saveUser(userDTO);
        return ResponseEntity.ok("User registered successfully with username: " + user.getUsername());
    }

    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token")String confirmationToken) {
        return authService.confirmEmail(confirmationToken);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginDTO) {
        AuthResponse authResponse = authService.authenticateUser(loginDTO);

        if (authResponse != null) {
            return ResponseEntity.ok(authResponse);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PutMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordDTO forgetPasswordDTO) {
        boolean emailSent = authService.forgetPassword(forgetPasswordDTO.getEmail());

        if (emailSent) {
            return ResponseEntity.ok("Password reset link sent to your email.");
        } else {
            return ResponseEntity.badRequest().body("Error: Email not found.");
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        boolean success = authService.resetPassword(resetPasswordDTO);
        if (success) {
            return ResponseEntity.ok("Password has been reset successfully!");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token!");
        }
    }



}