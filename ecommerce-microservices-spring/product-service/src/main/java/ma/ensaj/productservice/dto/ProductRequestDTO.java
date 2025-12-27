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

public class ProductRequestDTO {
    //  Données envoyées par le client pour créer/modifier un produit
    //Pas d'id → généré par la base
    // Pas de active → par défaut true
    //Pas d'id →
    //Parce que dans une vraie API REST, l’ID ne vient jamais du body, mais de l’URL.
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String categoryId;

    private String sku;
    private String imageUrl;
    private boolean active;

}
