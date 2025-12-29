package ma.ensaj.inventoryservice.controllers;

import lombok.RequiredArgsConstructor;
import ma.ensaj.inventoryservice.dto.BatchInventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryResponse;
import ma.ensaj.inventoryservice.dto.InventoryValidationResponse;
import ma.ensaj.inventoryservice.services.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor

@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/{skuCode}")
    public InventoryResponse isInStock(@PathVariable String skuCode) {
        return inventoryService.checkStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse addProduct(@RequestBody InventoryRequest request) {
        return inventoryService.addProduct(request);
    }

    @PutMapping
    public InventoryResponse updateProduct(@RequestBody InventoryRequest request) {
        return inventoryService.updateStock(request);
    }
    @PostMapping("/deduct")
    @ResponseStatus(HttpStatus.OK)
    public void deductStock(@RequestBody List<BatchInventoryRequest> requests) {
        inventoryService.deductStock(requests);
    }
    // New endpoints for inter-service communication
    @PostMapping("/check-batch")
    public List<InventoryValidationResponse> checkBatchStock(@RequestBody List<BatchInventoryRequest> requests) {
        return inventoryService.checkBatchStock(requests);
    }
    
    @GetMapping("/check/{skuCode}")
    public InventoryValidationResponse checkStockWithQuantity(
            @PathVariable String skuCode, 
            @RequestParam Integer quantity) {
        return inventoryService.checkStockWithQuantity(skuCode, quantity);
    }
}
