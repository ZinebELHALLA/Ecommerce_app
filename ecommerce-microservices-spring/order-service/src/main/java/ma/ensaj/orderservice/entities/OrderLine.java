package ma.ensaj.orderservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "t_order_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skuCode;      // Product SKU (Stock Keeping Unit)
    private BigDecimal price;    // Unit price
    private Integer quantity;    // Quantity
}
