package ma.ensaj.inventoryservice.mapper;

import ma.ensaj.inventoryservice.dto.InventoryResponse;
import ma.ensaj.inventoryservice.entities.Inventory;
import org.springframework.stereotype.Component;

@Component

public class InventoryMapper {
    public InventoryResponse toResponse(Inventory inv) {
        InventoryResponse res = new InventoryResponse();
        res.setSkuCode(inv.getSkuCode());
        res.setQuantity(inv.getQuantity());
        res.setInStock(inv.getQuantity() > 0);
        return res;
    }
}
