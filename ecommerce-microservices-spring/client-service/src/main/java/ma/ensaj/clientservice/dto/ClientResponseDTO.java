package ma.ensaj.clientservice.dto;

import lombok.Data;

@Data
public class ClientResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String authUserId;
}
