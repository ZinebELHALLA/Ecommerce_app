package ma.ensaj.cartservice.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private String skuCode;
    private Integer quantity;
    private BigDecimal price;
}
