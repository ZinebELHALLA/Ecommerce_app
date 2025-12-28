package ma.ensaj.clientservice.controllers;
import lombok.RequiredArgsConstructor;
import ma.ensaj.clientservice.dto.LoginRequest;
import ma.ensaj.clientservice.dto.LoginResponse;
import ma.ensaj.clientservice.dto.RegisterRequest;
import ma.ensaj.clientservice.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}