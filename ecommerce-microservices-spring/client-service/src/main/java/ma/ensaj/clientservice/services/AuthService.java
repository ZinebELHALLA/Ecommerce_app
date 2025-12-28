package ma.ensaj.clientservice.services;
import lombok.RequiredArgsConstructor;
import ma.ensaj.clientservice.dto.LoginRequest;
import ma.ensaj.clientservice.dto.LoginResponse;
import ma.ensaj.clientservice.dto.RegisterRequest;
import ma.ensaj.clientservice.entities.Client;
import ma.ensaj.clientservice.repositories.ClientRepository;
import ma.ensaj.clientservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public LoginResponse login(LoginRequest request) {
        Client client = clientRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(client.getId(), client.getEmail(), client.getRole());
        return LoginResponse.builder()
                .token(token)
                .username(client.getEmail())
                .role(client.getRole())
                .userId(client.getId())
                .build();
    }
    public String register(RegisterRequest request) {
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        Client client = Client.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .authUserId(request.getEmail()) // Use email as authUserId
                .build();
        clientRepository.save(client);
        return "User registered successfully";
    }
}