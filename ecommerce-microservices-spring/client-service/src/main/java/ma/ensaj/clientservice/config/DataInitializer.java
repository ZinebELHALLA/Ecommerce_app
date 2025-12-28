package ma.ensaj.clientservice.config;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ma.ensaj.clientservice.entities.Client;
import ma.ensaj.clientservice.repositories.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    public void init() {
        if (!clientRepository.findByEmail("admin").isPresent()) {
            Client admin = Client.builder()
                    .email("admin")
                    .firstName("Admin")
                    .lastName("User")
                    .phone("0000000000")
                    .password(passwordEncoder.encode("admin"))
                    .role("MANAGER")
                    .authUserId("admin")
                    .build();
            clientRepository.save(admin);
            System.out.println("Admin user created: admin/admin");
        }
    }
}