package ma.ensaj.productservice.services;

import ma.ensaj.productservice.dto.ProductRequestDTO;
import ma.ensaj.productservice.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO updateProduct(String id,ProductRequestDTO productRequestDTO);
    ProductResponseDTO getProductBYId(String id);
    void deleteProduct(String id);
    List<ProductResponseDTO> getAllProducts();


}
