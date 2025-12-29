package ma.ensaj.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientValidationResponse {
    private Long clientId;
    private boolean exists;
    private String firstName;
    private String lastName;
    private String email;
    private String errorMessage;
}