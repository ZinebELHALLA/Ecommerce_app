package ma.ensaj.clientservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    // ID utilisateur provenant du service Auth (Keycloak, auth-service…)
    @Column(nullable = false, unique = true)
    private String authUserId;
}
