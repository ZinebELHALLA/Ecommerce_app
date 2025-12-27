package ma.ensaj.cartservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Cart")
public class Cart implements Serializable {
    @Id
    private String id;
    @Indexed
    private Long userId;
    private List<CartItem> items=new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
