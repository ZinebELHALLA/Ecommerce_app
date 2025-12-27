package ma.ensaj.inventoryservice.services;

import lombok.RequiredArgsConstructor;
import ma.ensaj.inventoryservice.dto.InventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryResponse;
import ma.ensaj.inventoryservice.entities.Inventory;
import ma.ensaj.inventoryservice.mapper.InventoryMapper;
import ma.ensaj.inventoryservice.repositories.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
