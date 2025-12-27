package ma.ensaj.productservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDTO {
    private String name;
    private String description;
}
