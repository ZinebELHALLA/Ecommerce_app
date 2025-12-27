package ma.ensaj.productservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDTO {
    private String id;
    private String name;
    private String description;
}
