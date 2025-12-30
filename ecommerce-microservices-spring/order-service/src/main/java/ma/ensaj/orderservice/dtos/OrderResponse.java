package ma.ensaj.orderservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensaj.orderservice.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private List<OrderLineResponse> items; // Changed from orderLines to items to match frontend
}
