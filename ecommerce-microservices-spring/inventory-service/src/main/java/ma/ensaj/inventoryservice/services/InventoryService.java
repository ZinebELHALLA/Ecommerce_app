package ma.ensaj.inventoryservice.services;

import lombok.RequiredArgsConstructor;
import ma.ensaj.inventoryservice.dto.BatchInventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryResponse;
import ma.ensaj.inventoryservice.dto.InventoryValidationResponse;
import ma.ensaj.inventoryservice.entities.Inventory;
import ma.ensaj.inventoryservice.mapper.InventoryMapper;
import ma.ensaj.inventoryservice.repositories.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper mapper;

    @Transactional(readOnly = true)
    public InventoryResponse checkStock(String skuCode) {
        Inventory inv = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapper.toResponse(inv);
    }
    @Transactional
    public void deductStock(List<BatchInventoryRequest> requests) {
        List<String> skuCodes = requests.stream()
                .map(BatchInventoryRequest::getSkuCode)
                .collect(Collectors.toList());

        // Un seul SELECT avec lock pessimiste
        List<Inventory> inventories = inventoryRepository.findBySkuCodeInForUpdate(skuCodes);

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, inv -> inv));

        for (BatchInventoryRequest request : requests) {
            Inventory inventory = inventoryMap.get(request.getSkuCode());

            if (inventory == null) {
                throw new RuntimeException("Product not found: " + request.getSkuCode());
            }

            if (inventory.getQuantity() < request.getRequiredQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + request.getSkuCode());
            }

            // Déduction atomique
            inventory.setQuantity(inventory.getQuantity() - request.getRequiredQuantity());
        }

        inventoryRepository.saveAll(inventories);
    }
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        Inventory inv = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        inv.setQuantity(request.getQuantity());
        inventoryRepository.save(inv);

        return mapper.toResponse(inv);
    }

    @Transactional
    public InventoryResponse addProduct(InventoryRequest request) {
        Inventory inv = new Inventory();
        inv.setSkuCode(request.getSkuCode());
        inv.setQuantity(request.getQuantity());

        inventoryRepository.save(inv);
        return mapper.toResponse(inv);
    }
    
    @Transactional(readOnly = true)
    public List<InventoryValidationResponse> checkBatchStock(List<BatchInventoryRequest> requests) {
        List<String> skuCodes = requests.stream()
                .map(BatchInventoryRequest::getSkuCode)
                .collect(Collectors.toList());
        
        List<Inventory> inventories = inventoryRepository.findBySkuCodeIn(skuCodes);
        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, inv -> inv));
        
        List<InventoryValidationResponse> responses = new ArrayList<>();
        
        for (BatchInventoryRequest request : requests) {
            Inventory inventory = inventoryMap.get(request.getSkuCode());
            
            if (inventory == null) {
                responses.add(InventoryValidationResponse.builder()
                        .skuCode(request.getSkuCode())
                        .inStock(false)
                        .availableQuantity(0)
                        .requestedQuantity(request.getRequiredQuantity())
                        .errorMessage("Product not found in inventory")
                        .build());
            } else {
                boolean inStock = inventory.getQuantity() >= request.getRequiredQuantity();
                responses.add(InventoryValidationResponse.builder()
                        .skuCode(request.getSkuCode())
                        .inStock(inStock)
                        .availableQuantity(inventory.getQuantity())
                        .requestedQuantity(request.getRequiredQuantity())
                        .errorMessage(inStock ? null : "Insufficient stock")
                        .build());
            }
        }
        
        return responses;
    }
    
    @Transactional(readOnly = true)
    public InventoryValidationResponse checkStockWithQuantity(String skuCode, Integer requiredQuantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode).orElse(null);
        
        if (inventory == null) {
            return InventoryValidationResponse.builder()
                    .skuCode(skuCode)
                    .inStock(false)
                    .availableQuantity(0)
                    .requestedQuantity(requiredQuantity)
                    .errorMessage("Product not found in inventory")
                    .build();
        }
        
        boolean inStock = inventory.getQuantity() >= requiredQuantity;
        return InventoryValidationResponse.builder()
                .skuCode(skuCode)
                .inStock(inStock)
                .availableQuantity(inventory.getQuantity())
                .requestedQuantity(requiredQuantity)
                .errorMessage(inStock ? null : "Insufficient stock")
                .build();
    }
}
