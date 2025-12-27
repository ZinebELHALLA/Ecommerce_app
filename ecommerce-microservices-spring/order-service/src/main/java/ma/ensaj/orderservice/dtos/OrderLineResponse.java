package ma.ensaj.orderservice.dtos;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderLineResponse {
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
