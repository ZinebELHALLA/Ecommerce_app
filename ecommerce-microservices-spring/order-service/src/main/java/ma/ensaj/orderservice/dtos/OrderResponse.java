package ma.ensaj.orderservice.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data

public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderLineResponse> orderLines;
}
