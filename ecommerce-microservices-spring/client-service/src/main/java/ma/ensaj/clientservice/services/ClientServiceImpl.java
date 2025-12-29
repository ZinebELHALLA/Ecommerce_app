package ma.ensaj.clientservice.services;

import lombok.RequiredArgsConstructor;
import ma.ensaj.clientservice.dto.ClientRequestDTO;
import ma.ensaj.clientservice.dto.ClientResponseDTO;
import ma.ensaj.clientservice.dto.ClientValidationResponse;
import ma.ensaj.clientservice.entities.Client;
import ma.ensaj.clientservice.mapper.ClientMapper;
import ma.ensaj.clientservice.repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper mapper;

    @Override
    public ClientResponseDTO createClient(ClientRequestDTO dto) {
        Client client = mapper.toEntity(dto);
        return mapper.toDTO(clientRepository.save(client));
    }

    @Override
    public ClientResponseDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return mapper.toDTO(client);
    }

    @Override
    public ClientResponseDTO getClientByAuthId(String authUserId) {
        Client client = clientRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return mapper.toDTO(client);
    }

    @Override
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());

        return mapper.toDTO(clientRepository.save(client));
    }

    @Override
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
    
    @Override
    public ClientValidationResponse validateClient(Long clientId) {
        Client client = clientRepository.findById(clientId).orElse(null);
        
        if (client == null) {
            return ClientValidationResponse.builder()
                    .clientId(clientId)
                    .exists(false)
                    .errorMessage("Client not found")
                    .build();
        }
        
        return ClientValidationResponse.builder()
                .clientId(clientId)
                .exists(true)
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .build();
    }
}
