package ma.ensaj.clientservice.dto;
import lombok.Data;
@Data
public class LoginRequest {
    private String username; // will use email as username
    private String password;
}