package ma.ensaj.inventoryservice.controllers;

import lombok.RequiredArgsConstructor;
import ma.ensaj.inventoryservice.dto.InventoryRequest;
import ma.ensaj.inventoryservice.dto.InventoryResponse;
import ma.ensaj.inventoryservice.services.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
}
