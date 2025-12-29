package ma.ensaj.clientservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensaj.clientservice.dto.ClientRequestDTO;
import ma.ensaj.clientservice.dto.ClientResponseDTO;
import ma.ensaj.clientservice.dto.ClientValidationResponse;
import ma.ensaj.clientservice.services.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    public ClientResponseDTO create(@Valid @RequestBody ClientRequestDTO dto) {
        return clientService.createClient(dto);
    }

    @GetMapping("/{id}")
    public ClientResponseDTO getById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }

    @GetMapping("/auth/{authUserId}")
    public ClientResponseDTO getByAuthId(@PathVariable String authUserId) {
        return clientService.getClientByAuthId(authUserId);
    }

    @GetMapping
    public List<ClientResponseDTO> getAll() {
        return clientService.getAllClients();
    }

    @PutMapping("/{id}")
    public ClientResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO dto
    ) {
        return clientService.updateClient(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.deleteClient(id);
    }
    
    // New endpoint for inter-service communication
    @GetMapping("/validate/{clientId}")
    public ClientValidationResponse validateClient(@PathVariable Long clientId) {
        return clientService.validateClient(clientId);
    }
}
