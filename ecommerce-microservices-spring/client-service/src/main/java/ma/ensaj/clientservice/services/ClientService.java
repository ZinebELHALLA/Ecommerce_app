package ma.ensaj.clientservice.services;

import ma.ensaj.clientservice.dto.ClientRequestDTO;
import ma.ensaj.clientservice.dto.ClientResponseDTO;
import ma.ensaj.clientservice.dto.ClientValidationResponse;

import java.util.List;

public interface ClientService {
    ClientResponseDTO createClient(ClientRequestDTO dto);

    ClientResponseDTO getClientById(Long id);

    ClientResponseDTO getClientByAuthId(String authUserId);

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO updateClient(Long id, ClientRequestDTO dto);

    void deleteClient(Long id);
    
    // New method for inter-service communication
    ClientValidationResponse validateClient(Long clientId);
}
