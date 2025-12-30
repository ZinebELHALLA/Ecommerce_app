package ma.ensaj.productservice.clients;

import ma.ensaj.productservice.dto.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {
    @PostMapping
    void addProduct(@RequestBody InventoryRequest request);
}
