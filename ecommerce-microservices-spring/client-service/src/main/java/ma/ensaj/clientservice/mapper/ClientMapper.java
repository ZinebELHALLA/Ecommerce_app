package ma.ensaj.clientservice.mapper;

import ma.ensaj.clientservice.dto.ClientRequestDTO;
import ma.ensaj.clientservice.dto.ClientResponseDTO;
import ma.ensaj.clientservice.entities.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public Client toEntity(ClientRequestDTO dto) {
        return Client.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .authUserId(dto.getAuthUserId())
                .build();
    }
    public ClientResponseDTO toDTO(Client client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAuthUserId(client.getAuthUserId());
        return dto;
    }
}
