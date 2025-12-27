package ma.ensaj.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data


public class OrderRequest {
    private List<OrderLineRequest> orderLines;

}
