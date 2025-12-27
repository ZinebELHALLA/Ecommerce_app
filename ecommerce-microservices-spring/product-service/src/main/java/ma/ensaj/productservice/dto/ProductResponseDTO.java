package ma.ensaj.productservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    // Données renvoyées au client après traitement

    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String categoryId;
    private boolean active;
    //
    private String sku;
    private String imageUrl;

}
